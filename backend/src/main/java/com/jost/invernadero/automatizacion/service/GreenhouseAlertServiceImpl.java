package com.jost.invernadero.automatizacion.service;

import com.jost.invernadero.automatizacion.entity.GreenhouseAlert;
import com.jost.invernadero.automatizacion.repository.GreenhouseAlertRepository;

import java.util.List;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GreenhouseAlertServiceImpl implements GreenhouseAlertService {

    private final GreenhouseAlertRepository greenhouseAlertRepository;

    @Override
    public List<GreenhouseAlert> findAll() {
        return greenhouseAlertRepository.findAll();
    }

    @Override
    public Optional<GreenhouseAlert> findById(Long id) {
        return greenhouseAlertRepository.findById(id);
    }

    @Override
    public GreenhouseAlert save(GreenhouseAlert entity) {
        return greenhouseAlertRepository.save(entity);
    }

    @Override
    public void deleteById(Long id) {
        greenhouseAlertRepository.deleteById(id);
    }

}
