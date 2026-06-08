package com.duoc.cloudnativeapp.controller;

import com.duoc.cloudnativeapp.repository.GuiaDespachoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Comparator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Pruebas de integracion para validar el flujo principal del controlador de guias
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GuiaDespachoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private GuiaDespachoRepository guiaDespachoRepository;

    @Value("${app.efs.guias-path}")
    private String guiasPath;

    @BeforeEach
    void setUp() throws IOException {
        guiaDespachoRepository.deleteAll();
        limpiarGuias();
    }

    @Test
    void debeCrearGuiaYGenerarArchivoPdfTemporal() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/guias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new GuiaPayload(
                                        "GD-001",
                                        "PED-001",
                                        "Transportes del Norte",
                                        LocalDate.of(2026, 6, 7),
                                        "Santiago",
                                        "Antofagasta",
                                        "Cliente Demo",
                                        "Equipos electricos",
                                        "GENERADA",
                                        "PERMISO-123"
                                )
                        )))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.numeroGuia").value("GD-001"))
                .andExpect(jsonPath("$.numeroPedido").value("PED-001"))
                .andReturn();

        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        Long id = response.get("id").longValue();
        Path archivoGenerado = Path.of(guiasPath).resolve("guia-" + id + ".pdf");

        assertThat(Files.exists(archivoGenerado)).isTrue();
        assertThat(response.get("nombreArchivo").asText()).isEqualTo("guia-" + id + ".pdf");
        assertThat(response.get("rutaTemporalEfs").asText()).endsWith("guia-" + id + ".pdf");
    }

    @Test
    void debeConsultarGuiasPorTransportistaYFecha() throws Exception {
        crearGuia("GD-001", "Transportes del Norte", LocalDate.of(2026, 6, 7));
        crearGuia("GD-002", "Transportes del Sur", LocalDate.of(2026, 6, 8));

        mockMvc.perform(get("/api/guias")
                        .param("transportista", "Transportes del Norte")
                        .param("fecha", "2026-06-07"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].numeroGuia").value("GD-001"))
                .andExpect(jsonPath("$[0].numeroPedido").value("PED-001"))
                .andExpect(jsonPath("$[0].transportista").value("Transportes del Norte"));
    }

    @Test
    void debeResponder403CuandoElCodigoPermisoEsIncorrecto() throws Exception {
        Long id = crearGuia("GD-003", "Transportes del Norte", LocalDate.of(2026, 6, 7));

        mockMvc.perform(get("/api/guias/{id}/descargar-s3", id)
                        .param("codigoPermiso", "PERMISO-ERRONEO"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message")
                        .value("El codigo de permiso no es valido para descargar esta guia"));
    }

    private Long crearGuia(String numeroGuia, String transportista, LocalDate fechaGuia) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/guias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new GuiaPayload(
                                        numeroGuia,
                                        "PED-001",
                                        transportista,
                                        fechaGuia,
                                        "Santiago",
                                        "Antofagasta",
                                        "Cliente Demo",
                                        "Equipos electricos",
                                        "GENERADA",
                                        "PERMISO-123"
                                )
                        )))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").longValue();
    }

    private void limpiarGuias() throws IOException {
        Path directorio = Path.of(guiasPath);
        if (!Files.exists(directorio)) {
            return;
        }

        try (var paths = Files.walk(directorio)) {
            paths.sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException exception) {
                            throw new RuntimeException(exception);
                        }
                    });
        }
    }

    private record GuiaPayload(String numeroGuia,
                               String numeroPedido,
                               String transportista,
                               LocalDate fechaGuia,
                               String origen,
                               String destino,
                               String destinatario,
                               String descripcionCarga,
                               String estado,
                               String codigoPermisoDescarga) {
    }
}
