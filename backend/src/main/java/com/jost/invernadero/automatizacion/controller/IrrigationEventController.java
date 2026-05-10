package com.jost.invernadero.automatizacion.controller;

import com.jost.invernadero.automatizacion.dto.IrrigationEventDto;
import com.jost.invernadero.automatizacion.entity.IrrigationEvent;

import com.jost.invernadero.automatizacion.entity.IrrigationEventMethod;



import com.jost.invernadero.automatizacion.entity.Greenhouse;


import com.jost.invernadero.automatizacion.service.IrrigationEventService;

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
@RequestMapping("/api/irrigation-event")
@RequiredArgsConstructor
public class IrrigationEventController {

    private final IrrigationEventService irrigationEventService;

    @GetMapping
    public ResponseEntity<List<IrrigationEventDto>> findAll() {
        return ResponseEntity.ok(irrigationEventService.findAll().stream()
                .map(this::toDto)
                .toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<IrrigationEventDto> findById(@PathVariable Long id) {
        return irrigationEventService.findById(id)
                .map(this::toDto)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "irrigation-event.not.found"));
    }

    @PostMapping
    public ResponseEntity<IrrigationEventDto> create(@RequestBody IrrigationEventDto dto) {
        IrrigationEvent saved = irrigationEventService.save(toEntity(dto));
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<IrrigationEventDto> update(@PathVariable Long id, @RequestBody IrrigationEventDto dto) {
        IrrigationEvent entity = toEntity(dto);
        entity.setId(id);
        return ResponseEntity.ok(toDto(irrigationEventService.save(entity)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        irrigationEventService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private IrrigationEventDto toDto(IrrigationEvent entity) {
        return new IrrigationEventDto(

                entity.getId(),

                entity.getStartedAt(),

                entity.getEndedAt(),

                entity.getWaterLiters(),

                entity.getMethod(),

                entity.getNotes(),

                entity.getGreenhouse() == null ? null : entity.getGreenhouse().getId()

        );
    }

    private IrrigationEvent toEntity(IrrigationEventDto dto) {
        IrrigationEvent entity = new IrrigationEvent();




        entity.setStartedAt(dto.startedAt());



        entity.setEndedAt(dto.endedAt());



        entity.setWaterLiters(dto.waterLiters());



        entity.setMethod(dto.method());



        entity.setNotes(dto.notes());




        if (dto.greenhouseId() != null) {
            Greenhouse greenhouse = new Greenhouse();
            greenhouse.setId(dto.greenhouseId());
            entity.setGreenhouse(greenhouse);
        }


        return entity;
    }
}
