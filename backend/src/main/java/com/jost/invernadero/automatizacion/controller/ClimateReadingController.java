package com.jost.invernadero.automatizacion.controller;

import com.jost.invernadero.automatizacion.dto.ClimateReadingDto;
import com.jost.invernadero.automatizacion.entity.ClimateReading;



import com.jost.invernadero.automatizacion.entity.Sensor;



import com.jost.invernadero.automatizacion.entity.Greenhouse;


import com.jost.invernadero.automatizacion.service.ClimateReadingService;

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
@RequestMapping("/api/climate-reading")
@RequiredArgsConstructor
public class ClimateReadingController {

    private final ClimateReadingService climateReadingService;

    @GetMapping
    public ResponseEntity<List<ClimateReadingDto>> findAll() {
        return ResponseEntity.ok(climateReadingService.findAll().stream()
                .map(this::toDto)
                .toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClimateReadingDto> findById(@PathVariable Long id) {
        return climateReadingService.findById(id)
                .map(this::toDto)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "climate-reading.not.found"));
    }

    @PostMapping
    public ResponseEntity<ClimateReadingDto> create(@RequestBody ClimateReadingDto dto) {
        ClimateReading saved = climateReadingService.save(toEntity(dto));
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClimateReadingDto> update(@PathVariable Long id, @RequestBody ClimateReadingDto dto) {
        ClimateReading entity = toEntity(dto);
        entity.setId(id);
        return ResponseEntity.ok(toDto(climateReadingService.save(entity)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        climateReadingService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private ClimateReadingDto toDto(ClimateReading entity) {
        return new ClimateReadingDto(

                entity.getId(),

                entity.getRecordedAt(),

                entity.getTemperatureCelsius(),

                entity.getHumidityPercent(),

                entity.getCo2Ppm(),

                entity.getLightLux(),

                entity.getSensor() == null ? null : entity.getSensor().getId(),

                entity.getGreenhouse() == null ? null : entity.getGreenhouse().getId()

        );
    }

    private ClimateReading toEntity(ClimateReadingDto dto) {
        ClimateReading entity = new ClimateReading();




        entity.setRecordedAt(dto.recordedAt());



        entity.setTemperatureCelsius(dto.temperatureCelsius());



        entity.setHumidityPercent(dto.humidityPercent());



        entity.setCo2Ppm(dto.co2Ppm());



        entity.setLightLux(dto.lightLux());




        if (dto.sensorId() != null) {
            Sensor sensor = new Sensor();
            sensor.setId(dto.sensorId());
            entity.setSensor(sensor);
        }



        if (dto.greenhouseId() != null) {
            Greenhouse greenhouse = new Greenhouse();
            greenhouse.setId(dto.greenhouseId());
            entity.setGreenhouse(greenhouse);
        }


        return entity;
    }
}
