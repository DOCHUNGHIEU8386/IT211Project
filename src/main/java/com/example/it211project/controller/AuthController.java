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
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JWTProvider jwtProvider;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;

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

    @PostMapping("/logout")
    public ResponseEntity<ApiDataResponse<String>> logout(
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow();

        refreshTokenService.revokeAllTokensForUser(user);

        log.info("User {} logged out", user.getUsername());

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