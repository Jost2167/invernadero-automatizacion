package com.jost.invernadero.automatizacion.service;

import com.jost.invernadero.automatizacion.entity.Greenhouse;
import com.jost.invernadero.automatizacion.repository.GreenhouseRepository;
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
class GreenhouseServiceTest {

    @Mock
    private GreenhouseRepository greenhouseRepository;

    @InjectMocks
    private GreenhouseServiceImpl greenhouseService;

    @Test
    void findAll_delegatesToRepository() {
        Greenhouse entity = new Greenhouse();
        when(greenhouseRepository.findAll()).thenReturn(List.of(entity));

        List<Greenhouse> result = greenhouseService.findAll();

        assertThat(result).containsExactly(entity);
        verify(greenhouseRepository).findAll();
    }

    @Test
    void findById_delegatesToRepository() {
        Long id = 1L;
        Greenhouse entity = new Greenhouse();
        when(greenhouseRepository.findById(id)).thenReturn(Optional.of(entity));

        Optional<Greenhouse> result = greenhouseService.findById(id);

        assertThat(result).contains(entity);
        verify(greenhouseRepository).findById(id);
    }

    @Test
    void save_delegatesToRepository() {
        Greenhouse entity = new Greenhouse();
        when(greenhouseRepository.save(entity)).thenReturn(entity);

        Greenhouse result = greenhouseService.save(entity);

        assertThat(result).isSameAs(entity);
        verify(greenhouseRepository).save(entity);
    }

    @Test
    void deleteById_delegatesToRepository() {
        Long id = 1L;

        greenhouseService.deleteById(id);

        verify(greenhouseRepository).deleteById(id);
    }
}
