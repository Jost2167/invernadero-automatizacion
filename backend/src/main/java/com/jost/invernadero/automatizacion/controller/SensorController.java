package com.jost.invernadero.automatizacion.controller;

import com.jost.invernadero.automatizacion.dto.SensorDto;
import com.jost.invernadero.automatizacion.entity.Sensor;

import com.jost.invernadero.automatizacion.entity.SensorType;



import com.jost.invernadero.automatizacion.entity.Location;


import com.jost.invernadero.automatizacion.service.SensorService;

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
@RequestMapping("/api/sensor")
@RequiredArgsConstructor
public class SensorController {

    private final SensorService sensorService;

    @GetMapping
    public ResponseEntity<List<SensorDto>> findAll() {
        return ResponseEntity.ok(sensorService.findAll().stream()
                .map(this::toDto)
                .toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SensorDto> findById(@PathVariable Long id) {
        return sensorService.findById(id)
                .map(this::toDto)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "sensor.not.found"));
    }

    @PostMapping
    public ResponseEntity<SensorDto> create(@RequestBody SensorDto dto) {
        Sensor saved = sensorService.save(toEntity(dto));
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SensorDto> update(@PathVariable Long id, @RequestBody SensorDto dto) {
        Sensor entity = toEntity(dto);
        entity.setId(id);
        return ResponseEntity.ok(toDto(sensorService.save(entity)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        sensorService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private SensorDto toDto(Sensor entity) {
        return new SensorDto(

                entity.getId(),

                entity.getName(),

                entity.getType(),

                entity.getLastReadingAt(),

                entity.getBatteryLevel(),

                entity.getActive(),

                entity.getLocation() == null ? null : entity.getLocation().getId()

        );
    }

    private Sensor toEntity(SensorDto dto) {
        Sensor entity = new Sensor();




        entity.setName(dto.name());



        entity.setType(dto.type());



        entity.setLastReadingAt(dto.lastReadingAt());



        entity.setBatteryLevel(dto.batteryLevel());



        entity.setActive(dto.active());




        if (dto.locationId() != null) {
            Location location = new Location();
            location.setId(dto.locationId());
            entity.setLocation(location);
        }


        return entity;
    }
}
