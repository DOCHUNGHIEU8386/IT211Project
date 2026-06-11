package com.example.it211project.service.impl;

import com.example.it211project.dto.request.RegisterRequest;
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

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

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

    // ==================== FR-05: Get all users (paginated) ====================
    @Override
    public Page<UserResponse> getAllUsers(int page, int size) {
        return userRepository.findAll(PageRequest.of(page, size))
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