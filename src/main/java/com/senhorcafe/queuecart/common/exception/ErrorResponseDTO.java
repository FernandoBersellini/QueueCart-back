package com.senhorcafe.queuecart.common.exception;

import java.time.Instant;

public record ErrorResponseDTO(
        String message,
        String errorCode,
        Instant timestamp
) {}
