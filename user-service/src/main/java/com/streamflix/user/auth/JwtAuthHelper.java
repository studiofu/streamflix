package com.streamflix.user.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.util.Date;

/**
 * Must stay aligned with {@code federation-gateway} JWT_SECRET and claim shape ({@code userId}, {@code typ}).
 */
public final class JwtAuthHelper {

    /** Same default as {@code federation-gateway/index.js} JWT_SECRET. */
    public static final String JWT_SECRET =
            System.getenv().getOrDefault("JWT_SECRET", "super-secret-streamflix-key");

    public static final String CLAIM_USER_ID = "userId";
    public static final String CLAIM_TYP = "typ";
    public static final String TYPE_ACCESS = "access";
    public static final String TYPE_REFRESH = "refresh";

    private static final long ACCESS_TTL_MS = 15 * 60 * 1000L;
    private static final long REFRESH_TTL_MS = 30L * 24 * 60 * 60 * 1000L;

    private JwtAuthHelper() {}

    private static Algorithm algorithm() {
        return Algorithm.HMAC256(JWT_SECRET);
    }

    public static String mintAccessToken(String userId) {
        return JWT.create()
                .withClaim(CLAIM_USER_ID, userId)
                .withClaim(CLAIM_TYP, TYPE_ACCESS)
                .withExpiresAt(new Date(System.currentTimeMillis() + ACCESS_TTL_MS))
                .sign(algorithm());
    }

    public static String mintRefreshToken(String userId) {
        return JWT.create()
                .withClaim(CLAIM_USER_ID, userId)
                .withClaim(CLAIM_TYP, TYPE_REFRESH)
                .withExpiresAt(new Date(System.currentTimeMillis() + REFRESH_TTL_MS))
                .sign(algorithm());
    }

    /**
     * Verifies signature, expiry, and {@code typ == refresh}.
     */
    public static DecodedJWT verifyRefreshToken(String refreshToken) {
        JWTVerifier verifier = JWT.require(algorithm())
                .withClaim(CLAIM_TYP, TYPE_REFRESH)
                .build();
        return verifier.verify(refreshToken);
    }
}
