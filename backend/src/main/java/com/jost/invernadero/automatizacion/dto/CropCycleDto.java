package com.jost.invernadero.automatizacion.dto;


import com.jost.invernadero.automatizacion.entity.CropCycleStatus;

import java.time.LocalDate;

public record CropCycleDto(

        Long id,

        String cropName,

        String variety,

        LocalDate startedAt,

        LocalDate expectedHarvestAt,

        CropCycleStatus status,

        Long greenhouseId
) {
}
