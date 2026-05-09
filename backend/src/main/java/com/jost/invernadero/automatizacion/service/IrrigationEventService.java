package com.jost.invernadero.automatizacion.service;

import com.jost.invernadero.automatizacion.entity.IrrigationEvent;

import java.util.List;

import java.util.Optional;


public interface IrrigationEventService {

    List<IrrigationEvent> findAll();

    Optional<IrrigationEvent> findById(Long id);

    IrrigationEvent save(IrrigationEvent entity);

    void deleteById(Long id);

}
