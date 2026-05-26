package com.jost.invernadero.automatizacion.service;

import com.jost.invernadero.automatizacion.entity.PestInspection;

import java.util.List;

import java.util.Optional;


public interface PestInspectionService {

    List<PestInspection> findAll();

    Optional<PestInspection> findById(Long id);

    PestInspection save(PestInspection entity);

    void deleteById(Long id);

}
