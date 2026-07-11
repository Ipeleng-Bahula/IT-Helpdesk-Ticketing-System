package com.helpdesk.config;

import com.helpdesk.model.Role;
import com.helpdesk.model.User;
import com.helpdesk.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Default login: admin / Admin123!
 * Change the password immediately after first login in a real deployment.
 */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {
 
    private static final String DEFAULT_ADMIN_PASSWORD = "Admin123!";
 
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
 
    @Override
    public void run(String... args) {
        userRepository.findByUsername("admin").ifPresentOrElse(
            existing -> {
                if (existing.getRole() != Role.ADMIN) {
                    existing.setRole(Role.ADMIN);
                    existing.setPassword(passwordEncoder.encode(DEFAULT_ADMIN_PASSWORD));
                    userRepository.save(existing);
                    System.out.println("=================================================");
                    System.out.println(" Existing 'admin' user was not ADMIN role — fixed.");
                    System.out.println("   login email: " + existing.getEmail());
                    System.out.println("   password: " + DEFAULT_ADMIN_PASSWORD + " (reset)");
                    System.out.println("=================================================");
                }
                // Already a healthy admin account — leave it untouched,
                // including whatever password is currently set.
            },
            () -> {
                User admin = User.builder()
                        .username("admin")
                        .email("admin@helpdesk.local")
                        .password(passwordEncoder.encode(DEFAULT_ADMIN_PASSWORD))
                        .role(Role.ADMIN)
                        .build();
                userRepository.save(admin);
                System.out.println("=================================================");
                System.out.println(" No admin account found — created default admin:");
                System.out.println("   login email: admin@helpdesk.local");
                System.out.println("   password: " + DEFAULT_ADMIN_PASSWORD);
                System.out.println(" Please log in and change this password.");
                System.out.println("=================================================");
            }
        );
    }
}