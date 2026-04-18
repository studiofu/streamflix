package com.streamflix.user.dto;

public record AuthPayload(String token, String refreshToken, String userId) {}
