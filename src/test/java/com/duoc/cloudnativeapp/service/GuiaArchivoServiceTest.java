package com.duoc.cloudnativeapp.service;

import com.duoc.cloudnativeapp.model.GuiaDespacho;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

// Prueba que valida la generacion del PDF de una guia
class GuiaArchivoServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void debeGenerarArchivoPdfTemporalDeLaGuia() throws Exception {
        GuiaDespacho guiaDespacho = crearGuia();
        GuiaArchivoService guiaArchivoService = new GuiaArchivoService(tempDir.toString());

        Path archivo = guiaArchivoService.regenerarArchivo(guiaDespacho);

        assertThat(Files.exists(archivo)).isTrue();
        assertThat(archivo.getFileName().toString()).isEqualTo("guia-1.pdf");
        assertThat(Files.readAllBytes(archivo)).startsWith("%PDF".getBytes());

        try (PDDocument document = PDDocument.load(archivo.toFile())) {
            String contenido = new PDFTextStripper().getText(document);
            assertThat(contenido)
                    .contains("Guia de Despacho")
                    .contains("Numero de guia: GD-001")
                    .contains("Numero de pedido: PED-001")
                    .contains("Transportista: Transportes del Norte")
                    .contains("Descripcion de carga: Equipos electricos");
        }
    }

    private GuiaDespacho crearGuia() {
        GuiaDespacho guiaDespacho = new GuiaDespacho();
        guiaDespacho.setId(1L);
        guiaDespacho.setNumeroGuia("GD-001");
        guiaDespacho.setNumeroPedido("PED-001");
        guiaDespacho.setTransportista("Transportes del Norte");
        guiaDespacho.setFechaGuia(LocalDate.of(2026, 6, 7));
        guiaDespacho.setOrigen("Santiago");
        guiaDespacho.setDestino("Antofagasta");
        guiaDespacho.setDestinatario("Cliente Demo");
        guiaDespacho.setDescripcionCarga("Equipos electricos");
        guiaDespacho.setEstado("GENERADA");
        return guiaDespacho;
    }
}
