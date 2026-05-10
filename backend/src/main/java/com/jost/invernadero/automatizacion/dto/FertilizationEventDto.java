package com.jost.invernadero.automatizacion.dto;


import com.jost.invernadero.automatizacion.entity.FertilizationEventUnit;

import java.math.BigDecimal;

import java.time.LocalDateTime;

public record FertilizationEventDto(

        Long id,

        LocalDateTime appliedAt,

        String fertilizerName,

        BigDecimal dose,

        FertilizationEventUnit unit,

        String notes,

        Long cropCycleId
) {
}
