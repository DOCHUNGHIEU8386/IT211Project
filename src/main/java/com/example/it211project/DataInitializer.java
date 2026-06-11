package com.example.it211project;

import com.example.it211project.entity.Role;
import com.example.it211project.entity.User;
import com.example.it211project.repository.RoleRepository;
import com.example.it211project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Khởi tạo dữ liệu mặc định khi ứng dụng khởi động:
 * - Tạo ROLE_ADMIN, ROLE_STUDENT, ROLE_LECTURER nếu chưa có
 * - Tạo tài khoản admin mặc định nếu chưa có
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        // Tạo roles nếu chưa có
        Role adminRole = createRoleIfNotExists("ROLE_ADMIN");
        createRoleIfNotExists("ROLE_STUDENT");
        createRoleIfNotExists("ROLE_LECTURER");

        // Tạo admin mặc định nếu chưa có
        if (!userRepository.existsByUsername("admin")) {
            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .fullName("System Administrator")
                    .email("admin@it211.com")
                    .enabled(true)
                    .roles(List.of(adminRole))
                    .build();
            userRepository.save(admin);
            log.info(">>> Default admin account created: admin / admin123");
        }

        log.info(">>> DataInitializer completed.");
    }

    private Role createRoleIfNotExists(String roleName) {
        return roleRepository.findByRoleName(roleName)
                .orElseGet(() -> {
                    Role role = roleRepository.save(
                            Role.builder().roleName(roleName).build());
                    log.info(">>> Created role: {}", roleName);
                    return role;
                });
    }
}
