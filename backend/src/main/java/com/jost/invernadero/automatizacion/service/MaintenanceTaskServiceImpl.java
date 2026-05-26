package com.jost.invernadero.automatizacion.service;

import com.jost.invernadero.automatizacion.entity.MaintenanceTask;
import com.jost.invernadero.automatizacion.repository.MaintenanceTaskRepository;

import java.util.List;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MaintenanceTaskServiceImpl implements MaintenanceTaskService {

    private final MaintenanceTaskRepository maintenanceTaskRepository;

    @Override
    public List<MaintenanceTask> findAll() {
        return maintenanceTaskRepository.findAll();
    }

    @Override
    public Optional<MaintenanceTask> findById(Long id) {
        return maintenanceTaskRepository.findById(id);
    }

    @Override
    public MaintenanceTask save(MaintenanceTask entity) {
        return maintenanceTaskRepository.save(entity);
    }

    @Override
    public void deleteById(Long id) {
        maintenanceTaskRepository.deleteById(id);
    }

}
