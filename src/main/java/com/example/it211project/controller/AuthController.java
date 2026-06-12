package com.example.it211project.controller;

import com.example.it211project.dto.request.*;
import com.example.it211project.dto.response.ApiDataResponse;
import com.example.it211project.dto.response.TokenResponse;
import com.example.it211project.dto.response.UserResponse;
import com.example.it211project.entity.RefreshToken;
import com.example.it211project.entity.User;
import com.example.it211project.repository.UserRepository;
import com.example.it211project.security.jwt.JWTProvider;
import com.example.it211project.service.RefreshTokenService;
import com.example.it211project.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JWTProvider jwtProvider;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;

    // ==================== FR-04: Đăng ký ====================
    /**
     * POST /api/v1/auth/register
     * Đăng ký tài khoản sinh viên mới
     */
    @PostMapping("/register")
    public ResponseEntity<ApiDataResponse<UserResponse>> register(
            @Valid @RequestBody RegisterRequest request) {

        UserResponse userResponse = userService.register(request);

        return new ResponseEntity<>(
                new ApiDataResponse<>(
                        true,
                        "Register successful",
                        userResponse,
                        null,
                        HttpStatus.CREATED
                ),
                HttpStatus.CREATED
        );
    }

    // ==================== FR-01: Đăng nhập – Cấp phát JWT ====================
    /**
     * POST /api/v1/auth/login
     * Trả về accessToken + refreshToken
     */
    @PostMapping("/login")
    public ResponseEntity<ApiDataResponse<TokenResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.getUsername(),
                                request.getPassword()
                        )
                );

        String accessToken = jwtProvider.generateToken(authentication);

        // Lấy User entity để tạo refresh token
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow();

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        TokenResponse tokenResponse = TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .build();

        return ResponseEntity.ok(
                new ApiDataResponse<>(
                        true,
                        "Login successful",
                        tokenResponse,
                        null,
                        HttpStatus.OK
                )
        );
    }

    // ==================== FR-02: Xoay vòng Token ====================
    /**
     * POST /api/v1/auth/refresh-token
     * Gửi refreshToken cũ -> nhận accessToken + refreshToken mới
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<ApiDataResponse<TokenResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {

        TokenResponse tokenResponse = refreshTokenService.rotateToken(request.getRefreshToken());

        return ResponseEntity.ok(
                new ApiDataResponse<>(
                        true,
                        "Token refreshed successfully",
                        tokenResponse,
                        null,
                        HttpStatus.OK
                )
        );
    }

    // ==================== FR-03: Đăng xuất (Revoke Token) ====================
    /**
     * POST /api/v1/auth/logout
     * Revoke toàn bộ refresh token của user hiện tại
     * Header: Authorization: Bearer <accessToken>
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiDataResponse<String>> logout(
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow();

        refreshTokenService.revokeAllTokensForUser(user);

        return ResponseEntity.ok(
                new ApiDataResponse<>(
                        true,
                        "Logged out successfully",
                        "All tokens have been revoked",
                        null,
                        HttpStatus.OK
                )
        );
    }

    // ==================== FR-10: Đổi mật khẩu ====================
    /**
     * POST /api/v1/auth/change-password
     * Yêu cầu đăng nhập (Bearer token)
     */
    @PostMapping("/change-password")
    public ResponseEntity<ApiDataResponse<String>> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ChangePasswordRequest request) {

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow();

        userService.changePassword(user.getId(), request);

        return ResponseEntity.ok(
                new ApiDataResponse<>(
                        true,
                        "Password changed successfully",
                        "Password has been updated",
                        null,
                        HttpStatus.OK
                )
        );
    }

    // ==================== FR-10: Quên mật khẩu ====================
    /**
     * POST /api/v1/auth/forgot-password
     * Gửi email chứa reset link (không cần đăng nhập)
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiDataResponse<String>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {

        userService.forgotPassword(request);

        return ResponseEntity.ok(
                new ApiDataResponse<>(
                        true,
                        "Reset password email sent",
                        "Please check your email for the reset link",
                        null,
                        HttpStatus.OK
                )
        );
    }

    // ==================== FR-10: Reset mật khẩu ====================
    /**
     * POST /api/v1/auth/reset-password
     * Dùng reset token từ email để đặt lại mật khẩu
     */
    @PostMapping("/reset-password")
    public ResponseEntity<ApiDataResponse<String>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        userService.resetPassword(request);

        return ResponseEntity.ok(
                new ApiDataResponse<>(
                        true,
                        "Password reset successfully",
                        "You can now login with your new password",
                        null,
                        HttpStatus.OK
                )
        );
    }
}