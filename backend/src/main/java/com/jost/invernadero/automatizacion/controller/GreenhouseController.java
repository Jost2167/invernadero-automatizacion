package com.jost.invernadero.automatizacion.controller;

import com.jost.invernadero.automatizacion.dto.GreenhouseDto;
import com.jost.invernadero.automatizacion.entity.Greenhouse;

import com.jost.invernadero.automatizacion.entity.GreenhouseStatus;



import com.jost.invernadero.automatizacion.entity.Location;


import com.jost.invernadero.automatizacion.service.GreenhouseService;

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
@RequestMapping("/api/greenhouse")
@RequiredArgsConstructor
public class GreenhouseController {

    private final GreenhouseService greenhouseService;

    @GetMapping
    public ResponseEntity<List<GreenhouseDto>> findAll() {
        return ResponseEntity.ok(greenhouseService.findAll().stream()
                .map(this::toDto)
                .toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<GreenhouseDto> findById(@PathVariable Long id) {
        return greenhouseService.findById(id)
                .map(this::toDto)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "greenhouse.not.found"));
    }

    @PostMapping
    public ResponseEntity<GreenhouseDto> create(@RequestBody GreenhouseDto dto) {
        Greenhouse saved = greenhouseService.save(toEntity(dto));
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GreenhouseDto> update(@PathVariable Long id, @RequestBody GreenhouseDto dto) {
        Greenhouse entity = toEntity(dto);
        entity.setId(id);
        return ResponseEntity.ok(toDto(greenhouseService.save(entity)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        greenhouseService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private GreenhouseDto toDto(Greenhouse entity) {
        return new GreenhouseDto(

                entity.getId(),

                entity.getCode(),

                entity.getName(),

                entity.getAreaSquareMeters(),

                entity.getStatus(),

                entity.getActive(),

                entity.getLocation() == null ? null : entity.getLocation().getId()

        );
    }

    private Greenhouse toEntity(GreenhouseDto dto) {
        Greenhouse entity = new Greenhouse();




        entity.setCode(dto.code());



        entity.setName(dto.name());



        entity.setAreaSquareMeters(dto.areaSquareMeters());



        entity.setStatus(dto.status());



        entity.setActive(dto.active());




        if (dto.locationId() != null) {
            Location location = new Location();
            location.setId(dto.locationId());
            entity.setLocation(location);
        }


        return entity;
    }
}
