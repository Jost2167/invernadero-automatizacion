package com.jost.invernadero.automatizacion.service;

import com.jost.invernadero.automatizacion.entity.ClimateReading;

import java.util.List;

import java.util.Optional;


public interface ClimateReadingService {

    List<ClimateReading> findAll();

    Optional<ClimateReading> findById(Long id);

    ClimateReading save(ClimateReading entity);

    void deleteById(Long id);

}
