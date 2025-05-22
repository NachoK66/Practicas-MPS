package com.uma.example.springuma.integration.controller;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Arrays;
import java.util.Calendar;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uma.example.springuma.model.Imagen;
import com.uma.example.springuma.model.Paciente;
import com.uma.example.springuma.model.RepositoryPaciente;

import jakarta.annotation.PostConstruct;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ImagenControllerWebTestClientIT {
    
    @LocalServerPort
    private int port;
    
    private WebTestClient webTestClient;

    @Autowired
    private RepositoryPaciente repositoryPaciente;

    @Autowired
    private ObjectMapper objectMapper;

    private Imagen imagen;
    private Paciente paciente;

    @PostConstruct
    public void init() throws Exception {
        webTestClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .responseTimeout(Duration.ofMillis(30000))
                .build();

        // Inicializa el paciente con datos de prueba
        paciente = new Paciente();
        paciente.setId(1);
        paciente.setNombre("John Doe");
        paciente.setDni("12345678A");

        // Inicializa el paciente en la base de datos
        repositoryPaciente.save(paciente);

        // Inicializa la imagen
        imagen = new Imagen();
        imagen.setId(1);
        imagen.setNombre("healthy.png");
        imagen.setFile_content(Files.readAllBytes(Paths.get(getClass().getResource("/healthy.png").toURI())));
        imagen.setFecha(Calendar.getInstance());
        imagen.setPaciente(paciente);
    }

    @Test
    @DisplayName("Se puede subir una imagen y obtener su información")
    public void givenImage_whenUploaded_canGetInfo() throws JsonProcessingException {
        // Setup para el multipart
        String pacienteJson = objectMapper.writeValueAsString(paciente);
        MultipartBodyBuilder multipartBodyBuilder = new MultipartBodyBuilder();
        multipartBodyBuilder.part("image", new ByteArrayResource(imagen.getFile_content()) {
            @Override
            public String getFilename() {
                return "healthy.png";
            }
        }).contentType(MediaType.IMAGE_PNG);
        multipartBodyBuilder.part("paciente", pacienteJson, MediaType.APPLICATION_JSON);
        
        // Sube la imagen
        webTestClient.post()
                .uri("/imagen")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(multipartBodyBuilder.build()))
                .exchange()
                .expectStatus().isOk();

        // Obtiene la información de la imagen
        webTestClient.get()
                .uri("/imagen/info/" + imagen.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody(Imagen.class)
                .consumeWith(response -> {
                    Imagen responseBody = response.getResponseBody();
                    assert responseBody != null;
                    assert responseBody.getId() == imagen.getId();
                    assert responseBody.getNombre().equals(imagen.getNombre());

                });
    }

    @Test
    @DisplayName("Se puede subir una imagen y descargarla")
    public void givenImage_whenUploaded_canDownload() throws JsonProcessingException {
        // Setup para el multipart
        String pacienteJson = objectMapper.writeValueAsString(paciente);
        MultipartBodyBuilder multipartBodyBuilder = new MultipartBodyBuilder();
        multipartBodyBuilder.part("image", new ByteArrayResource(imagen.getFile_content()) {
            @Override
            public String getFilename() {
                return "healthy.png";
            }
        }).contentType(MediaType.IMAGE_PNG);
        multipartBodyBuilder.part("paciente", pacienteJson, MediaType.APPLICATION_JSON);
        
        // Sube la imagen
        webTestClient.post()
                .uri("/imagen")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(multipartBodyBuilder.build()))
                .exchange()
                .expectStatus().isOk();

        // Descarga la imagen
        webTestClient.get()
                .uri("/imagen/{id}", imagen.getId())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("image/png")
                .expectBody(byte[].class)
                .consumeWith(response -> {
                    byte[] imageData = response.getResponseBody();
                    assert imageData != null;
                    assert imageData.length == imagen.getFile_content().length;
                    assert Arrays.equals(imageData, imagen.getFile_content());
                });
    }

    @Test
    @DisplayName("Se puede eliminar una imagen")
    public void givenImage_whenOnDelete_getsDeleted() throws JsonProcessingException {
        // Setup para el multipart
        String pacienteJson = objectMapper.writeValueAsString(paciente);
        MultipartBodyBuilder multipartBodyBuilder = new MultipartBodyBuilder();
        multipartBodyBuilder.part("image", new ByteArrayResource(imagen.getFile_content()) {
            @Override
            public String getFilename() {
                return "healthy.png";
            }
        }).contentType(MediaType.IMAGE_PNG);
        multipartBodyBuilder.part("paciente", pacienteJson, MediaType.APPLICATION_JSON);
        
        // Sube la imagen
        webTestClient.post()
                .uri("/imagen")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(multipartBodyBuilder.build()))
                .exchange()
                .expectStatus().isOk();

        // Elimina la imagen
        webTestClient.delete()
                .uri("/imagen/{id}", imagen.getId())
                .exchange()
                .expectStatus().isNoContent();

        // Al intentar buscarla, no se puede obtener la imagen
        webTestClient.get()
                .uri("/imagen/info/{id}", imagen.getId())
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    @DisplayName("Se puede hacer una predicción de la imagen")
    public void givenImage_canDoPredictions() throws JsonProcessingException {
        // Setup para el multipart
        String pacienteJson = objectMapper.writeValueAsString(paciente);
        MultipartBodyBuilder multipartBodyBuilder = new MultipartBodyBuilder();
        multipartBodyBuilder.part("image", new ByteArrayResource(imagen.getFile_content()) {
            @Override
            public String getFilename() {
                return "healthy.png";
            }
        }).contentType(MediaType.IMAGE_PNG);
        multipartBodyBuilder.part("paciente", pacienteJson, MediaType.APPLICATION_JSON);
        
        // Sube la imagen
        webTestClient.post()
                .uri("/imagen")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(multipartBodyBuilder.build()))
                .exchange()
                .expectStatus().isOk();

        // Realiza la predicción
        webTestClient.get()
                .uri("/imagen/predict/{id}", imagen.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(response -> {
                    String responseBody = response.getResponseBody();
                    assert responseBody != null;
                    assert responseBody.contains("status");
                    assert responseBody.contains("score");
                });
    }

    @Test
    @DisplayName("Se puede obtener todas las imagenes de un paciente")
    public void givenImage_whenOnGetAllImages_getsAllImages() throws Exception {
        // Sube otra imagen más
        Imagen imagen2 = new Imagen();
        imagen2.setId(2);
        imagen2.setNombre("not_healthy.png");
        imagen2.setFile_content(Files.readAllBytes(Paths.get(getClass().getResource("/no_healthty.png").toURI())));
        imagen2.setFecha(Calendar.getInstance());
        imagen2.setPaciente(paciente);

        // Setup para el multipart
        String pacienteJson = objectMapper.writeValueAsString(paciente);
        MultipartBodyBuilder multipartBodyBuilder = new MultipartBodyBuilder();
        multipartBodyBuilder.part("image", new ByteArrayResource(imagen.getFile_content()) {
            @Override
            public String getFilename() {
                return "healthy.png";
            }
        }).contentType(MediaType.IMAGE_PNG);
        multipartBodyBuilder.part("paciente", pacienteJson, MediaType.APPLICATION_JSON);
        
        // Sube la imagen
        webTestClient.post()
                .uri("/imagen")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(multipartBodyBuilder.build()))
                .exchange()
                .expectStatus().isOk();

        // Sube la segunda imagen
        multipartBodyBuilder = new MultipartBodyBuilder();
        multipartBodyBuilder.part("image", new ByteArrayResource(imagen2.getFile_content()) {
            @Override
            public String getFilename() {
                return "not_healthy.png";
            }
        }).contentType(MediaType.IMAGE_PNG);
        multipartBodyBuilder.part("paciente", pacienteJson, MediaType.APPLICATION_JSON);

        webTestClient.post()
                .uri("/imagen")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(multipartBodyBuilder.build()))
                .exchange()
                .expectStatus().isOk();

        // Obtiene todas las imágenes del paciente
        webTestClient.get()
                .uri("/imagen/paciente/{id}", paciente.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Imagen.class)
                .consumeWith(response -> {
                    assert response.getResponseBody() != null;
                    assert response.getResponseBody().size() == 2;
                    assert response.getResponseBody().get(0).getId() == imagen.getId();
                    assert response.getResponseBody().get(0).getPaciente().getId() == paciente.getId();
                    assert response.getResponseBody().get(1).getId() == imagen2.getId();
                    assert response.getResponseBody().get(1).getPaciente().getId() == paciente.getId();
                });
    }

}
