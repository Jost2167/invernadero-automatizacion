package com.jost.invernadero.automatizacion.service;

import com.jost.invernadero.automatizacion.entity.FertilizationEvent;

import java.util.List;

import java.util.Optional;


public interface FertilizationEventService {

    List<FertilizationEvent> findAll();

    Optional<FertilizationEvent> findById(Long id);

    FertilizationEvent save(FertilizationEvent entity);

    void deleteById(Long id);

}
