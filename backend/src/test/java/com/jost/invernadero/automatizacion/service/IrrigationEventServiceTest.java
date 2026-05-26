package com.jost.invernadero.automatizacion.service;

import com.jost.invernadero.automatizacion.entity.IrrigationEvent;
import com.jost.invernadero.automatizacion.repository.IrrigationEventRepository;
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
class IrrigationEventServiceTest {

    @Mock
    private IrrigationEventRepository irrigationEventRepository;

    @InjectMocks
    private IrrigationEventServiceImpl irrigationEventService;

    @Test
    void findAll_delegatesToRepository() {
        IrrigationEvent entity = new IrrigationEvent();
        when(irrigationEventRepository.findAll()).thenReturn(List.of(entity));

        List<IrrigationEvent> result = irrigationEventService.findAll();

        assertThat(result).containsExactly(entity);
        verify(irrigationEventRepository).findAll();
    }

    @Test
    void findById_delegatesToRepository() {
        Long id = 1L;
        IrrigationEvent entity = new IrrigationEvent();
        when(irrigationEventRepository.findById(id)).thenReturn(Optional.of(entity));

        Optional<IrrigationEvent> result = irrigationEventService.findById(id);

        assertThat(result).contains(entity);
        verify(irrigationEventRepository).findById(id);
    }

    @Test
    void save_delegatesToRepository() {
        IrrigationEvent entity = new IrrigationEvent();
        when(irrigationEventRepository.save(entity)).thenReturn(entity);

        IrrigationEvent result = irrigationEventService.save(entity);

        assertThat(result).isSameAs(entity);
        verify(irrigationEventRepository).save(entity);
    }

    @Test
    void deleteById_delegatesToRepository() {
        Long id = 1L;

        irrigationEventService.deleteById(id);

        verify(irrigationEventRepository).deleteById(id);
    }
}
