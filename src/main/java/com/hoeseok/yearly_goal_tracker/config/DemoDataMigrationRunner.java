package com.hoeseok.yearly_goal_tracker.config;

import com.hoeseok.yearly_goal_tracker.domain.User;
import com.hoeseok.yearly_goal_tracker.domain.enums.UserRole;
import com.hoeseok.yearly_goal_tracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DemoDataMigrationRunner implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        List<User> users = userRepository.findAll();
        for (User user : users) {
            boolean updated = false;
            if (user.getPassword() == null || !user.getPassword().startsWith("$2a$")) {
                user.updatePassword(passwordEncoder.encode("password123!"));
                updated = true;
            }
            if (user.getRole() == null) {
                user.updateRole(UserRole.ROLE_USER);
                updated = true;
            }
            if (updated) {
                userRepository.save(user);
                log.info("Migrated password/role for user: {}", user.getEmail());
            }
        }
    }
}
