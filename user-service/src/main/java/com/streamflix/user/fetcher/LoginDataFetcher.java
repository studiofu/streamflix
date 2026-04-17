package com.streamflix.user.fetcher;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.InputArgument;

import java.util.Date;

@DgsComponent
public class LoginDataFetcher {

    // A simple record to hold the return data
    public record AuthPayload(String token, String userId) {}

    @DgsMutation
    public AuthPayload login(@InputArgument String username, @InputArgument String password) {

        System.out.println("Login attempt for username: " + username + " with password: " + password);
        
        // DUMMY CHECK: If password is "password123", let them in!
        if ("password123".equals(password)) {
            
            // This MUST match the secret key in your Node.js Gateway!
            Algorithm algorithm = Algorithm.HMAC256("super-secret-streamflix-key");
            
            String token = JWT.create()
                    .withClaim("userId", username) // Embed the username inside the token
                    .withExpiresAt(new Date(System.currentTimeMillis() + 3600 * 1000)) // 1 hour
                    .sign(algorithm);
                    
            return new AuthPayload(token, username);
        }
        
        throw new RuntimeException("Invalid credentials!");
    }
}