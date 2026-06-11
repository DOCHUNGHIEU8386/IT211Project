package com.example.it211project.controller;

import com.example.it211project.dto.request.LoginRequest;
import com.example.it211project.dto.request.RegisterRequest;
import com.example.it211project.dto.response.ApiDataResponse;
import com.example.it211project.dto.response.LoginResponse;
import com.example.it211project.dto.response.UserResponse;
import com.example.it211project.security.jwt.JWTProvider;
import com.example.it211project.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JWTProvider jwtProvider;

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
    public ResponseEntity<ApiDataResponse<LoginResponse>> login(
            @RequestBody LoginRequest request) {

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.getUsername(),
                                request.getPassword()
                        )
                );

        String token = jwtProvider.generateToken(authentication);

        return ResponseEntity.ok(
                new ApiDataResponse<>(
                        true,
                        "Login successful",
                        new LoginResponse(token),
                        null,
                        HttpStatus.OK
                )
        );
    }
}