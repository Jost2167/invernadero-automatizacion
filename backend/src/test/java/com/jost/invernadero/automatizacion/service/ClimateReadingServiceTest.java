package com.jost.invernadero.automatizacion.service;

import com.jost.invernadero.automatizacion.entity.ClimateReading;
import com.jost.invernadero.automatizacion.repository.ClimateReadingRepository;
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
class ClimateReadingServiceTest {

    @Mock
    private ClimateReadingRepository climateReadingRepository;

    @InjectMocks
    private ClimateReadingServiceImpl climateReadingService;

    @Test
    void findAll_delegatesToRepository() {
        ClimateReading entity = new ClimateReading();
        when(climateReadingRepository.findAll()).thenReturn(List.of(entity));

        List<ClimateReading> result = climateReadingService.findAll();

        assertThat(result).containsExactly(entity);
        verify(climateReadingRepository).findAll();
    }

    @Test
    void findById_delegatesToRepository() {
        Long id = 1L;
        ClimateReading entity = new ClimateReading();
        when(climateReadingRepository.findById(id)).thenReturn(Optional.of(entity));

        Optional<ClimateReading> result = climateReadingService.findById(id);

        assertThat(result).contains(entity);
        verify(climateReadingRepository).findById(id);
    }

    @Test
    void save_delegatesToRepository() {
        ClimateReading entity = new ClimateReading();
        when(climateReadingRepository.save(entity)).thenReturn(entity);

        ClimateReading result = climateReadingService.save(entity);

        assertThat(result).isSameAs(entity);
        verify(climateReadingRepository).save(entity);
    }

    @Test
    void deleteById_delegatesToRepository() {
        Long id = 1L;

        climateReadingService.deleteById(id);

        verify(climateReadingRepository).deleteById(id);
    }
}
