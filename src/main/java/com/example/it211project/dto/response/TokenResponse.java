package com.example.it211project.dto.response;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class TokenResponse {

    /** JWT access token */
    private String accessToken;

    /** Refresh token string */
    private String refreshToken;

    @Builder.Default
    private String tokenType = "Bearer";
}
