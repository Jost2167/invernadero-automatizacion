package com.jost.invernadero.automatizacion.service;

import com.jost.invernadero.automatizacion.entity.Sensor;
import com.jost.invernadero.automatizacion.repository.SensorRepository;
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
class SensorServiceTest {

    @Mock
    private SensorRepository sensorRepository;

    @InjectMocks
    private SensorServiceImpl sensorService;

    @Test
    void findAll_delegatesToRepository() {
        Sensor entity = new Sensor();
        when(sensorRepository.findAll()).thenReturn(List.of(entity));

        List<Sensor> result = sensorService.findAll();

        assertThat(result).containsExactly(entity);
        verify(sensorRepository).findAll();
    }

    @Test
    void findById_delegatesToRepository() {
        Long id = 1L;
        Sensor entity = new Sensor();
        when(sensorRepository.findById(id)).thenReturn(Optional.of(entity));

        Optional<Sensor> result = sensorService.findById(id);

        assertThat(result).contains(entity);
        verify(sensorRepository).findById(id);
    }

    @Test
    void save_delegatesToRepository() {
        Sensor entity = new Sensor();
        when(sensorRepository.save(entity)).thenReturn(entity);

        Sensor result = sensorService.save(entity);

        assertThat(result).isSameAs(entity);
        verify(sensorRepository).save(entity);
    }

    @Test
    void deleteById_delegatesToRepository() {
        Long id = 1L;

        sensorService.deleteById(id);

        verify(sensorRepository).deleteById(id);
    }
}
