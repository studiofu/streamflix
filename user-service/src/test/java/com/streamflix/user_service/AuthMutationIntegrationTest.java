package com.streamflix.user_service;

import com.streamflix.user.auth.JwtAuthHelper;
import com.streamflix.user.dto.AuthPayload;
import com.streamflix.user.fetcher.LoginDataFetcher;
import com.streamflix.user.fetcher.RefreshDataFetcher;
import com.streamflix.user.fetcher.RegisterDataFetcher;
import com.streamflix.user.model.User;
import com.streamflix.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuthMutationIntegrationTest {

    @Autowired
    private RegisterDataFetcher registerDataFetcher;

    @Autowired
    private LoginDataFetcher loginDataFetcher;

    @Autowired
    private RefreshDataFetcher refreshDataFetcher;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void cleanDb() {
        userRepository.deleteAll();
    }

    @Test
    void registerPersistsUserAndReturnsUuidAndTokens() {
        AuthPayload p = registerDataFetcher.register("newuser", "new@example.com", "secret12");

        assertThat(p.userId()).matches("[0-9a-fA-F-]{36}");
        assertThat(p.username()).isEqualTo("newuser");
        assertThat(p.token()).isNotBlank();
        assertThat(p.refreshToken()).isNotBlank();

        User u = userRepository.findByUsername("newuser").orElseThrow();
        assertThat(passwordEncoder.matches("secret12", u.getPasswordHash())).isTrue();
    }

    @Test
    void registerRejectsDuplicateUsername() {
        registerDataFetcher.register("dup", "a@x.com", "secret12");
        assertThatThrownBy(() -> registerDataFetcher.register("dup", "b@x.com", "otherpwd12"))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Username already taken");
    }

    @Test
    void registerRejectsShortPassword() {
        assertThatThrownBy(() -> registerDataFetcher.register("u", "u@x.com", "short"))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("6");
    }

    @Test
    void loginSucceedsWithCorrectPassword() {
        registerDataFetcher.register("logme", "logme@x.com", "correct12");
        AuthPayload p = loginDataFetcher.login("logme", "correct12");
        assertThat(p.userId()).isNotBlank();
        assertThat(p.username()).isEqualTo("logme");
    }

    @Test
    void loginFailsForWrongPasswordUnknownUserAndMissingHash() {
        registerDataFetcher.register("x", "x@x.com", "goodpass12");
        assertThatThrownBy(() -> loginDataFetcher.login("x", "wrongpass12"))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Invalid credentials");

        assertThatThrownBy(() -> loginDataFetcher.login("nobody", "any"))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Invalid credentials");

        User noHash = new User("nohash", "n@x.com");
        noHash.setPasswordHash(null);
        userRepository.save(noHash);
        assertThatThrownBy(() -> loginDataFetcher.login("nohash", "x"))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Invalid credentials");
    }

    @Test
    void refreshReturnsNewTokensForValidRefreshToken() {
        AuthPayload first = registerDataFetcher.register("ref", "ref@x.com", "secret12");
        AuthPayload second = refreshDataFetcher.refresh(first.refreshToken());

        assertThat(second.userId()).isEqualTo(first.userId());
        assertThat(second.username()).isEqualTo("ref");
        assertThat(second.token()).isNotBlank();
        assertThat(second.refreshToken()).isNotBlank();
        JwtAuthHelper.verifyRefreshToken(second.refreshToken());
    }

    @Test
    void refreshFailsWhenUserIdNotInDatabase() {
        String orphanRefresh = JwtAuthHelper.mintRefreshToken(UUID.randomUUID().toString());
        assertThatThrownBy(() -> refreshDataFetcher.refresh(orphanRefresh))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Invalid refresh token");
    }

    @Test
    void refreshFailsWhenUserRemoved() {
        AuthPayload p = registerDataFetcher.register("gone", "gone@x.com", "secret12");
        userRepository.deleteAll();
        assertThatThrownBy(() -> refreshDataFetcher.refresh(p.refreshToken()))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Invalid refresh token");
    }
}
