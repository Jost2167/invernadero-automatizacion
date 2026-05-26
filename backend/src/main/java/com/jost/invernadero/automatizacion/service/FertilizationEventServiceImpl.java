package com.jost.invernadero.automatizacion.service;

import com.jost.invernadero.automatizacion.entity.FertilizationEvent;
import com.jost.invernadero.automatizacion.repository.FertilizationEventRepository;

import java.util.List;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FertilizationEventServiceImpl implements FertilizationEventService {

    private final FertilizationEventRepository fertilizationEventRepository;

    @Override
    public List<FertilizationEvent> findAll() {
        return fertilizationEventRepository.findAll();
    }

    @Override
    public Optional<FertilizationEvent> findById(Long id) {
        return fertilizationEventRepository.findById(id);
    }

    @Override
    public FertilizationEvent save(FertilizationEvent entity) {
        return fertilizationEventRepository.save(entity);
    }

    @Override
    public void deleteById(Long id) {
        fertilizationEventRepository.deleteById(id);
    }

}
