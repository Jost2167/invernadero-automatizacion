package com.jost.invernadero.automatizacion.service;

import com.jost.invernadero.automatizacion.entity.CropCycle;
import com.jost.invernadero.automatizacion.repository.CropCycleRepository;

import java.util.List;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CropCycleServiceImpl implements CropCycleService {

    private final CropCycleRepository cropCycleRepository;

    @Override
    public List<CropCycle> findAll() {
        return cropCycleRepository.findAll();
    }

    @Override
    public Optional<CropCycle> findById(Long id) {
        return cropCycleRepository.findById(id);
    }

    @Override
    public CropCycle save(CropCycle entity) {
        return cropCycleRepository.save(entity);
    }

    @Override
    public void deleteById(Long id) {
        cropCycleRepository.deleteById(id);
    }

}
