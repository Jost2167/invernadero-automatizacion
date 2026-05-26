package com.jost.invernadero.automatizacion.service;

import com.jost.invernadero.automatizacion.entity.Location;
import com.jost.invernadero.automatizacion.repository.LocationRepository;
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
class LocationServiceTest {

    @Mock
    private LocationRepository locationRepository;

    @InjectMocks
    private LocationServiceImpl locationService;

    @Test
    void findAll_delegatesToRepository() {
        Location entity = new Location();
        when(locationRepository.findAll()).thenReturn(List.of(entity));

        List<Location> result = locationService.findAll();

        assertThat(result).containsExactly(entity);
        verify(locationRepository).findAll();
    }

    @Test
    void findById_delegatesToRepository() {
        Long id = 1L;
        Location entity = new Location();
        when(locationRepository.findById(id)).thenReturn(Optional.of(entity));

        Optional<Location> result = locationService.findById(id);

        assertThat(result).contains(entity);
        verify(locationRepository).findById(id);
    }

    @Test
    void save_delegatesToRepository() {
        Location entity = new Location();
        when(locationRepository.save(entity)).thenReturn(entity);

        Location result = locationService.save(entity);

        assertThat(result).isSameAs(entity);
        verify(locationRepository).save(entity);
    }

    @Test
    void deleteById_delegatesToRepository() {
        Long id = 1L;

        locationService.deleteById(id);

        verify(locationRepository).deleteById(id);
    }
}
