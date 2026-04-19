package com.streamflix.user.fetcher;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.InputArgument;
import com.streamflix.user.auth.JwtAuthHelper;
import com.streamflix.user.dto.AuthPayload;
import com.streamflix.user.model.User;
import com.streamflix.user.repository.UserRepository;
import com.streamflix.user.service.AuthTokenService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

@DgsComponent
public class RefreshDataFetcher {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthTokenService authTokenService;

    @DgsMutation
    public AuthPayload refresh(@InputArgument String refreshToken) {
        try {
            DecodedJWT decoded = JwtAuthHelper.verifyRefreshToken(refreshToken);
            String userIdStr = decoded.getClaim(JwtAuthHelper.CLAIM_USER_ID).asString();
            if (userIdStr == null || userIdStr.isBlank()) {
                throw new RuntimeException("Invalid refresh token");
            }
            UUID id = UUID.fromString(userIdStr);
            User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));
            return authTokenService.forUser(user);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid refresh token", e);
        } catch (JWTVerificationException e) {
            throw new RuntimeException("Invalid or expired refresh token", e);
        }
    }
}
