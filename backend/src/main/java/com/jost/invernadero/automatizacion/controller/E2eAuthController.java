package com.jost.invernadero.automatizacion.controller;

import com.jost.invernadero.automatizacion.service.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Profile("e2e")
@RequiredArgsConstructor
public class E2eAuthController {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;

    @GetMapping("/e2e/token")
    public ResponseEntity<Map<String, String>> token(@RequestParam(defaultValue = "e2e@example.com") String email) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        return ResponseEntity.ok(Map.of("token", jwtTokenProvider.generateToken(userDetails)));
    }
}
