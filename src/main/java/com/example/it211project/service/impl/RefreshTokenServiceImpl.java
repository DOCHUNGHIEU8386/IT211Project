package com.example.it211project.service.impl;

import com.example.it211project.dto.response.TokenResponse;
import com.example.it211project.entity.RefreshToken;
import com.example.it211project.entity.User;
import com.example.it211project.exception.ResourceNotFoundException;
import com.example.it211project.repository.RefreshTokenRepository;
import com.example.it211project.security.jwt.JWTProvider;
import com.example.it211project.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JWTProvider jwtProvider;

    /** Thời gian sống của refresh token: 7 ngày (ms) */
    @Value("${jwt.refresh-expiration-ms:604800000}")
    private Long refreshExpirationMs;

    // ==================== FR-02: Tạo refresh token ====================
    @Override
    @Transactional
    public RefreshToken createRefreshToken(User user) {
        // Giải quyết lỗi Duplicate entry cho ràng buộc UNIQUE user_id
        refreshTokenRepository.deleteByUser(user);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(refreshExpirationMs))
                .revoked(false)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    // ==================== FR-02: Xác minh refresh token ====================
    @Override
    public RefreshToken verifyRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Refresh token not found: " + token));

        if (refreshToken.getRevoked()) {
            throw new RuntimeException("Refresh token has been revoked");
        }

        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new RuntimeException("Refresh token has expired. Please login again.");
        }

        return refreshToken;
    }

    // ==================== FR-02: Xoay vòng token ====================
    @Override
    @Transactional
    public TokenResponse rotateToken(String oldRefreshToken) {
        RefreshToken verified = verifyRefreshToken(oldRefreshToken);
        User user = verified.getUser();

        // Revoke token cũ
        verified.setRevoked(true);
        refreshTokenRepository.save(verified);

        // Tạo access token mới
        List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                .map(r -> new SimpleGrantedAuthority(r.getRoleName()))
                .collect(Collectors.toList());

        Authentication auth = new UsernamePasswordAuthenticationToken(
                user.getUsername(), null, authorities);
        String newAccessToken = jwtProvider.generateToken(auth);

        // Tạo refresh token mới (sẽ xóa cái cũ do createRefreshToken đã xóa)
        RefreshToken newRefreshToken = createRefreshToken(user);

        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken.getToken())
                .tokenType("Bearer")
                .build();
    }

    // ==================== FR-03: Revoke tất cả token (logout) ====================
    @Override
    @Transactional
    public void revokeAllTokensForUser(User user) {
        refreshTokenRepository.deleteByUser(user);
    }
}