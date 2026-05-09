package com.jost.invernadero.automatizacion.controller;

import com.jost.invernadero.automatizacion.dto.PestInspectionDto;
import com.jost.invernadero.automatizacion.entity.PestInspection;

import com.jost.invernadero.automatizacion.entity.PestInspectionSeverity;



import com.jost.invernadero.automatizacion.entity.CropCycle;


import com.jost.invernadero.automatizacion.service.PestInspectionService;

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
@RequestMapping("/api/pest-inspection")
@RequiredArgsConstructor
public class PestInspectionController {

    private final PestInspectionService pestInspectionService;

    @GetMapping
    public ResponseEntity<List<PestInspectionDto>> findAll() {
        return ResponseEntity.ok(pestInspectionService.findAll().stream()
                .map(this::toDto)
                .toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PestInspectionDto> findById(@PathVariable Long id) {
        return pestInspectionService.findById(id)
                .map(this::toDto)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "pest-inspection.not.found"));
    }

    @PostMapping
    public ResponseEntity<PestInspectionDto> create(@RequestBody PestInspectionDto dto) {
        PestInspection saved = pestInspectionService.save(toEntity(dto));
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PestInspectionDto> update(@PathVariable Long id, @RequestBody PestInspectionDto dto) {
        PestInspection entity = toEntity(dto);
        entity.setId(id);
        return ResponseEntity.ok(toDto(pestInspectionService.save(entity)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        pestInspectionService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private PestInspectionDto toDto(PestInspection entity) {
        return new PestInspectionDto(

                entity.getId(),

                entity.getInspectedAt(),

                entity.getPestType(),

                entity.getSeverity(),

                entity.getAffectedAreaSquareMeters(),

                entity.getTreatmentApplied(),

                entity.getCropCycle() == null ? null : entity.getCropCycle().getId()

        );
    }

    private PestInspection toEntity(PestInspectionDto dto) {
        PestInspection entity = new PestInspection();




        entity.setInspectedAt(dto.inspectedAt());



        entity.setPestType(dto.pestType());



        entity.setSeverity(dto.severity());



        entity.setAffectedAreaSquareMeters(dto.affectedAreaSquareMeters());



        entity.setTreatmentApplied(dto.treatmentApplied());




        if (dto.cropCycleId() != null) {
            CropCycle cropCycle = new CropCycle();
            cropCycle.setId(dto.cropCycleId());
            entity.setCropCycle(cropCycle);
        }


        return entity;
    }
}
