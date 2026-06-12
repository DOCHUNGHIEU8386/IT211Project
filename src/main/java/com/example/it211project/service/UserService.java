package com.example.it211project.service;

import com.example.it211project.dto.request.ChangePasswordRequest;
import com.example.it211project.dto.request.ForgotPasswordRequest;
import com.example.it211project.dto.request.RegisterRequest;
import com.example.it211project.dto.request.ResetPasswordRequest;
import com.example.it211project.dto.request.UserUpdateRequest;
import com.example.it211project.dto.response.UserResponse;
import org.springframework.data.domain.Page;

public interface UserService {

    UserResponse register(RegisterRequest request);

    Page<UserResponse> getAllUsers(int page, int size, String keyword);

    UserResponse getUserById(Long id);

    UserResponse updateUser(Long id, UserUpdateRequest request);

    void deleteUser(Long id);

    /** FR-10: Đổi mật khẩu (khi đã đăng nhập) */
    void changePassword(Long userId, ChangePasswordRequest request);

    /** FR-10: Gửi email reset password (quên mật khẩu) */
    void forgotPassword(ForgotPasswordRequest request);

    /** FR-10: Đặt lại mật khẩu bằng reset token */
    void resetPassword(ResetPasswordRequest request);
}