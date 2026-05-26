package com.jost.invernadero.automatizacion.service;

import com.jost.invernadero.automatizacion.entity.Sensor;
import com.jost.invernadero.automatizacion.repository.SensorRepository;

import java.util.List;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SensorServiceImpl implements SensorService {

    private final SensorRepository sensorRepository;

    @Override
    public List<Sensor> findAll() {
        return sensorRepository.findAll();
    }

    @Override
    public Optional<Sensor> findById(Long id) {
        return sensorRepository.findById(id);
    }

    @Override
    public Sensor save(Sensor entity) {
        return sensorRepository.save(entity);
    }

    @Override
    public void deleteById(Long id) {
        sensorRepository.deleteById(id);
    }

}
