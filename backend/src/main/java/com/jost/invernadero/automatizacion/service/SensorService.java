package com.jost.invernadero.automatizacion.service;

import com.jost.invernadero.automatizacion.entity.Sensor;

import java.util.List;

import java.util.Optional;


public interface SensorService {

    List<Sensor> findAll();

    Optional<Sensor> findById(Long id);

    Sensor save(Sensor entity);

    void deleteById(Long id);

}
