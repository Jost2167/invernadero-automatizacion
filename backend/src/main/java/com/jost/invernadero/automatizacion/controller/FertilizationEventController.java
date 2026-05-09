package com.jost.invernadero.automatizacion.controller;

import com.jost.invernadero.automatizacion.dto.FertilizationEventDto;
import com.jost.invernadero.automatizacion.entity.FertilizationEvent;

import com.jost.invernadero.automatizacion.entity.FertilizationEventUnit;



import com.jost.invernadero.automatizacion.entity.CropCycle;


import com.jost.invernadero.automatizacion.service.FertilizationEventService;

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
@RequestMapping("/api/fertilization-event")
@RequiredArgsConstructor
public class FertilizationEventController {

    private final FertilizationEventService fertilizationEventService;

    @GetMapping
    public ResponseEntity<List<FertilizationEventDto>> findAll() {
        return ResponseEntity.ok(fertilizationEventService.findAll().stream()
                .map(this::toDto)
                .toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FertilizationEventDto> findById(@PathVariable Long id) {
        return fertilizationEventService.findById(id)
                .map(this::toDto)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "fertilization-event.not.found"));
    }

    @PostMapping
    public ResponseEntity<FertilizationEventDto> create(@RequestBody FertilizationEventDto dto) {
        FertilizationEvent saved = fertilizationEventService.save(toEntity(dto));
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FertilizationEventDto> update(@PathVariable Long id, @RequestBody FertilizationEventDto dto) {
        FertilizationEvent entity = toEntity(dto);
        entity.setId(id);
        return ResponseEntity.ok(toDto(fertilizationEventService.save(entity)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        fertilizationEventService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private FertilizationEventDto toDto(FertilizationEvent entity) {
        return new FertilizationEventDto(

                entity.getId(),

                entity.getAppliedAt(),

                entity.getFertilizerName(),

                entity.getDose(),

                entity.getUnit(),

                entity.getNotes(),

                entity.getCropCycle() == null ? null : entity.getCropCycle().getId()

        );
    }

    private FertilizationEvent toEntity(FertilizationEventDto dto) {
        FertilizationEvent entity = new FertilizationEvent();




        entity.setAppliedAt(dto.appliedAt());



        entity.setFertilizerName(dto.fertilizerName());



        entity.setDose(dto.dose());



        entity.setUnit(dto.unit());



        entity.setNotes(dto.notes());




        if (dto.cropCycleId() != null) {
            CropCycle cropCycle = new CropCycle();
            cropCycle.setId(dto.cropCycleId());
            entity.setCropCycle(cropCycle);
        }


        return entity;
    }
}
