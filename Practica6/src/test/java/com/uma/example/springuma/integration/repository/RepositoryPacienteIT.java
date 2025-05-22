package com.uma.example.springuma.integration.repository;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.uma.example.springuma.model.Medico;
import com.uma.example.springuma.model.Paciente;
import com.uma.example.springuma.model.RepositoryMedico;
import com.uma.example.springuma.model.RepositoryPaciente;

@DataJpaTest
public class RepositoryPacienteIT {
    
    @Autowired
    private RepositoryPaciente pacienteRepository;
    @Autowired
    private RepositoryMedico medicoRepository;

    @Test
    @DisplayName("Comprueba que los datos persisten correctamente al guardar un paciente y se puede recuperar por DNI")
    public void givenPaciente_whenSave_thenSeGuardaEnLaBD() {
        // Given
        Paciente paciente = new Paciente();
        paciente.setNombre("John");
        paciente.setDni("1223A");

        // When
        pacienteRepository.save(paciente);

        // Then
        Paciente foundPaciente = pacienteRepository.findByDni("1223A");
        assertNotNull(foundPaciente);
        assertEquals("John", foundPaciente.getNombre());
    }

    @Test
    @DisplayName("Comprueba que se pueden recuperar los pacientes por id de medico")
    public void givenMedicoId_whenGetPacientes_thenSeRecuperanLosPacientes() {
        // Given
        Medico medico = new Medico();
        medico.setId(1L);
        medico.setNombre("Dr. Smith");
        medico.setDni("1223A");
        medico.setEspecialidad("Cardiología");

        // Guarda el medico
        medicoRepository.save(medico);

        Paciente paciente1 = new Paciente();
        paciente1.setNombre("John");
        paciente1.setDni("1223A");
        paciente1.setMedico(medico);
        
        Paciente paciente2 = new Paciente();
        paciente2.setNombre("Jane");
        paciente2.setDni("1224B");
        paciente2.setMedico(medico);

        // When
        pacienteRepository.save(paciente1);
        pacienteRepository.save(paciente2);

        // Then
        assertEquals(2, pacienteRepository.findByMedicoId(1L).size());
        assertEquals("John", pacienteRepository.findByMedicoId(1L).get(0).getNombre());
        assertEquals("Jane", pacienteRepository.findByMedicoId(1L).get(1).getNombre());
    }
}
