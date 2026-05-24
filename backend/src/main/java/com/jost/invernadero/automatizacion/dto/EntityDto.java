package com.jost.invernadero.automatizacion.dto;

import java.util.List;

public record EntityDto(String name, String displayName, List<FieldDto> fields) {}
