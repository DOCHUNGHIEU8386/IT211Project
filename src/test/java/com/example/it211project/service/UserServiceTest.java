package com.example.it211project.service;

import com.example.it211project.dto.request.ChangePasswordRequest;
import com.example.it211project.dto.request.RegisterRequest;
import com.example.it211project.dto.request.UserUpdateRequest;
import com.example.it211project.dto.response.UserResponse;
import com.example.it211project.entity.Role;
import com.example.it211project.entity.User;
import com.example.it211project.exception.DuplicateResourceException;
import com.example.it211project.exception.ResourceNotFoundException;
import com.example.it211project.repository.RoleRepository;
import com.example.it211project.repository.UserRepository;
import com.example.it211project.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private RegisterRequest registerRequest;
    private User user;
    private UserResponse userResponse;
    private Role studentRole;

    @BeforeEach
    void setUp() {
        studentRole = Role.builder().id(1L).roleName("ROLE_STUDENT").build();

        registerRequest = RegisterRequest.builder()
                .username("testuser")
                .password("123456")
                .fullName("Test User")
                .email("test@example.com")
                .build();

        user = User.builder()
                .id(1L)
                .username("testuser")
                .password("encodedPassword")
                .fullName("Test User")
                .email("test@example.com")
                .enabled(true)
                .roles(List.of(studentRole))
                .build();

        userResponse = UserResponse.builder()
                .id(1L)
                .username("testuser")
                .fullName("Test User")
                .email("test@example.com")
                .enabled(true)
                .role("ROLE_STUDENT")
                .build();
    }

    // ==================== Test Register - Success ====================
    @Test
    void register_ValidRequest_ReturnsUserResponse() {
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(roleRepository.findByRoleName("ROLE_STUDENT")).thenReturn(Optional.of(studentRole));
        when(passwordEncoder.encode("123456")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponse result = userService.register(registerRequest);

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getFullName()).isEqualTo("Test User");
        assertThat(result.getRole()).isEqualTo("ROLE_STUDENT");

        verify(userRepository).save(any(User.class));
    }

    // ==================== Test Register - Duplicate Username ====================
    @Test
    void register_DuplicateUsername_ThrowsException() {
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        assertThatThrownBy(() -> userService.register(registerRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Username 'testuser' already exists");

        verify(userRepository, never()).save(any(User.class));
    }

    // ==================== Test Register - Duplicate Email ====================
    @Test
    void register_DuplicateEmail_ThrowsException() {
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.register(registerRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Email 'test@example.com' already exists");

        verify(userRepository, never()).save(any(User.class));
    }

    // ==================== Test Get User By ID - Success ====================
    @Test
    void getUserById_ValidId_ReturnsUserResponse() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponse result = userService.getUserById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUsername()).isEqualTo("testuser");

        verify(userRepository).findById(1L);
    }

    // ==================== Test Get User By ID - Not Found ====================
    @Test
    void getUserById_InvalidId_ThrowsException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with id: 999");
    }

    // ==================== Test Get All Users - Paginated ====================
    @Test
    void getAllUsers_ValidRequest_ReturnsPage() {
        PageRequest pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(List.of(user));

        when(userRepository.findAll(pageable)).thenReturn(userPage);

        Page<UserResponse> result = userService.getAllUsers(0, 10, null);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getUsername()).isEqualTo("testuser");
    }

    // ==================== Test Update User - Success ====================
    @Test
    void updateUser_ValidRequest_ReturnsUpdatedUser() {
        UserUpdateRequest updateRequest = UserUpdateRequest.builder()
                .fullName("Updated Name")
                .email("updated@example.com")
                .enabled(true)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("updated@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponse result = userService.updateUser(1L, updateRequest);

        assertThat(result).isNotNull();
        assertThat(result.getFullName()).isEqualTo("Test User"); // Still old name in mock
    }

    // ==================== Test Change Password - Success ====================
    @Test
    void changePassword_ValidRequest_UpdatesPassword() {
        ChangePasswordRequest changeRequest = ChangePasswordRequest.builder()
                .oldPassword("old123")
                .newPassword("new456")
                .confirmPassword("new456")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("old123", "encodedPassword")).thenReturn(true);
        when(passwordEncoder.matches("new456", "encodedPassword")).thenReturn(false);
        when(passwordEncoder.encode("new456")).thenReturn("newEncodedPassword");

        userService.changePassword(1L, changeRequest);

        verify(userRepository).save(any(User.class));
    }

    // ==================== Test Change Password - Wrong Old Password ====================
    @Test
    void changePassword_WrongOldPassword_ThrowsException() {
        ChangePasswordRequest changeRequest = ChangePasswordRequest.builder()
                .oldPassword("wrong")
                .newPassword("new456")
                .confirmPassword("new456")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "encodedPassword")).thenReturn(false);

        assertThatThrownBy(() -> userService.changePassword(1L, changeRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Old password is incorrect");

        verify(userRepository, never()).save(any(User.class));
    }

    // ==================== Test Delete User - Success ====================
    @Test
    void deleteUser_ValidId_DeletesUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        doNothing().when(userRepository).delete(user);

        userService.deleteUser(1L);

        verify(userRepository).delete(user);
    }
}