package com.jost.invernadero.automatizacion.dto;


import com.jost.invernadero.automatizacion.entity.GreenhouseAlertSeverity;

import java.time.LocalDateTime;

public record GreenhouseAlertDto(

        Long id,

        String title,

        GreenhouseAlertSeverity severity,

        String message,

        LocalDateTime detectedAt,

        Boolean resolved,

        Long greenhouseId,

        Long sensorId
) {
}
