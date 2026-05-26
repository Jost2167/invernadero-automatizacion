package com.jost.invernadero.automatizacion.service;

import com.jost.invernadero.automatizacion.entity.Location;

import java.util.List;

import java.util.Optional;


public interface LocationService {

    List<Location> findAll();

    Optional<Location> findById(Long id);

    Location save(Location entity);

    void deleteById(Long id);

}
