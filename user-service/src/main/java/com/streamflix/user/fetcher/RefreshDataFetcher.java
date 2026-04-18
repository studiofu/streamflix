package com.streamflix.user.fetcher;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.InputArgument;
import com.streamflix.user.auth.JwtAuthHelper;
import com.streamflix.user.dto.AuthPayload;

@DgsComponent
public class RefreshDataFetcher {

    @DgsMutation
    public AuthPayload refresh(@InputArgument String refreshToken) {
        try {
            DecodedJWT decoded = JwtAuthHelper.verifyRefreshToken(refreshToken);
            String userId = decoded.getClaim(JwtAuthHelper.CLAIM_USER_ID).asString();
            if (userId == null || userId.isBlank()) {
                throw new RuntimeException("Invalid refresh token");
            }
            String newAccess = JwtAuthHelper.mintAccessToken(userId);
            String newRefresh = JwtAuthHelper.mintRefreshToken(userId);
            return new AuthPayload(newAccess, newRefresh, userId);
        } catch (JWTVerificationException e) {
            throw new RuntimeException("Invalid or expired refresh token", e);
        }
    }
}
