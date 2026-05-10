package com.jost.invernadero.automatizacion.dto;


import com.jost.invernadero.automatizacion.entity.SensorType;

import java.math.BigDecimal;

import java.time.LocalDateTime;

public record SensorDto(

        Long id,

        String name,

        SensorType type,

        LocalDateTime lastReadingAt,

        BigDecimal batteryLevel,

        Boolean active,

        Long locationId
) {
}
