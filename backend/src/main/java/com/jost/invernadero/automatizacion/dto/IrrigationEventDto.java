package com.jost.invernadero.automatizacion.dto;


import com.jost.invernadero.automatizacion.entity.IrrigationEventMethod;

import java.math.BigDecimal;

import java.time.LocalDateTime;

public record IrrigationEventDto(

        Long id,

        LocalDateTime startedAt,

        LocalDateTime endedAt,

        BigDecimal waterLiters,

        IrrigationEventMethod method,

        String notes,

        Long greenhouseId
) {
}
