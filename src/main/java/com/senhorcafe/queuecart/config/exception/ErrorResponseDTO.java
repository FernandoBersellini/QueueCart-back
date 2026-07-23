package com.senhorcafe.queuecart.config.exception;

import java.time.Instant;

public record ErrorResponseDTO(
        String message,
        String errorCode,
        Instant timestamp
) {}
