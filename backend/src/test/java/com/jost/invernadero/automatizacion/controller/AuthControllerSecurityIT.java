package com.jost.invernadero.automatizacion.controller;

import com.jost.invernadero.automatizacion.entity.Role;
import com.jost.invernadero.automatizacion.entity.User;
import com.jost.invernadero.automatizacion.repository.RoleRepository;
import com.jost.invernadero.automatizacion.repository.UserRepository;
import com.jost.invernadero.automatizacion.service.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerSecurityIT {

    private static final String EMAIL = "test@example.com";

    @Autowired private MockMvc mockMvc;
    @Autowired private JwtTokenProvider tokenProvider;
    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;

    @BeforeEach
    void seedUser() {
        Role role = roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_USER").build()));
        if (userRepository.findByEmail(EMAIL).isEmpty()) {
            userRepository.save(User.builder()
                    .email(EMAIL)
                    .name("Test User")
                    .active(true)
                    .roles(new HashSet<>(Set.of(role)))
                    .build());
        }
    }

    @Test
    void getAuthMe_without_token_returns401() throws Exception {
        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getAuthMe_with_invalid_token_returns401() throws Exception {
        mockMvc.perform(get("/auth/me").header("Authorization", "Bearer not-a-valid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getAuthMe_without_token_acceptLanguageEs_returnsSpanishMessage() throws Exception {
        mockMvc.perform(get("/auth/me").header("Accept-Language", "es"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("No autorizado"));
    }

    @Test
    void getAuthMe_without_token_acceptLanguageEn_returnsEnglishMessage() throws Exception {
        mockMvc.perform(get("/auth/me").header("Accept-Language", "en"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("Unauthorized"));
    }

    @Test
    void getAuthMe_with_valid_token_returns200AndUserInfo() throws Exception {
        UserDetails details = org.springframework.security.core.userdetails.User
                .withUsername(EMAIL)
                .password("")
                .authorities("ROLE_USER")
                .build();
        String token = tokenProvider.generateToken(details);

        mockMvc.perform(get("/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(EMAIL))
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"));
    }

    @Test
    void postAuthLogout_without_token_returns401() throws Exception {
        mockMvc.perform(post("/auth/logout"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void postAuthLogout_with_valid_token_returns200() throws Exception {
        UserDetails details = org.springframework.security.core.userdetails.User
                .withUsername(EMAIL)
                .password("")
                .authorities("ROLE_USER")
                .build();
        String token = tokenProvider.generateToken(details);

        mockMvc.perform(post("/auth/logout").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());
    }
}
