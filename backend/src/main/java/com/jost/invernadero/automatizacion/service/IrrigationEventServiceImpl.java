package com.jost.invernadero.automatizacion.service;

import com.jost.invernadero.automatizacion.entity.IrrigationEvent;
import com.jost.invernadero.automatizacion.repository.IrrigationEventRepository;

import java.util.List;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IrrigationEventServiceImpl implements IrrigationEventService {

    private final IrrigationEventRepository irrigationEventRepository;

    @Override
    public List<IrrigationEvent> findAll() {
        return irrigationEventRepository.findAll();
    }

    @Override
    public Optional<IrrigationEvent> findById(Long id) {
        return irrigationEventRepository.findById(id);
    }

    @Override
    public IrrigationEvent save(IrrigationEvent entity) {
        return irrigationEventRepository.save(entity);
    }

    @Override
    public void deleteById(Long id) {
        irrigationEventRepository.deleteById(id);
    }

}
