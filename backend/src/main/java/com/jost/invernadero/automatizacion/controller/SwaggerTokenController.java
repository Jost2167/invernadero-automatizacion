package com.jost.invernadero.automatizacion.controller;

import com.jost.invernadero.automatizacion.dto.JwtTokenResponse;
import com.jost.invernadero.automatizacion.service.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@Profile("!prod")
@RequiredArgsConstructor
@Tag(name = "Swagger Auth")
public class SwaggerTokenController {

    private static final String BEARER = "Bearer";

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;

    @GetMapping("/auth/swagger-token")
    @SecurityRequirements
    @Operation(
            summary = "Genera un JWT para pruebas desde Swagger",
            description = "Disponible solo fuera del perfil prod. Usa el token devuelto en el boton Authorize.")
    public ResponseEntity<JwtTokenResponse> token(
            @Parameter(description = "Email de un usuario activo registrado", example = "e2e@example.com")
            @RequestParam String email) {
        if (!StringUtils.hasText(email)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "email.required");
        }

        UserDetails userDetails = loadUser(email);
        if (!userDetails.isEnabled()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "user.inactive");
        }

        String token = jwtTokenProvider.generateToken(userDetails);
        return ResponseEntity.ok(new JwtTokenResponse(BEARER, token, BEARER + " " + token));
    }

    private UserDetails loadUser(String email) {
        try {
            return userDetailsService.loadUserByUsername(email);
        } catch (UsernameNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "user.not.found", ex);
        }
    }
}
