package com.jost.invernadero.automatizacion.service;

import com.jost.invernadero.automatizacion.entity.MaintenanceTask;
import com.jost.invernadero.automatizacion.repository.MaintenanceTaskRepository;
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
class MaintenanceTaskServiceTest {

    @Mock
    private MaintenanceTaskRepository maintenanceTaskRepository;

    @InjectMocks
    private MaintenanceTaskServiceImpl maintenanceTaskService;

    @Test
    void findAll_delegatesToRepository() {
        MaintenanceTask entity = new MaintenanceTask();
        when(maintenanceTaskRepository.findAll()).thenReturn(List.of(entity));

        List<MaintenanceTask> result = maintenanceTaskService.findAll();

        assertThat(result).containsExactly(entity);
        verify(maintenanceTaskRepository).findAll();
    }

    @Test
    void findById_delegatesToRepository() {
        Long id = 1L;
        MaintenanceTask entity = new MaintenanceTask();
        when(maintenanceTaskRepository.findById(id)).thenReturn(Optional.of(entity));

        Optional<MaintenanceTask> result = maintenanceTaskService.findById(id);

        assertThat(result).contains(entity);
        verify(maintenanceTaskRepository).findById(id);
    }

    @Test
    void save_delegatesToRepository() {
        MaintenanceTask entity = new MaintenanceTask();
        when(maintenanceTaskRepository.save(entity)).thenReturn(entity);

        MaintenanceTask result = maintenanceTaskService.save(entity);

        assertThat(result).isSameAs(entity);
        verify(maintenanceTaskRepository).save(entity);
    }

    @Test
    void deleteById_delegatesToRepository() {
        Long id = 1L;

        maintenanceTaskService.deleteById(id);

        verify(maintenanceTaskRepository).deleteById(id);
    }
}
