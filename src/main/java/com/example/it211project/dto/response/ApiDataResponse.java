package com.example.it211project.dto.response;

import lombok.*;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ApiDataResponse<T> {

    private Boolean success;

    private String message;

    private T data;

    private T errors;

    private HttpStatus httpStatus;
}