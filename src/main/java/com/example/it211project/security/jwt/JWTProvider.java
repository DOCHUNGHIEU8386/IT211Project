package com.example.it211project.security.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JWTProvider {

    private static final String SECRET_KEY =
            "mySecretKeymySecretKeymySecretKeymySecretKey";

    private static final long EXPIRATION_TIME = 86400000; // 24h

    private SecretKey key;

    @PostConstruct
    public void init() {
        key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    /**
     * Tạo JWT token
     */
    public String generateToken(Authentication authentication) {

        return Jwts.builder()
                .subject(authentication.getName())
                .issuedAt(new Date())
                .expiration(
                        new Date(System.currentTimeMillis() + EXPIRATION_TIME)
                )
                .signWith(key)
                .compact();
    }

    /**
     * Lấy username từ token
     */
    public String getUsernameFromToken(String token) {

        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    /**
     * Kiểm tra token hợp lệ
     */
    public boolean validateToken(String token) {

        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);

            return true;

        } catch (Exception e) {
            return false;
        }
    }
}