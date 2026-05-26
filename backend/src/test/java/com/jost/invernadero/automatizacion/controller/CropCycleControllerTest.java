package com.jost.invernadero.automatizacion.controller;

import com.jost.invernadero.automatizacion.entity.CropCycle;
import com.jost.invernadero.automatizacion.filter.JwtAuthenticationFilter;
import com.jost.invernadero.automatizacion.service.CropCycleService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = CropCycleController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class))
@AutoConfigureMockMvc(addFilters = false)
class CropCycleControllerTest {

    private static final String BASE_PATH = "/api/crop-cycle";
    private static final String JSON_BODY = "{}";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CropCycleService cropCycleService;

    @Test
    void findAll_returns200() throws Exception {
        CropCycle entity = entityWithId(1L);
        when(cropCycleService.findAll()).thenReturn(List.of(entity));

        mockMvc.perform(get(BASE_PATH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void findById_whenExists_returns200() throws Exception {
        CropCycle entity = entityWithId(1L);
        when(cropCycleService.findById(1L)).thenReturn(Optional.of(entity));

        mockMvc.perform(get(BASE_PATH + "/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void findById_whenNotFound_returns404() throws Exception {
        when(cropCycleService.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get(BASE_PATH + "/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_returns201() throws Exception {
        CropCycle entity = entityWithId(1L);
        when(cropCycleService.save(any(CropCycle.class))).thenReturn(entity);

        mockMvc.perform(post(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void update_returns200() throws Exception {
        CropCycle entity = entityWithId(1L);
        when(cropCycleService.save(any(CropCycle.class))).thenReturn(entity);

        mockMvc.perform(put(BASE_PATH + "/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void delete_returns204() throws Exception {
        mockMvc.perform(delete(BASE_PATH + "/{id}", 1L))
                .andExpect(status().isNoContent());
    }

    private CropCycle entityWithId(Long id) {
        CropCycle entity = new CropCycle();
        entity.setId(id);
        return entity;
    }
}
