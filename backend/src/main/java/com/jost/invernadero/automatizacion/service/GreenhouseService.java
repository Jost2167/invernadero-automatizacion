package com.jost.invernadero.automatizacion.service;

import com.jost.invernadero.automatizacion.entity.Greenhouse;

import java.util.List;

import java.util.Optional;


public interface GreenhouseService {

    List<Greenhouse> findAll();

    Optional<Greenhouse> findById(Long id);

    Greenhouse save(Greenhouse entity);

    void deleteById(Long id);

}
