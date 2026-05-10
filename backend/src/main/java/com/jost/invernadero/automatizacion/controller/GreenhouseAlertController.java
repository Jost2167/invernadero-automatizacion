package com.jost.invernadero.automatizacion.controller;

import com.jost.invernadero.automatizacion.dto.GreenhouseAlertDto;
import com.jost.invernadero.automatizacion.entity.GreenhouseAlert;

import com.jost.invernadero.automatizacion.entity.GreenhouseAlertSeverity;



import com.jost.invernadero.automatizacion.entity.Greenhouse;



import com.jost.invernadero.automatizacion.entity.Sensor;


import com.jost.invernadero.automatizacion.service.GreenhouseAlertService;

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
@RequestMapping("/api/greenhouse-alert")
@RequiredArgsConstructor
public class GreenhouseAlertController {

    private final GreenhouseAlertService greenhouseAlertService;

    @GetMapping
    public ResponseEntity<List<GreenhouseAlertDto>> findAll() {
        return ResponseEntity.ok(greenhouseAlertService.findAll().stream()
                .map(this::toDto)
                .toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<GreenhouseAlertDto> findById(@PathVariable Long id) {
        return greenhouseAlertService.findById(id)
                .map(this::toDto)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "greenhouse-alert.not.found"));
    }

    @PostMapping
    public ResponseEntity<GreenhouseAlertDto> create(@RequestBody GreenhouseAlertDto dto) {
        GreenhouseAlert saved = greenhouseAlertService.save(toEntity(dto));
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GreenhouseAlertDto> update(@PathVariable Long id, @RequestBody GreenhouseAlertDto dto) {
        GreenhouseAlert entity = toEntity(dto);
        entity.setId(id);
        return ResponseEntity.ok(toDto(greenhouseAlertService.save(entity)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        greenhouseAlertService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private GreenhouseAlertDto toDto(GreenhouseAlert entity) {
        return new GreenhouseAlertDto(

                entity.getId(),

                entity.getTitle(),

                entity.getSeverity(),

                entity.getMessage(),

                entity.getDetectedAt(),

                entity.getResolved(),

                entity.getGreenhouse() == null ? null : entity.getGreenhouse().getId(),

                entity.getSensor() == null ? null : entity.getSensor().getId()

        );
    }

    private GreenhouseAlert toEntity(GreenhouseAlertDto dto) {
        GreenhouseAlert entity = new GreenhouseAlert();




        entity.setTitle(dto.title());



        entity.setSeverity(dto.severity());



        entity.setMessage(dto.message());



        entity.setDetectedAt(dto.detectedAt());



        entity.setResolved(dto.resolved());




        if (dto.greenhouseId() != null) {
            Greenhouse greenhouse = new Greenhouse();
            greenhouse.setId(dto.greenhouseId());
            entity.setGreenhouse(greenhouse);
        }



        if (dto.sensorId() != null) {
            Sensor sensor = new Sensor();
            sensor.setId(dto.sensorId());
            entity.setSensor(sensor);
        }


        return entity;
    }
}
