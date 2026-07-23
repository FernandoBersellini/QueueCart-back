package com.senhorcafe.queuecart.config.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenDenylistServiceTest {

    @Mock
    private RevokedTokenRepository revokedTokenRepository;

    private TokenDenylistService tokenDenylistService;

    @BeforeEach
    void setUp() {
        tokenDenylistService = new TokenDenylistService(revokedTokenRepository);
    }

    @Test
    void revokeShouldPersistNewJti() {
        Instant expiresAt = Instant.now().plusSeconds(3600);
        when(revokedTokenRepository.existsByJti("jti-123")).thenReturn(false);

        tokenDenylistService.revoke("jti-123", expiresAt);

        ArgumentCaptor<RevokedToken> captor = ArgumentCaptor.forClass(RevokedToken.class);
        verify(revokedTokenRepository).save(captor.capture());
        assertThat(captor.getValue().getJti()).isEqualTo("jti-123");
        assertThat(captor.getValue().getExpiresAt()).isEqualTo(expiresAt);
    }

    @Test
    void revokeShouldBeIdempotentForAlreadyRevokedJti() {
        when(revokedTokenRepository.existsByJti("jti-123")).thenReturn(true);

        tokenDenylistService.revoke("jti-123", Instant.now().plusSeconds(3600));

        verify(revokedTokenRepository, never()).save(any());
    }

    @Test
    void isRevokedShouldDelegateToRepository() {
        when(revokedTokenRepository.existsByJti("jti-123")).thenReturn(true);

        assertThat(tokenDenylistService.isRevoked("jti-123")).isTrue();
    }
}
