package com.jost.invernadero.automatizacion.service;

import com.jost.invernadero.automatizacion.entity.Greenhouse;
import com.jost.invernadero.automatizacion.repository.GreenhouseRepository;

import java.util.List;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GreenhouseServiceImpl implements GreenhouseService {

    private final GreenhouseRepository greenhouseRepository;

    @Override
    public List<Greenhouse> findAll() {
        return greenhouseRepository.findAll();
    }

    @Override
    public Optional<Greenhouse> findById(Long id) {
        return greenhouseRepository.findById(id);
    }

    @Override
    public Greenhouse save(Greenhouse entity) {
        return greenhouseRepository.save(entity);
    }

    @Override
    public void deleteById(Long id) {
        greenhouseRepository.deleteById(id);
    }

}
