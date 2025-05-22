package com.uma.example.springuma.integration.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Calendar;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.uma.example.springuma.model.Imagen;
import com.uma.example.springuma.model.Paciente;
import com.uma.example.springuma.model.RepositoryImagen;
import com.uma.example.springuma.model.RepositoryPaciente;

@DataJpaTest
public class RepositoryImagenIT {
    
    @Autowired
    private RepositoryImagen repositoryImagen;

    @Autowired
    private RepositoryPaciente repositoryPaciente;

    @Test
    @DisplayName("Comprueba que los datos persisten correctamente al guardar una imagen y se puede recuperar por ID")
    public void givenImagen_whenSave_thenSeGuardaEnLaBD() {
        // Given
        Imagen imagen = new Imagen();
        imagen.setNombre("Imagen de prueba");
        imagen.setFecha(Calendar.getInstance());
        byte[] fileContent = {1, 2, 3}; // Simulando contenido de archivo
        imagen.setFile_content(fileContent);
        
        // When
        repositoryImagen.save(imagen);
        
        // Then
        Imagen foundImagen = repositoryImagen.getReferenceById(imagen.getId());
        assertNotNull(foundImagen);
        assertEquals("Imagen de prueba", foundImagen.getNombre());
    }

    @Test
    @DisplayName("Comprueba que se puede eliminar una imagen")
    public void givenImagen_whenDelete_thenSeEliminaCorrectamente() {
        // Given
        Imagen imagen = new Imagen();
        imagen.setNombre("Imagen de prueba");
        imagen.setFecha(Calendar.getInstance());
        byte[] fileContent = {1, 2, 3}; // Simulando contenido de archivo
        imagen.setFile_content(fileContent);
        
        // When
        repositoryImagen.save(imagen);
        repositoryImagen.delete(imagen);
        
        // Then
        assertNull(repositoryImagen.findById(imagen.getId()).orElse(null));
    }

    @Test
    @DisplayName("Comprueba que se puede actualizar una imagen")
    public void givenImagen_whenUpdate_thenSeActualizaCorrectamente() {
        // Given
        Imagen imagen = new Imagen();
        imagen.setNombre("Imagen de prueba");
        imagen.setFecha(Calendar.getInstance());
        byte[] fileContent = {1, 2, 3}; // Simulando contenido de archivo
        imagen.setFile_content(fileContent);
        
        // When
        repositoryImagen.save(imagen);
        
        // Update
        imagen.setNombre("Imagen actualizada");
        repositoryImagen.save(imagen);
        
        // Then
        Imagen foundImagen = repositoryImagen.getReferenceById(imagen.getId());
        assertNotNull(foundImagen);
        assertEquals("Imagen actualizada", foundImagen.getNombre());
    }

    @Test
    @DisplayName("Se pueden obtener todas las imágenes de un paciente")
    public void givenImagenes_whenGetByPacienteId_thenSeRecuperanCorrectamente() {
        // Given
        Paciente paciente = new Paciente();
        paciente = repositoryPaciente.save(paciente);
        
        Imagen imagen1 = new Imagen();
        imagen1.setNombre("Imagen 1");
        imagen1.setFecha(Calendar.getInstance());
        byte[] fileContent1 = {1, 2, 3}; // Simulando contenido de archivo
        imagen1.setFile_content(fileContent1);
        imagen1.setPaciente(paciente);
        
        Imagen imagen2 = new Imagen();
        imagen2.setNombre("Imagen 2");
        imagen2.setFecha(Calendar.getInstance());
        byte[] fileContent2 = {4, 5, 6}; // Simulando contenido de archivo
        imagen2.setFile_content(fileContent2);
        imagen2.setPaciente(paciente);
        
        // When
        repositoryImagen.save(imagen1);
        repositoryImagen.save(imagen2);
        
        // Then
        assertEquals(2, repositoryImagen.getByPacienteId(paciente.getId()).size());
    }
}
