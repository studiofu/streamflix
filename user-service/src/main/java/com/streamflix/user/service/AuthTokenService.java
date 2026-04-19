package com.streamflix.user.service;

import com.streamflix.user.auth.JwtAuthHelper;
import com.streamflix.user.dto.AuthPayload;
import com.streamflix.user.model.User;
import org.springframework.stereotype.Service;

@Service
public class AuthTokenService {

    public AuthPayload forUser(User user) {
        String userId = user.getId().toString();
        return new AuthPayload(
            JwtAuthHelper.mintAccessToken(userId),
            JwtAuthHelper.mintRefreshToken(userId),
            userId,
            user.getUsername()
        );
    }
}
