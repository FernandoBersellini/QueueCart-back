package com.senhorcafe.queuecart.config.security;

import com.senhorcafe.queuecart.user.entity.Role;
import com.senhorcafe.queuecart.user.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRET = "test-secret-key-with-at-least-256-bits-for-hs512-signing";

    private User buildUser() {
        User user = new User();
        user.setId(1L);
        user.setEmail("fernando@queuecart.dev");
        user.setRole(Role.CUSTOMER);
        return user;
    }

    @Test
    void generateTokenShouldProduceClaimsReadableByParseClaims() {
        JwtService jwtService = new JwtService(SECRET, 60_000);
        User user = buildUser();

        String token = jwtService.generateToken(user);
        Claims claims = jwtService.parseClaims(token);

        assertThat(claims.getSubject()).isEqualTo("fernando@queuecart.dev");
        assertThat(jwtService.extractUserId(claims)).isEqualTo(1L);
        assertThat(claims.get("role", String.class)).isEqualTo("CUSTOMER");
        assertThat(claims.getId()).isNotBlank();
    }

    @Test
    void generateTokenShouldProduceUniqueJtiPerToken() {
        JwtService jwtService = new JwtService(SECRET, 60_000);
        User user = buildUser();

        String firstJti = jwtService.parseClaims(jwtService.generateToken(user)).getId();
        String secondJti = jwtService.parseClaims(jwtService.generateToken(user)).getId();

        assertThat(firstJti).isNotEqualTo(secondJti);
    }

    @Test
    void extractExpirationShouldReturnTokenExpirationInstant() {
        JwtService jwtService = new JwtService(SECRET, 60_000);
        Claims claims = jwtService.parseClaims(jwtService.generateToken(buildUser()));

        assertThat(jwtService.extractExpiration(claims)).isEqualTo(claims.getExpiration().toInstant());
    }

    @Test
    void parseClaimsShouldThrowForExpiredToken() throws InterruptedException {
        JwtService jwtService = new JwtService(SECRET, 1);
        String token = jwtService.generateToken(buildUser());

        Thread.sleep(10);

        assertThatThrownBy(() -> jwtService.parseClaims(token))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void parseClaimsShouldThrowForTokenSignedWithDifferentSecret() {
        JwtService issuer = new JwtService(SECRET, 60_000);
        JwtService verifier = new JwtService("another-secret-key-with-at-least-256-bits-for-hs512", 60_000);
        String token = issuer.generateToken(buildUser());

        assertThatThrownBy(() -> verifier.parseClaims(token))
                .isInstanceOf(io.jsonwebtoken.security.SignatureException.class);
    }
}
