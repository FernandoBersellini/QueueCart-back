package com.senhorcafe.queuecart.user.controller;

import com.senhorcafe.queuecart.user.dto.AuthResponseDTO;
import com.senhorcafe.queuecart.user.dto.SignInDTO;
import com.senhorcafe.queuecart.user.dto.SignUpDTO;
import com.senhorcafe.queuecart.user.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("user/auth/")
@RequiredArgsConstructor
public class UserController {
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final AuthService authService;

    @PostMapping("sign-up")
    public ResponseEntity<AuthResponseDTO> signUp(@RequestBody SignUpDTO signUpDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.signUp(signUpDTO));
    }

    @PostMapping("sign-in")
    public ResponseEntity<AuthResponseDTO> signIn(@RequestBody SignInDTO signInDTO) {
        return ResponseEntity.ok(authService.signIn(signInDTO));
    }

    @PostMapping("logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        authService.logout(extractToken(request));
        return ResponseEntity.noContent().build();
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader(AUTHORIZATION_HEADER);
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing or invalid Authorization header");
        }
        return header.substring(BEARER_PREFIX.length());
    }
}
