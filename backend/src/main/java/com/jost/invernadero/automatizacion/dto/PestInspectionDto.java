package com.jost.invernadero.automatizacion.dto;


import com.jost.invernadero.automatizacion.entity.PestInspectionSeverity;

import java.math.BigDecimal;

import java.time.LocalDateTime;

public record PestInspectionDto(

        Long id,

        LocalDateTime inspectedAt,

        String pestType,

        PestInspectionSeverity severity,

        BigDecimal affectedAreaSquareMeters,

        Boolean treatmentApplied,

        Long cropCycleId
) {
}
