package com.jost.invernadero.automatizacion.controller;

import com.jost.invernadero.automatizacion.dto.MaintenanceTaskDto;
import com.jost.invernadero.automatizacion.entity.MaintenanceTask;

import com.jost.invernadero.automatizacion.entity.MaintenanceTaskStatus;



import com.jost.invernadero.automatizacion.entity.Greenhouse;


import com.jost.invernadero.automatizacion.service.MaintenanceTaskService;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/maintenance-task")
@RequiredArgsConstructor
public class MaintenanceTaskController {

    private final MaintenanceTaskService maintenanceTaskService;

    @GetMapping
    public ResponseEntity<List<MaintenanceTaskDto>> findAll() {
        return ResponseEntity.ok(maintenanceTaskService.findAll().stream()
                .map(this::toDto)
                .toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MaintenanceTaskDto> findById(@PathVariable Long id) {
        return maintenanceTaskService.findById(id)
                .map(this::toDto)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "maintenance-task.not.found"));
    }

    @PostMapping
    public ResponseEntity<MaintenanceTaskDto> create(@RequestBody MaintenanceTaskDto dto) {
        MaintenanceTask saved = maintenanceTaskService.save(toEntity(dto));
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MaintenanceTaskDto> update(@PathVariable Long id, @RequestBody MaintenanceTaskDto dto) {
        MaintenanceTask entity = toEntity(dto);
        entity.setId(id);
        return ResponseEntity.ok(toDto(maintenanceTaskService.save(entity)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        maintenanceTaskService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private MaintenanceTaskDto toDto(MaintenanceTask entity) {
        return new MaintenanceTaskDto(

                entity.getId(),

                entity.getTitle(),

                entity.getDescription(),

                entity.getScheduledAt(),

                entity.getCompletedAt(),

                entity.getStatus(),

                entity.getGreenhouse() == null ? null : entity.getGreenhouse().getId()

        );
    }

    private MaintenanceTask toEntity(MaintenanceTaskDto dto) {
        MaintenanceTask entity = new MaintenanceTask();




        entity.setTitle(dto.title());



        entity.setDescription(dto.description());



        entity.setScheduledAt(dto.scheduledAt());



        entity.setCompletedAt(dto.completedAt());



        entity.setStatus(dto.status());




        if (dto.greenhouseId() != null) {
            Greenhouse greenhouse = new Greenhouse();
            greenhouse.setId(dto.greenhouseId());
            entity.setGreenhouse(greenhouse);
        }


        return entity;
    }
}
