package com.jost.invernadero.automatizacion.service;

import com.jost.invernadero.automatizacion.entity.Location;
import com.jost.invernadero.automatizacion.repository.LocationRepository;

import java.util.List;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LocationServiceImpl implements LocationService {

    private final LocationRepository locationRepository;

    @Override
    public List<Location> findAll() {
        return locationRepository.findAll();
    }

    @Override
    public Optional<Location> findById(Long id) {
        return locationRepository.findById(id);
    }

    @Override
    public Location save(Location entity) {
        return locationRepository.save(entity);
    }

    @Override
    public void deleteById(Long id) {
        locationRepository.deleteById(id);
    }

}
