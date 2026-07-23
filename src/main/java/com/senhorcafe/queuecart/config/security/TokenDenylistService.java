package com.senhorcafe.queuecart.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class TokenDenylistService {
    private final RevokedTokenRepository revokedTokenRepository;

    @Transactional
    public void revoke(String jti, Instant expiresAt) {
        if (revokedTokenRepository.existsByJti(jti)) {
            return;
        }

        RevokedToken revokedToken = new RevokedToken();
        revokedToken.setJti(jti);
        revokedToken.setExpiresAt(expiresAt);
        revokedTokenRepository.save(revokedToken);
    }

    public boolean isRevoked(String jti) {
        return revokedTokenRepository.existsByJti(jti);
    }
}
