package com.example.it211project.service;

import com.example.it211project.dto.response.TokenResponse;
import com.example.it211project.entity.RefreshToken;
import com.example.it211project.entity.User;

public interface RefreshTokenService {

    /** FR-02: Tạo refresh token mới cho user */
    RefreshToken createRefreshToken(User user);

    /** FR-02: Xác minh refresh token còn valid không */
    RefreshToken verifyRefreshToken(String token);

    /** FR-02: Xoay vòng token – revoke cũ, cấp mới */
    TokenResponse rotateToken(String oldRefreshToken);

    /** FR-03: Revoke toàn bộ token của user (logout) */
    void revokeAllTokensForUser(User user);
}
