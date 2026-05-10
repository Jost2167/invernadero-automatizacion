package com.jost.invernadero.automatizacion.service;

import com.jost.invernadero.automatizacion.entity.PestInspection;
import com.jost.invernadero.automatizacion.repository.PestInspectionRepository;

import java.util.List;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PestInspectionServiceImpl implements PestInspectionService {

    private final PestInspectionRepository pestInspectionRepository;

    @Override
    public List<PestInspection> findAll() {
        return pestInspectionRepository.findAll();
    }

    @Override
    public Optional<PestInspection> findById(Long id) {
        return pestInspectionRepository.findById(id);
    }

    @Override
    public PestInspection save(PestInspection entity) {
        return pestInspectionRepository.save(entity);
    }

    @Override
    public void deleteById(Long id) {
        pestInspectionRepository.deleteById(id);
    }

}
