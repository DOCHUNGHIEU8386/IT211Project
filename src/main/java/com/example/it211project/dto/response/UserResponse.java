package com.example.it211project.dto.response;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UserResponse {

    private Long id;
    private String username;
    private String fullName;
    private String email;
    private Boolean enabled;
    private String role;
}
