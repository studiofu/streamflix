package com.streamflix.user.fetcher;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.InputArgument;
import com.streamflix.user.dto.AuthPayload;
import com.streamflix.user.model.User;
import com.streamflix.user.repository.UserRepository;
import com.streamflix.user.service.AuthTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

@DgsComponent
public class RegisterDataFetcher {

    private static final int MIN_PASSWORD_LENGTH = 6;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthTokenService authTokenService;

    @DgsMutation
    public AuthPayload register(
        @InputArgument String username,
        @InputArgument String email,
        @InputArgument String password
    ) {
        if (username == null || username.isBlank()) {
            throw new RuntimeException("Username is required");
        }
        if (email == null || email.isBlank()) {
            throw new RuntimeException("Email is required");
        }
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            throw new RuntimeException("Password must be at least " + MIN_PASSWORD_LENGTH + " characters");
        }
        String u = username.trim();
        if (userRepository.existsByUsername(u)) {
            throw new RuntimeException("Username already taken");
        }
        User user = new User(u, email.trim());
        user.setPasswordHash(passwordEncoder.encode(password));
        userRepository.save(user);
        return authTokenService.forUser(user);
    }
}
