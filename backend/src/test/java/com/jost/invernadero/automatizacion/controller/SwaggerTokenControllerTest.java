package com.jost.invernadero.automatizacion.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jost.invernadero.automatizacion.entity.Role;
import com.jost.invernadero.automatizacion.entity.User;
import com.jost.invernadero.automatizacion.repository.RoleRepository;
import com.jost.invernadero.automatizacion.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SwaggerTokenControllerTest {

    private static final String EMAIL = "swagger@example.com";

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;

    @BeforeEach
    void seedUser() {
        Role role = roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_USER").build()));
        userRepository.findByEmail(EMAIL)
                .orElseGet(() -> userRepository.save(User.builder()
                        .email(EMAIL)
                        .name("Swagger User")
                        .active(true)
                        .roles(new HashSet<>(Set.of(role)))
                        .build()));
    }

    @Test
    void swaggerToken_withoutAuthentication_returnsJwtThatCanAccessProtectedEndpoint() throws Exception {
        MvcResult result = mockMvc.perform(get("/auth/swagger-token").param("email", EMAIL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.authorizationHeader").isNotEmpty())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        String token = json.get("token").asText();

        assertThat(token).isNotBlank();

        mockMvc.perform(get("/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(EMAIL))
                .andExpect(jsonPath("$.name").value("Swagger User"));
    }

    @Test
    void swaggerToken_unknownUser_returns404() throws Exception {
        mockMvc.perform(get("/auth/swagger-token").param("email", "missing@example.com"))
                .andExpect(status().isNotFound());
    }
}
