package com.jost.invernadero.automatizacion.service;

import com.jost.invernadero.automatizacion.entity.PestInspection;
import com.jost.invernadero.automatizacion.repository.PestInspectionRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PestInspectionServiceTest {

    @Mock
    private PestInspectionRepository pestInspectionRepository;

    @InjectMocks
    private PestInspectionServiceImpl pestInspectionService;

    @Test
    void findAll_delegatesToRepository() {
        PestInspection entity = new PestInspection();
        when(pestInspectionRepository.findAll()).thenReturn(List.of(entity));

        List<PestInspection> result = pestInspectionService.findAll();

        assertThat(result).containsExactly(entity);
        verify(pestInspectionRepository).findAll();
    }

    @Test
    void findById_delegatesToRepository() {
        Long id = 1L;
        PestInspection entity = new PestInspection();
        when(pestInspectionRepository.findById(id)).thenReturn(Optional.of(entity));

        Optional<PestInspection> result = pestInspectionService.findById(id);

        assertThat(result).contains(entity);
        verify(pestInspectionRepository).findById(id);
    }

    @Test
    void save_delegatesToRepository() {
        PestInspection entity = new PestInspection();
        when(pestInspectionRepository.save(entity)).thenReturn(entity);

        PestInspection result = pestInspectionService.save(entity);

        assertThat(result).isSameAs(entity);
        verify(pestInspectionRepository).save(entity);
    }

    @Test
    void deleteById_delegatesToRepository() {
        Long id = 1L;

        pestInspectionService.deleteById(id);

        verify(pestInspectionRepository).deleteById(id);
    }
}
