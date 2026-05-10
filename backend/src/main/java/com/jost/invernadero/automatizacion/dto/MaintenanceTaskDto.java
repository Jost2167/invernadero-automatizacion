package com.jost.invernadero.automatizacion.dto;


import com.jost.invernadero.automatizacion.entity.MaintenanceTaskStatus;

import java.time.LocalDateTime;

public record MaintenanceTaskDto(

        Long id,

        String title,

        String description,

        LocalDateTime scheduledAt,

        LocalDateTime completedAt,

        MaintenanceTaskStatus status,

        Long greenhouseId
) {
}
