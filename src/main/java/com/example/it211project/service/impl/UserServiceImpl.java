package com.example.it211project.service.impl;

import com.example.it211project.dto.request.ChangePasswordRequest;
import com.example.it211project.dto.request.ForgotPasswordRequest;
import com.example.it211project.dto.request.RegisterRequest;
import com.example.it211project.dto.request.ResetPasswordRequest;
import com.example.it211project.dto.request.UserUpdateRequest;
import com.example.it211project.dto.response.UserResponse;
import com.example.it211project.entity.Role;
import com.example.it211project.entity.User;
import com.example.it211project.exception.DuplicateResourceException;
import com.example.it211project.exception.ResourceNotFoundException;
import com.example.it211project.repository.RoleRepository;
import com.example.it211project.repository.UserRepository;
import com.example.it211project.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * In-memory store cho reset password tokens.
     * Production nên dùng Redis hoặc DB table.
     * Map<token, username>
     */
    private final Map<String, String> resetTokenStore = new ConcurrentHashMap<>();

    // ==================== FR-04: Register ====================
    @Override
    public UserResponse register(RegisterRequest request) {

        // Validate duplicate username
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException(
                    "Username '" + request.getUsername() + "' already exists");
        }

        // Validate duplicate email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException(
                    "Email '" + request.getEmail() + "' already exists");
        }

        // Auto-assign ROLE_STUDENT (create if not exists)
        Role studentRole = roleRepository.findByRoleName("ROLE_STUDENT")
                .orElseGet(() -> roleRepository.save(
                        Role.builder().roleName("ROLE_STUDENT").build()));

        // Encode password with BCrypt
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .email(request.getEmail())
                .enabled(true)
                .roles(List.of(studentRole))
                .build();

        User saved = userRepository.save(user);
        return mapToResponse(saved);
    }

    // ==================== FR-05: Get all users (paginated + search) ====================
    @Override
    public Page<UserResponse> getAllUsers(int page, int size, String keyword) {
        PageRequest pageable = PageRequest.of(page, size);
        if (keyword != null && !keyword.isBlank()) {
            return userRepository.searchUsers(keyword, pageable)
                    .map(this::mapToResponse);
        }
        return userRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    // ==================== FR-05: Get user by ID ====================
    @Override
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found with id: " + id));
        return mapToResponse(user);
    }

    // ==================== FR-05: Update user ====================
    @Override
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found with id: " + id));

        // Validate email uniqueness (nếu đổi email)
        if (request.getEmail() != null && !request.getEmail().equalsIgnoreCase(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new DuplicateResourceException(
                        "Email '" + request.getEmail() + "' is already in use");
            }
        }

        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        if (request.getEnabled() != null) {
            user.setEnabled(request.getEnabled());
        }

        return mapToResponse(userRepository.save(user));
    }

    // ==================== FR-05: Delete user ====================
    @Override
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found with id: " + id));
        userRepository.delete(user);
    }

    // ==================== FR-10: Đổi mật khẩu ====================
    @Override
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found with id: " + userId));

        // Kiểm tra mật khẩu cũ
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Old password is incorrect");
        }

        // Kiểm tra xác nhận mật khẩu mới
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("New password and confirm password do not match");
        }

        // Không cho đặt lại mật khẩu cũ
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new IllegalArgumentException("New password must be different from old password");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    // ==================== FR-10: Quên mật khẩu (gửi token) ====================
    @Override
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No account found with email: " + request.getEmail()));

        // Tạo reset token
        String resetToken = UUID.randomUUID().toString();
        resetTokenStore.put(resetToken, user.getUsername());

        // TODO: Tích hợp email service để gửi link reset:
        // emailService.sendResetPasswordEmail(user.getEmail(), resetToken);

        // Tạm thời log ra console (production dùng mail)
        System.out.println("=== RESET PASSWORD TOKEN (dev only) ===");
        System.out.println("Email: " + user.getEmail());
        System.out.println("Token: " + resetToken);
        System.out.println("Reset URL: http://localhost:8080/api/v1/auth/reset-password");
        System.out.println("========================================");
    }

    // ==================== FR-10: Reset mật khẩu ====================
    @Override
    public void resetPassword(ResetPasswordRequest request) {
        String username = resetTokenStore.get(request.getResetToken());
        if (username == null) {
            throw new IllegalArgumentException("Invalid or expired reset token");
        }

        // Kiểm tra xác nhận mật khẩu
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("New password and confirm password do not match");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found: " + username));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Xóa token sau khi dùng (one-time use)
        resetTokenStore.remove(request.getResetToken());
    }

    // ==================== Helper: Entity -> DTO ====================
    private UserResponse mapToResponse(User user) {
        String role = user.getRoles().stream()
                .map(Role::getRoleName)
                .findFirst()
                .orElse("ROLE_STUDENT");

        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .enabled(user.getEnabled())
                .role(role)
                .build();
    }
}