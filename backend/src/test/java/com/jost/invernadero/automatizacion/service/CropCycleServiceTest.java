package com.jost.invernadero.automatizacion.service;

import com.jost.invernadero.automatizacion.entity.CropCycle;
import com.jost.invernadero.automatizacion.repository.CropCycleRepository;
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
class CropCycleServiceTest {

    @Mock
    private CropCycleRepository cropCycleRepository;

    @InjectMocks
    private CropCycleServiceImpl cropCycleService;

    @Test
    void findAll_delegatesToRepository() {
        CropCycle entity = new CropCycle();
        when(cropCycleRepository.findAll()).thenReturn(List.of(entity));

        List<CropCycle> result = cropCycleService.findAll();

        assertThat(result).containsExactly(entity);
        verify(cropCycleRepository).findAll();
    }

    @Test
    void findById_delegatesToRepository() {
        Long id = 1L;
        CropCycle entity = new CropCycle();
        when(cropCycleRepository.findById(id)).thenReturn(Optional.of(entity));

        Optional<CropCycle> result = cropCycleService.findById(id);

        assertThat(result).contains(entity);
        verify(cropCycleRepository).findById(id);
    }

    @Test
    void save_delegatesToRepository() {
        CropCycle entity = new CropCycle();
        when(cropCycleRepository.save(entity)).thenReturn(entity);

        CropCycle result = cropCycleService.save(entity);

        assertThat(result).isSameAs(entity);
        verify(cropCycleRepository).save(entity);
    }

    @Test
    void deleteById_delegatesToRepository() {
        Long id = 1L;

        cropCycleService.deleteById(id);

        verify(cropCycleRepository).deleteById(id);
    }
}
