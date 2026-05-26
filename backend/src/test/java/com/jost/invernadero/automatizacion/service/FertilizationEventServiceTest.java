package com.jost.invernadero.automatizacion.service;

import com.jost.invernadero.automatizacion.entity.FertilizationEvent;
import com.jost.invernadero.automatizacion.repository.FertilizationEventRepository;
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
class FertilizationEventServiceTest {

    @Mock
    private FertilizationEventRepository fertilizationEventRepository;

    @InjectMocks
    private FertilizationEventServiceImpl fertilizationEventService;

    @Test
    void findAll_delegatesToRepository() {
        FertilizationEvent entity = new FertilizationEvent();
        when(fertilizationEventRepository.findAll()).thenReturn(List.of(entity));

        List<FertilizationEvent> result = fertilizationEventService.findAll();

        assertThat(result).containsExactly(entity);
        verify(fertilizationEventRepository).findAll();
    }

    @Test
    void findById_delegatesToRepository() {
        Long id = 1L;
        FertilizationEvent entity = new FertilizationEvent();
        when(fertilizationEventRepository.findById(id)).thenReturn(Optional.of(entity));

        Optional<FertilizationEvent> result = fertilizationEventService.findById(id);

        assertThat(result).contains(entity);
        verify(fertilizationEventRepository).findById(id);
    }

    @Test
    void save_delegatesToRepository() {
        FertilizationEvent entity = new FertilizationEvent();
        when(fertilizationEventRepository.save(entity)).thenReturn(entity);

        FertilizationEvent result = fertilizationEventService.save(entity);

        assertThat(result).isSameAs(entity);
        verify(fertilizationEventRepository).save(entity);
    }

    @Test
    void deleteById_delegatesToRepository() {
        Long id = 1L;

        fertilizationEventService.deleteById(id);

        verify(fertilizationEventRepository).deleteById(id);
    }
}
