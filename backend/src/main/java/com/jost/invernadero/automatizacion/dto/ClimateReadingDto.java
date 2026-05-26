package com.jost.invernadero.automatizacion.dto;


import java.math.BigDecimal;

import java.time.LocalDateTime;

public record ClimateReadingDto(

        Long id,

        LocalDateTime recordedAt,

        BigDecimal temperatureCelsius,

        BigDecimal humidityPercent,

        Integer co2Ppm,

        Integer lightLux,

        Long sensorId,

        Long greenhouseId
) {
}
