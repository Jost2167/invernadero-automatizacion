package com.jost.invernadero.automatizacion.service;

import com.jost.invernadero.automatizacion.entity.GreenhouseAlert;

import java.util.List;

import java.util.Optional;


public interface GreenhouseAlertService {

    List<GreenhouseAlert> findAll();

    Optional<GreenhouseAlert> findById(Long id);

    GreenhouseAlert save(GreenhouseAlert entity);

    void deleteById(Long id);

}
