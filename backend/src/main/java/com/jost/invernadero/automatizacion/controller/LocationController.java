package com.jost.invernadero.automatizacion.controller;

import com.jost.invernadero.automatizacion.dto.LocationDto;
import com.jost.invernadero.automatizacion.entity.Location;


import com.jost.invernadero.automatizacion.service.LocationService;

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
@RequestMapping("/api/location")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    @GetMapping
    public ResponseEntity<List<LocationDto>> findAll() {
        return ResponseEntity.ok(locationService.findAll().stream()
                .map(this::toDto)
                .toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LocationDto> findById(@PathVariable Long id) {
        return locationService.findById(id)
                .map(this::toDto)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "location.not.found"));
    }

    @PostMapping
    public ResponseEntity<LocationDto> create(@RequestBody LocationDto dto) {
        Location saved = locationService.save(toEntity(dto));
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LocationDto> update(@PathVariable Long id, @RequestBody LocationDto dto) {
        Location entity = toEntity(dto);
        entity.setId(id);
        return ResponseEntity.ok(toDto(locationService.save(entity)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        locationService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private LocationDto toDto(Location entity) {
        return new LocationDto(

                entity.getId(),

                entity.getName(),

                entity.getDescription(),

                entity.getActive()

        );
    }

    private Location toEntity(LocationDto dto) {
        Location entity = new Location();




        entity.setName(dto.name());



        entity.setDescription(dto.description());



        entity.setActive(dto.active());



        return entity;
    }
}
