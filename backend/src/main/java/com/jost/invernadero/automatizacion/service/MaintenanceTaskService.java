package com.jost.invernadero.automatizacion.service;

import com.jost.invernadero.automatizacion.entity.MaintenanceTask;

import java.util.List;

import java.util.Optional;


public interface MaintenanceTaskService {

    List<MaintenanceTask> findAll();

    Optional<MaintenanceTask> findById(Long id);

    MaintenanceTask save(MaintenanceTask entity);

    void deleteById(Long id);

}
