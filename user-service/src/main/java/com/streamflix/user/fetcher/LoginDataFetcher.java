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
public class LoginDataFetcher {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthTokenService authTokenService;

    @DgsMutation
    public AuthPayload login(@InputArgument String username, @InputArgument String password) {
        if (username == null || username.isBlank()) {
            throw new RuntimeException("Invalid credentials");
        }
        if (password == null) {
            throw new RuntimeException("Invalid credentials");
        }
        User user = userRepository.findByUsername(username.trim())
            .orElseThrow(() -> new RuntimeException("Invalid credentials"));
        if (user.getPasswordHash() == null || user.getPasswordHash().isBlank()) {
            throw new RuntimeException("Invalid credentials");
        }
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new RuntimeException("Invalid credentials");
        }
        return authTokenService.forUser(user);
    }
}
