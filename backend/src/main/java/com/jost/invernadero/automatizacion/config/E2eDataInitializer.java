package com.jost.invernadero.automatizacion.config;

import com.jost.invernadero.automatizacion.entity.Role;
import com.jost.invernadero.automatizacion.entity.User;
import com.jost.invernadero.automatizacion.repository.RoleRepository;
import com.jost.invernadero.automatizacion.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Configuration
@Profile("e2e")
public class E2eDataInitializer {

    @Bean
    @Transactional
    CommandLineRunner seedE2eUser(
            RoleRepository roleRepository,
            UserRepository userRepository,
            @Value("${app.e2e.user-email:e2e@example.com}") String userEmail) {
        return args -> {
            Role role = roleRepository.findByName("ROLE_USER")
                    .orElseGet(() -> roleRepository.save(Role.builder()
                            .name("ROLE_USER")
                            .build()));

            userRepository.findByEmail(userEmail)
                    .orElseGet(() -> userRepository.save(User.builder()
                            .email(userEmail)
                            .name("E2E User")
                            .active(true)
                            .roles(Set.of(role))
                            .build()));
        };
    }
}
