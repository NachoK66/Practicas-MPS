package com.uma.example.springuma.integration.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uma.example.springuma.integration.base.AbstractIntegration;
import com.uma.example.springuma.model.Medico;
import com.uma.example.springuma.model.Paciente;

public class PacienteControllerMockMvcIT extends AbstractIntegration {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Crea un paciente y lo obtiene correctamente")
    void givenPaciente_whenCreatePaciente_thenReturn201() throws Exception {
        // Given
        Paciente paciente = new Paciente();
        paciente.setId(1);
        paciente.setNombre("Pepe");
        paciente.setEdad(30);
        paciente.setCita("2023-10-01");
        paciente.setDni("1234A");

        // When
        mockMvc.perform(post("/paciente")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(paciente)))
                .andDo(print())
                .andExpect(status().isCreated()); // Then

        // Obtengo el paciente
        mockMvc.perform(get("/paciente/" + paciente.getId()))
                .andDo(print())
                .andExpect(status().isOk()) // Se obtiene correctamente
                .andExpect(jsonPath("$.nombre").value("Pepe"));
    }

    @Test
    @DisplayName("Actualiza un paciente y lo obtiene correctamente con los datos actualizados")
    void givenPaciente_whenUpdatePaciente_thenReturn200() throws Exception {
        // Given
        Paciente paciente = new Paciente();
        paciente.setId(1);
        paciente.setNombre("Pepe");
        paciente.setEdad(30);
        paciente.setCita("2023-10-01");
        paciente.setDni("1234A");

        // Creo el paciente
        mockMvc.perform(post("/paciente")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(paciente)))
                .andDo(print())
                .andExpect(status().isCreated());

        // Luego lo actualizo
        paciente.setNombre("Juan");

        // Actualizo el paciente
        mockMvc.perform(put("/paciente")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(paciente)))
                .andDo(print())
                .andExpect(status().isNoContent());

        // Obtengo el paciente
        mockMvc.perform(get("/paciente/" + paciente.getId()))
                .andDo(print())
                .andExpect(status().isOk()) // Se obtiene correctamente
                .andExpect(jsonPath("$.nombre").value("Juan"));
    }

    @Test
    @DisplayName("Elimina un paciente y verifica que no existe")
    void givenPaciente_whenDeletePaciente_thenReturnError() throws Exception {
        // Given
        Paciente paciente = new Paciente();
        paciente.setId(1);
        paciente.setNombre("Pepe");
        paciente.setEdad(30);
        paciente.setCita("2023-10-01");
        paciente.setDni("1234A");

        // Creo el paciente
        mockMvc.perform(post("/paciente")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(paciente)))
                .andDo(print())
                .andExpect(status().isCreated());

        // Elimino el paciente
        mockMvc.perform(delete("/paciente/" + paciente.getId()))
                .andDo(print())
                .andExpect(status().isOk());

        // Verifico que no existe
        mockMvc.perform(get("/paciente/" + paciente.getId()))
                .andDo(print())
                .andExpect(status().is5xxServerError());
    }

    @Test
    @DisplayName("Se obtiene correctamente un paciente con su médico asociado")
    void givenPacienteWithMedico_whenCreatePaciente_thenReturn201() throws Exception {
        Medico medico = new Medico();
        medico.setId(1);
        medico.setNombre("Dr. Smith");
        medico.setDni("5678B");
        medico.setEspecialidad("Cardiología");

        mockMvc.perform(post("/medico")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(medico)))
                .andDo(print())
                .andExpect(status().isCreated());

        // Creo el paciente
        Paciente paciente = new Paciente();
        paciente.setId(1);
        paciente.setNombre("Pepe");
        paciente.setEdad(30);
        paciente.setCita("2023-10-01");
        paciente.setDni("1234A");
        paciente.setMedico(medico);

        mockMvc.perform(post("/paciente")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(paciente)))
                .andDo(print())
                .andExpect(status().isCreated());

        // Obtengo el paciente
        mockMvc.perform(get("/paciente/" + paciente.getId()))
                .andDo(print())
                .andExpect(status().isOk()) // Se obtiene correctamente
                .andExpect(jsonPath("$.nombre").value("Pepe"))
                .andExpect(jsonPath("$.medico.nombre").value("Dr. Smith")); // Verifico que el médico está asociado

        // Obtengo los pacientes del médico y verifico que el paciente está en la lista
        mockMvc.perform(get("/paciente/medico/" + medico.getId()))
                .andDo(print())
                .andExpect(status().isOk()) // Se obtiene correctamente
                .andExpect(jsonPath("$[0].nombre").value("Pepe")); // Verifico que el paciente está en la lista
    }

    @Test
    @DisplayName("Cambia el médico de un paciente y verifica que se actualiza correctamente y el médico anterior no tiene pacientes asignados")
    void givenPaciente_whenChangeMedico_thenReturnUpdated() throws Exception {
        Medico medico = new Medico();
        medico.setId(1);
        medico.setNombre("Dr. Smith");
        medico.setDni("5678B");
        medico.setEspecialidad("Cardiología");

        mockMvc.perform(post("/medico")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(medico)))
                .andDo(print())
                .andExpect(status().isCreated());

        // Creo el paciente
        Paciente paciente = new Paciente();
        paciente.setId(1);
        paciente.setNombre("Pepe");
        paciente.setEdad(30);
        paciente.setCita("2023-10-01");
        paciente.setDni("1234A");
        paciente.setMedico(medico);

        mockMvc.perform(post("/paciente")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(paciente)))
                .andDo(print())
                .andExpect(status().isCreated());

        // Verifico que el paciente está asociado al médico
        mockMvc.perform(get("/paciente/medico/" + medico.getId()))
                .andDo(print())
                .andExpect(status().isOk()) // Se obtiene correctamente
                .andExpect(jsonPath("$[0].nombre").value("Pepe")); // Verifico que el paciente está en la lista

        // Cambio el médico del paciente
        Medico nuevoMedico = new Medico();
        nuevoMedico.setId(2);
        nuevoMedico.setNombre("Dr. Johnson");
        nuevoMedico.setDni("91011C");
        nuevoMedico.setEspecialidad("Pediatría");

        // Creo el nuevo médico
        mockMvc.perform(post("/medico")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(nuevoMedico)))
                .andDo(print())
                .andExpect(status().isCreated());

        // Actualizo el médico del paciente
        paciente.setMedico(nuevoMedico);
        mockMvc.perform(put("/paciente")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(paciente)))
                .andDo(print())
                .andExpect(status().isNoContent());

        // Verifico que el paciente está asociado al nuevo médico
        mockMvc.perform(get("/paciente/medico/" + nuevoMedico.getId()))
                .andDo(print())
                .andExpect(status().isOk()) // Se obtiene correctamente
                .andExpect(jsonPath("$[0].nombre").value("Pepe")); // Verifico que el paciente está en la lista

        // Verifico que el médico anterior no tiene pacientes asignados
        mockMvc.perform(get("/paciente/medico/" + medico.getId()))
                .andDo(print())
                .andExpect(status().isOk()) // Se obtiene correctamente
                .andExpect(jsonPath("$").isEmpty()); // Verifico que la lista está vacía
    }

    @Test
    @DisplayName("Elimina un paciente y verifica que el médico no tiene pacientes asignados")
    void givenPaciente_whenDeletePaciente_thenReturnNoPatients() throws Exception {
        Medico medico = new Medico();
        medico.setId(1);
        medico.setNombre("Dr. Smith");
        medico.setDni("5678B");
        medico.setEspecialidad("Cardiología");

        mockMvc.perform(post("/medico")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(medico)))
                .andDo(print())
                .andExpect(status().isCreated());

        // Creo el paciente
        Paciente paciente = new Paciente();
        paciente.setId(1);
        paciente.setNombre("Pepe");
        paciente.setEdad(30);
        paciente.setCita("2023-10-01");
        paciente.setDni("1234A");
        paciente.setMedico(medico);

        mockMvc.perform(post("/paciente")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(paciente)))
                .andDo(print())
                .andExpect(status().isCreated());

        // Verifico que el paciente está asociado al médico
        mockMvc.perform(get("/paciente/medico/" + medico.getId()))
                .andDo(print())
                .andExpect(status().isOk()) // Se obtiene correctamente
                .andExpect(jsonPath("$[0].nombre").value("Pepe")); // Verifico que el paciente está en la lista

        // Elimino el paciente
        mockMvc.perform(delete("/paciente/" + paciente.getId()))
                .andDo(print())
                .andExpect(status().isOk());

        // Verifico que el médico ahora no tiene pacientes asignados
        mockMvc.perform(get("/paciente/medico/" + medico.getId()))
                .andDo(print())
                .andExpect(status().isOk()) // Se obtiene correctamente
                .andExpect(jsonPath("$").isEmpty()); // Verifico que la lista está vacía
    }
}
