package com.streamflix.user.fetcher;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.InputArgument;
import com.streamflix.user.auth.JwtAuthHelper;
import com.streamflix.user.dto.AuthPayload;

@DgsComponent
public class LoginDataFetcher {

    @DgsMutation
    public AuthPayload login(@InputArgument String username, @InputArgument String password) {

        System.out.println("Login attempt for username: " + username + " with password: " + password);

        if ("password123".equals(password)) {
            String access = JwtAuthHelper.mintAccessToken(username);
            String refresh = JwtAuthHelper.mintRefreshToken(username);
            return new AuthPayload(access, refresh, username);
        }

        throw new RuntimeException("Invalid credentials!");
    }
}
