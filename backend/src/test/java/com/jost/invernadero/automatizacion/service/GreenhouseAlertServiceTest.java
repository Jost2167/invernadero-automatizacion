package com.jost.invernadero.automatizacion.service;

import com.jost.invernadero.automatizacion.entity.GreenhouseAlert;
import com.jost.invernadero.automatizacion.repository.GreenhouseAlertRepository;
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
class GreenhouseAlertServiceTest {

    @Mock
    private GreenhouseAlertRepository greenhouseAlertRepository;

    @InjectMocks
    private GreenhouseAlertServiceImpl greenhouseAlertService;

    @Test
    void findAll_delegatesToRepository() {
        GreenhouseAlert entity = new GreenhouseAlert();
        when(greenhouseAlertRepository.findAll()).thenReturn(List.of(entity));

        List<GreenhouseAlert> result = greenhouseAlertService.findAll();

        assertThat(result).containsExactly(entity);
        verify(greenhouseAlertRepository).findAll();
    }

    @Test
    void findById_delegatesToRepository() {
        Long id = 1L;
        GreenhouseAlert entity = new GreenhouseAlert();
        when(greenhouseAlertRepository.findById(id)).thenReturn(Optional.of(entity));

        Optional<GreenhouseAlert> result = greenhouseAlertService.findById(id);

        assertThat(result).contains(entity);
        verify(greenhouseAlertRepository).findById(id);
    }

    @Test
    void save_delegatesToRepository() {
        GreenhouseAlert entity = new GreenhouseAlert();
        when(greenhouseAlertRepository.save(entity)).thenReturn(entity);

        GreenhouseAlert result = greenhouseAlertService.save(entity);

        assertThat(result).isSameAs(entity);
        verify(greenhouseAlertRepository).save(entity);
    }

    @Test
    void deleteById_delegatesToRepository() {
        Long id = 1L;

        greenhouseAlertService.deleteById(id);

        verify(greenhouseAlertRepository).deleteById(id);
    }
}
