package com.duoc.cloudnativeapp.service;

import com.duoc.cloudnativeapp.model.GuiaDespacho;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

// Servicio que genera y administra los archivos PDF de las guias
@Service
public class GuiaArchivoService {

    private final Path guiasPath;

    public GuiaArchivoService(@Value("${app.efs.guias-path:archivos/guias}") String guiasPath) {
        this.guiasPath = Paths.get(guiasPath);
    }

    public Path obtenerArchivoLocalOGenerar(GuiaDespacho guiaDespacho) {
        Path archivo = obtenerRutaArchivo(guiaDespacho);
        if (Files.exists(archivo)) {
            return archivo;
        }

        return regenerarArchivo(guiaDespacho);
    }

    public Path regenerarArchivo(GuiaDespacho guiaDespacho) {
        Path archivo = obtenerRutaArchivo(guiaDespacho);

        try {
            // Se usa una ruta configurable para permitir pruebas locales y reutilizar un EFS ya montado en EC2.
            Files.createDirectories(guiasPath);
            generarPdf(guiaDespacho, archivo);
            return archivo;
        } catch (IOException exception) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "No fue posible generar el archivo PDF de la guia"
            );
        }
    }

    public void eliminarArchivoTemporal(GuiaDespacho guiaDespacho) {
        try {
            Files.deleteIfExists(obtenerRutaArchivo(guiaDespacho));
        } catch (IOException exception) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "No fue posible eliminar el archivo temporal de la guia"
            );
        }
    }

    public Path obtenerRutaArchivo(GuiaDespacho guiaDespacho) {
        return guiasPath.resolve(obtenerNombreArchivo(guiaDespacho));
    }

    public String obtenerNombreArchivo(GuiaDespacho guiaDespacho) {
        return "guia-" + guiaDespacho.getId() + ".pdf";
    }

    private void generarPdf(GuiaDespacho guiaDespacho, Path archivo) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                // Se arma un contenido simple para mantener la guia legible y facil de descargar.
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 18);
                contentStream.setLeading(18f);
                contentStream.newLineAtOffset(50, 750);
                contentStream.showText("Guia de Despacho");
                contentStream.newLine();
                contentStream.newLine();

                contentStream.setFont(PDType1Font.HELVETICA, 12);
                for (String linea : construirLineas(guiaDespacho)) {
                    contentStream.showText(limpiarTexto(linea));
                    contentStream.newLine();
                }
                contentStream.endText();
            }

            document.save(archivo.toFile());
        }
    }

    private List<String> construirLineas(GuiaDespacho guiaDespacho) {
        return List.of(
                "Numero de guia: " + guiaDespacho.getNumeroGuia(),
                "Numero de pedido: " + guiaDespacho.getNumeroPedido(),
                "Transportista: " + guiaDespacho.getTransportista(),
                "Fecha: " + guiaDespacho.getFechaGuia(),
                "Origen: " + guiaDespacho.getOrigen(),
                "Destino: " + guiaDespacho.getDestino(),
                "Destinatario: " + guiaDespacho.getDestinatario(),
                "Descripcion de carga: " + guiaDespacho.getDescripcionCarga(),
                "Estado: " + guiaDespacho.getEstado()
        );
    }

    private String limpiarTexto(String texto) {
        if (!StringUtils.hasText(texto)) {
            return "";
        }

        return texto.replace("\n", " ").replace("\r", " ").trim();
    }
}
