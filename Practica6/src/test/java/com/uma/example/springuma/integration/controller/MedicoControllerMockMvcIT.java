package com.uma.example.springuma.integration.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.uma.example.springuma.integration.base.AbstractIntegration;
import com.uma.example.springuma.model.Medico;

class MedicoControllerMockMvcIT extends AbstractIntegration {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Crea un medico y lo obtiene correctamente")
    void givenMedico_whenCreateMedico_thenReturn201() throws Exception {
        // Given
        Medico medico = new Medico();
        medico.setId(1);
        medico.setNombre("John");
        medico.setDni("1234A");
        medico.setEspecialidad("Cardiología");

        // When
        // Creo un medico
        mockMvc.perform(post("/medico")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(medico)))
                .andDo(print())
                .andExpect(status().isCreated());   // Then

        // Obtengo el medico
        mockMvc.perform(get("/medico/1"))
                .andDo(print())
                .andExpect(status().isOk()) // Se obtiene correctamente
                .andExpect(jsonPath("$.nombre").value("John"));
    }

    @Test
    @DisplayName("Actualiza un medico y lo obtiene correctamente")
    void givenMedico_whenUpdateMedico_thenReturn200() throws Exception {
        Medico medico = new Medico();
        medico.setId(1);
        medico.setNombre("John");
        medico.setDni("1234A");
        medico.setEspecialidad("Cardiología");

        // Primero creo el medico
        mockMvc.perform(post("/medico")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(medico)))
                .andDo(print())
                .andExpect(status().isCreated());

        // Luego lo actualizo
        medico.setNombre("Jane");
        mockMvc.perform(put("/medico")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(medico)))
                .andDo(print())
                .andExpect(status().isNoContent());

        // Finalmente lo obtengo actualizado
        // La id es unica, por lo que sé que no es un nuevo medico
        mockMvc.perform(get("/medico/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Jane"));
    }

    @Test
    @DisplayName("Elimina un medico y lo busca para comprobar que no existe")
    void givenMedico_whenDeleteMedico_thenReturn200() throws Exception {
        Medico medico = new Medico();
        medico.setId(1);
        medico.setNombre("John");
        medico.setDni("1234A");
        medico.setEspecialidad("Cardiología");

        // Primero creo el medico
        mockMvc.perform(post("/medico")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(medico)))
                .andDo(print())
                .andExpect(status().isCreated());

        // Luego lo elimino
        mockMvc.perform(delete("/medico/1"))
                .andDo(print())
                .andExpect(status().isOk());

        // Finalmente lo busco y compruebo que no existe
        mockMvc.perform(get("/medico/1"))
                .andDo(print())
                .andExpect(status().is5xxServerError());

        // Tampoco existe por dni
        mockMvc.perform(get("/medico/dni/1234A"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

}