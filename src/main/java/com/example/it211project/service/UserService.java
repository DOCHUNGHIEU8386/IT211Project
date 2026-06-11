package com.example.it211project.service;

import com.example.it211project.dto.request.RegisterRequest;
import com.example.it211project.dto.request.UserUpdateRequest;
import com.example.it211project.dto.response.UserResponse;
import org.springframework.data.domain.Page;

public interface UserService {

    UserResponse register(RegisterRequest request);

    Page<UserResponse> getAllUsers(int page, int size);

    UserResponse getUserById(Long id);

    UserResponse updateUser(Long id, UserUpdateRequest request);

    void deleteUser(Long id);
}