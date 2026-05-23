package com.jost.invernadero.automatizacion.dto;

public record JwtTokenResponse(String tokenType, String token, String authorizationHeader) {
}
