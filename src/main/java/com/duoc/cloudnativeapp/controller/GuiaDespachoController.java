package com.duoc.cloudnativeapp.controller;

import com.duoc.cloudnativeapp.dto.GuiaDespachoRequestDTO;
import com.duoc.cloudnativeapp.dto.GuiaDespachoResponseDTO;
import com.duoc.cloudnativeapp.dto.MensajeResponseDTO;
import com.duoc.cloudnativeapp.service.GuiaDespachoService;
import jakarta.validation.Valid;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

// Controlador REST para gestionar las guias de despacho
@RestController
@RequestMapping("/api/guias")
public class GuiaDespachoController {

    private final GuiaDespachoService guiaDespachoService;

    public GuiaDespachoController(GuiaDespachoService guiaDespachoService) {
        this.guiaDespachoService = guiaDespachoService;
    }

    @GetMapping
    public ResponseEntity<List<GuiaDespachoResponseDTO>> listar(
            @RequestParam(required = false) String transportista,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ResponseEntity.ok(guiaDespachoService.obtenerGuias(transportista, fecha));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GuiaDespachoResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(guiaDespachoService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<GuiaDespachoResponseDTO> crear(@Valid @RequestBody GuiaDespachoRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(guiaDespachoService.crear(requestDTO));
    }

    @PostMapping("/{id}/subir-s3")
    public ResponseEntity<GuiaDespachoResponseDTO> subirAS3(@PathVariable Long id) {
        return ResponseEntity.ok(guiaDespachoService.subirAS3(id));
    }

    @GetMapping("/{id}/descargar-s3")
    public ResponseEntity<Resource> descargarDesdeS3(@PathVariable Long id, @RequestParam String codigoPermiso) {
        GuiaDespachoResponseDTO guiaDespacho = guiaDespachoService.obtenerPorId(id);
        byte[] contenido = guiaDespachoService.descargarDesdeS3(id, codigoPermiso);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + guiaDespacho.getNombreArchivo() + "\"")
                .body(new ByteArrayResource(contenido));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GuiaDespachoResponseDTO> actualizar(@PathVariable Long id,
                                                              @Valid @RequestBody GuiaDespachoRequestDTO requestDTO) {
        return ResponseEntity.ok(guiaDespachoService.actualizar(id, requestDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MensajeResponseDTO> eliminar(@PathVariable Long id) {
        return ResponseEntity.ok(guiaDespachoService.eliminar(id));
    }
}
