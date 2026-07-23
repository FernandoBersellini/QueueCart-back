package com.senhorcafe.queuecart.user.dto;

public record AuthResponseDTO(
   Long userId,
   String email,
   String name,
   //This field will be removed before deployment, tokens will be passed using cookies
   String token
) {}
