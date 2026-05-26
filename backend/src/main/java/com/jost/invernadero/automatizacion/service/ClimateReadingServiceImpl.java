package com.jost.invernadero.automatizacion.service;

import com.jost.invernadero.automatizacion.entity.ClimateReading;
import com.jost.invernadero.automatizacion.repository.ClimateReadingRepository;

import java.util.List;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClimateReadingServiceImpl implements ClimateReadingService {

    private final ClimateReadingRepository climateReadingRepository;

    @Override
    public List<ClimateReading> findAll() {
        return climateReadingRepository.findAll();
    }

    @Override
    public Optional<ClimateReading> findById(Long id) {
        return climateReadingRepository.findById(id);
    }

    @Override
    public ClimateReading save(ClimateReading entity) {
        return climateReadingRepository.save(entity);
    }

    @Override
    public void deleteById(Long id) {
        climateReadingRepository.deleteById(id);
    }

}
