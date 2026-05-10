package com.jost.invernadero.automatizacion.dto;


import com.jost.invernadero.automatizacion.entity.GreenhouseStatus;

import java.math.BigDecimal;

public record GreenhouseDto(

        Long id,

        String code,

        String name,

        BigDecimal areaSquareMeters,

        GreenhouseStatus status,

        Boolean active,

        Long locationId
) {
}
