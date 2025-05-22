package com.uma.example.springuma.integration.controller;

import java.lang.ProcessHandle.Info;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Calendar;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uma.example.springuma.model.Imagen;
import com.uma.example.springuma.model.Informe;
import com.uma.example.springuma.model.RepositoryImagen;

import jakarta.annotation.PostConstruct;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class InformeControllerWebTestClientIT {
    
    @LocalServerPort
    private int port;
    
    private WebTestClient webTestClient;

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private RepositoryImagen repositoryImagen;

    private Informe informe;
    private Imagen imagen;

    @PostConstruct
    public void init() throws Exception {
        webTestClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .responseTimeout(Duration.ofMillis(30000))
                .build();

        // Inicializa el informe y la imagen
        imagen = new Imagen();
        imagen.setNombre("Imagen de prueba");
        imagen.setFecha(Calendar.getInstance());
        imagen.setFile_content(Files.readAllBytes(Paths.get(getClass().getResource("/healthy.png").toURI())));

        imagen = repositoryImagen.save(imagen);
        
        informe = new Informe();
        informe.setId(1);
        informe.setPrediccion("Not cancer");
        informe.setContenido("Contenido de prueba");
        informe.setImagen(imagen);
    }

    @Test
    @DisplayName("Se puede crear un informe y obtenerlo por ID")
    public void givenInforme_whenSave_thenSavedInDB() throws Exception {
        // When
        webTestClient.post()
                .uri("/informe")
                .bodyValue(informe)
                .exchange()
                .expectStatus().isCreated();

        // Then
        webTestClient.get()
                .uri("/informe/{id}", informe.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody(Informe.class)
                .consumeWith(response -> {
                    Informe retrievedInforme = response.getResponseBody();
                    assert retrievedInforme != null;
                    assert retrievedInforme.getId() == informe.getId();
                });
    }

    @Test
    @DisplayName("Se puede eliminar un informe")
    public void givenInforme_whenDelete_thenNotInDb() throws Exception {
        // Meto el informe en la base de datos
        webTestClient.post()
        .uri("/informe")
        .bodyValue(informe)
        .exchange()
        .expectStatus().isCreated();
        
        // When
        webTestClient.delete()
                .uri("/informe/{id}", informe.getId())
                .exchange()
                .expectStatus().isNoContent();

        // Then
        webTestClient.get()
                .uri("/informe/{id}", informe.getId())
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    @DisplayName("Se puede obtener una lista de informes por ID de imagen")
    public void givenInforme_whenGetByImagenId_thenListOfInformes() throws Exception {
        // Creo un informe extra
        Informe informeExtra = new Informe();
        informeExtra.setPrediccion("Cancer");
        informeExtra.setContenido("Contenido de prueba extra");
        informeExtra.setImagen(imagen);

        // Meto el informe en la base de datos
        webTestClient.post()
                .uri("/informe")
                .bodyValue(informe)
                .exchange()
                .expectStatus().isCreated();

        // Obtengo la lista de informes de la imagen
        webTestClient.get()
                .uri("/informe/imagen/{id}", imagen.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Informe.class)
                .consumeWith(response -> {
                    assert response.getResponseBody().size() == 2;
                });
    }

}
