package com.uma.example.springuma.integration.repository;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.uma.example.springuma.model.Medico;
import com.uma.example.springuma.model.RepositoryMedico;

@DataJpaTest
public class RepositoryMedicoIT {
    
    @Autowired
    private RepositoryMedico medicoRepository;

    @Test
    @DisplayName("Comprueba que los datos persisten correctamente al guardar un medico y se puede recuperar por DNI")
    public void givenMedico_whenSave_thenSeGuardaEnLaBD() {
        // Given
        Medico medico = new Medico();
        medico.setNombre("John");
        medico.setDni("1223A");
        medico.setEspecialidad("Cardiología");

        // When
        medicoRepository.save(medico);

        // Then
        Medico foundMedico = medicoRepository.getMedicoByDni("1223A");
        assertNotNull(foundMedico);
        assertEquals("John", foundMedico.getNombre());
    }
}
