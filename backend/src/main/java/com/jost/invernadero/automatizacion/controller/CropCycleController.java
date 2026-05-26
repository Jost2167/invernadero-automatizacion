package com.jost.invernadero.automatizacion.controller;

import com.jost.invernadero.automatizacion.dto.CropCycleDto;
import com.jost.invernadero.automatizacion.entity.CropCycle;

import com.jost.invernadero.automatizacion.entity.CropCycleStatus;



import com.jost.invernadero.automatizacion.entity.Greenhouse;


import com.jost.invernadero.automatizacion.service.CropCycleService;

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
@RequestMapping("/api/crop-cycle")
@RequiredArgsConstructor
public class CropCycleController {

    private final CropCycleService cropCycleService;

    @GetMapping
    public ResponseEntity<List<CropCycleDto>> findAll() {
        return ResponseEntity.ok(cropCycleService.findAll().stream()
                .map(this::toDto)
                .toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CropCycleDto> findById(@PathVariable Long id) {
        return cropCycleService.findById(id)
                .map(this::toDto)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "crop-cycle.not.found"));
    }

    @PostMapping
    public ResponseEntity<CropCycleDto> create(@RequestBody CropCycleDto dto) {
        CropCycle saved = cropCycleService.save(toEntity(dto));
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CropCycleDto> update(@PathVariable Long id, @RequestBody CropCycleDto dto) {
        CropCycle entity = toEntity(dto);
        entity.setId(id);
        return ResponseEntity.ok(toDto(cropCycleService.save(entity)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        cropCycleService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private CropCycleDto toDto(CropCycle entity) {
        return new CropCycleDto(

                entity.getId(),

                entity.getCropName(),

                entity.getVariety(),

                entity.getStartedAt(),

                entity.getExpectedHarvestAt(),

                entity.getStatus(),

                entity.getGreenhouse() == null ? null : entity.getGreenhouse().getId()

        );
    }

    private CropCycle toEntity(CropCycleDto dto) {
        CropCycle entity = new CropCycle();




        entity.setCropName(dto.cropName());



        entity.setVariety(dto.variety());



        entity.setStartedAt(dto.startedAt());



        entity.setExpectedHarvestAt(dto.expectedHarvestAt());



        entity.setStatus(dto.status());




        if (dto.greenhouseId() != null) {
            Greenhouse greenhouse = new Greenhouse();
            greenhouse.setId(dto.greenhouseId());
            entity.setGreenhouse(greenhouse);
        }


        return entity;
    }
}
