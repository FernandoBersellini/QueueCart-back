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
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TokenDenylistService tokenDenylistService;

    public AuthResponseDTO signUp(SignUpDTO signUpDTO) {
        if (userRepository.existsByEmail(signUpDTO.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use");
        }

        User user = new User();
        user.setEmail(signUpDTO.email());
        user.setName(signUpDTO.name());
        user.setPasswordHash(passwordEncoder.encode(signUpDTO.password()));
        user.setRole(Role.CUSTOMER);

        User saved = userRepository.save(user);
        return toAuthResponse(saved);
    }

    public AuthResponseDTO signIn(SignInDTO signInDTO) {
        User user = userRepository.findByEmail(signInDTO.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));

        if (!user.isActive() || !passwordEncoder.matches(signInDTO.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        return toAuthResponse(user);
    }

    public void logout(String token) {
        try {
            Claims claims = jwtService.parseClaims(token);
            tokenDenylistService.revoke(claims.getId(), jwtService.extractExpiration(claims));
        } catch (JwtException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }
    }

    private AuthResponseDTO toAuthResponse(User user) {
        String token = jwtService.generateToken(user);
        return new AuthResponseDTO(user.getId(), user.getEmail(), user.getName(), token);
    }
}
