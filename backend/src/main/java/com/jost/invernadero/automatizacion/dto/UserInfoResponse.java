package com.jost.invernadero.automatizacion.dto;

import java.util.Set;

public record UserInfoResponse(String email, String name, Set<String> roles) {
}
