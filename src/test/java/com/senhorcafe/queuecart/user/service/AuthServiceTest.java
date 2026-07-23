package com.senhorcafe.queuecart.user.service;

import com.senhorcafe.queuecart.config.security.JwtService;
import com.senhorcafe.queuecart.config.security.TokenDenylistService;
import com.senhorcafe.queuecart.user.dto.AuthResponseDTO;
import com.senhorcafe.queuecart.user.dto.SignInDTO;
import com.senhorcafe.queuecart.user.dto.SignUpDTO;
import com.senhorcafe.queuecart.user.entity.Role;
import com.senhorcafe.queuecart.user.entity.User;
import com.senhorcafe.queuecart.user.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private TokenDenylistService tokenDenylistService;

    @Mock
    private Claims claims;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder, jwtService, tokenDenylistService);
    }

    private User buildUser(String email, String passwordHash, boolean active) {
        User user = new User();
        user.setId(1L);
        user.setEmail(email);
        user.setName("Fernando");
        user.setPasswordHash(passwordHash);
        user.setRole(Role.CUSTOMER);
        user.setActive(active);
        return user;
    }

    @Test
    void signUpShouldCreateUserAndReturnToken() {
        SignUpDTO signUpDTO = new SignUpDTO("fernando@queuecart.dev", "senha123", "Fernando");
        when(userRepository.existsByEmail(signUpDTO.email())).thenReturn(false);
        when(passwordEncoder.encode(signUpDTO.password())).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");

        AuthResponseDTO response = authService.signUp(signUpDTO);

        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo("fernando@queuecart.dev");
        assertThat(response.token()).isEqualTo("jwt-token");
    }

    @Test
    void signUpShouldRejectDuplicateEmail() {
        SignUpDTO signUpDTO = new SignUpDTO("fernando@queuecart.dev", "senha123", "Fernando");
        when(userRepository.existsByEmail(signUpDTO.email())).thenReturn(true);

        assertThatThrownBy(() -> authService.signUp(signUpDTO))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", HttpStatus.CONFLICT);
    }

    @Test
    void signInShouldReturnTokenForValidCredentials() {
        User user = buildUser("fernando@queuecart.dev", "hashed", true);
        SignInDTO signInDTO = new SignInDTO("fernando@queuecart.dev", "senha123");
        when(userRepository.findByEmail("fernando@queuecart.dev")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("senha123", "hashed")).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        AuthResponseDTO response = authService.signIn(signInDTO);

        assertThat(response.token()).isEqualTo("jwt-token");
    }

    @Test
    void signInShouldRejectUnknownEmail() {
        when(userRepository.findByEmail("naoexiste@queuecart.dev")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.signIn(new SignInDTO("naoexiste@queuecart.dev", "senha123")))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", HttpStatus.UNAUTHORIZED);
    }

    @Test
    void signInShouldRejectWrongPassword() {
        User user = buildUser("fernando@queuecart.dev", "hashed", true);
        when(userRepository.findByEmail("fernando@queuecart.dev")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("errada", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> authService.signIn(new SignInDTO("fernando@queuecart.dev", "errada")))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", HttpStatus.UNAUTHORIZED);
    }

    @Test
    void signInShouldRejectInactiveUser() {
        User user = buildUser("fernando@queuecart.dev", "hashed", false);
        when(userRepository.findByEmail("fernando@queuecart.dev")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.signIn(new SignInDTO("fernando@queuecart.dev", "senha123")))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", HttpStatus.UNAUTHORIZED);
    }

    @Test
    void logoutShouldRevokeTokenJti() {
        Instant expiresAt = Instant.now().plusSeconds(3600);
        when(jwtService.parseClaims("valid-token")).thenReturn(claims);
        when(claims.getId()).thenReturn("jti-123");
        when(jwtService.extractExpiration(claims)).thenReturn(expiresAt);

        authService.logout("valid-token");

        verify(tokenDenylistService).revoke("jti-123", expiresAt);
    }

    @Test
    void logoutShouldRejectInvalidToken() {
        when(jwtService.parseClaims("garbage")).thenThrow(new JwtException("malformed"));

        assertThatThrownBy(() -> authService.logout("garbage"))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", HttpStatus.UNAUTHORIZED);

        verify(tokenDenylistService, never()).revoke(any(), any());
    }
}
