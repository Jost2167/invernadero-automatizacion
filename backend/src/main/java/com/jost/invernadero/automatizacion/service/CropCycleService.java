package com.jost.invernadero.automatizacion.service;

import com.jost.invernadero.automatizacion.entity.CropCycle;

import java.util.List;

import java.util.Optional;


public interface CropCycleService {

    List<CropCycle> findAll();

    Optional<CropCycle> findById(Long id);

    CropCycle save(CropCycle entity);

    void deleteById(Long id);

}
