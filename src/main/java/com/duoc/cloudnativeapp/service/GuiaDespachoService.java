package com.duoc.cloudnativeapp.service;

import com.duoc.cloudnativeapp.dto.GuiaDespachoRequestDTO;
import com.duoc.cloudnativeapp.dto.GuiaDespachoResponseDTO;
import com.duoc.cloudnativeapp.dto.MensajeResponseDTO;
import com.duoc.cloudnativeapp.model.GuiaDespacho;
import com.duoc.cloudnativeapp.repository.GuiaDespachoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

// Servicio encargado de gestionar el ciclo principal de las guias
@Service
public class GuiaDespachoService {

    private final GuiaDespachoRepository guiaDespachoRepository;
    private final GuiaArchivoService guiaArchivoService;
    private final GuiaS3StorageService guiaS3StorageService;

    public GuiaDespachoService(GuiaDespachoRepository guiaDespachoRepository,
                               GuiaArchivoService guiaArchivoService,
                               GuiaS3StorageService guiaS3StorageService) {
        this.guiaDespachoRepository = guiaDespachoRepository;
        this.guiaArchivoService = guiaArchivoService;
        this.guiaS3StorageService = guiaS3StorageService;
    }

    public List<GuiaDespachoResponseDTO> obtenerGuias(String transportista, LocalDate fecha) {
        List<GuiaDespacho> guias;

        if (StringUtils.hasText(transportista) && fecha != null) {
            guias = guiaDespachoRepository.findByTransportistaContainingIgnoreCaseAndFechaGuiaOrderByIdAsc(
                    transportista,
                    fecha
            );
        } else if (StringUtils.hasText(transportista)) {
            guias = guiaDespachoRepository.findByTransportistaContainingIgnoreCaseOrderByIdAsc(transportista);
        } else if (fecha != null) {
            guias = guiaDespachoRepository.findByFechaGuiaOrderByIdAsc(fecha);
        } else {
            guias = guiaDespachoRepository.findAllByOrderByIdAsc();
        }

        return guias.stream()
                .map(this::convertirAResponseDTO)
                .toList();
    }

    public GuiaDespachoResponseDTO obtenerPorId(Long id) {
        return convertirAResponseDTO(obtenerEntidad(id));
    }

    public GuiaDespachoResponseDTO crear(GuiaDespachoRequestDTO requestDTO) {
        validarNumeroGuiaUnico(requestDTO.getNumeroGuia(), null);

        LocalDateTime ahora = LocalDateTime.now();
        GuiaDespacho guiaDespacho = new GuiaDespacho();
        aplicarCambios(guiaDespacho, requestDTO);
        guiaDespacho.setSubidaS3(Boolean.FALSE);
        guiaDespacho.setFechaCreacion(ahora);
        guiaDespacho.setFechaActualizacion(ahora);

        guiaDespacho = guiaDespachoRepository.save(guiaDespacho);

        // Se genera el PDF despues de guardar para usar el id real en el nombre del archivo.
        Path archivo = guiaArchivoService.regenerarArchivo(guiaDespacho);
        guiaDespacho.setNombreArchivo(guiaArchivoService.obtenerNombreArchivo(guiaDespacho));
        guiaDespacho.setRutaTemporalEfs(archivo.toAbsolutePath().toString());
        guiaDespacho.setFechaActualizacion(LocalDateTime.now());

        return convertirAResponseDTO(guiaDespachoRepository.save(guiaDespacho));
    }

    public GuiaDespachoResponseDTO subirAS3(Long id) {
        GuiaDespacho guiaDespacho = obtenerEntidad(id);
        Path archivo = guiaArchivoService.obtenerArchivoLocalOGenerar(guiaDespacho);

        guiaS3StorageService.subirArchivo(guiaDespacho, archivo);
        guiaDespacho.setNombreArchivo(guiaArchivoService.obtenerNombreArchivo(guiaDespacho));
        guiaDespacho.setRutaTemporalEfs(archivo.toAbsolutePath().toString());
        guiaDespacho.setBucketS3(guiaS3StorageService.getBucketName());
        guiaDespacho.setKeyS3(guiaS3StorageService.construirKey(guiaDespacho));
        guiaDespacho.setSubidaS3(Boolean.TRUE);
        guiaDespacho.setFechaActualizacion(LocalDateTime.now());

        return convertirAResponseDTO(guiaDespachoRepository.save(guiaDespacho));
    }

    public byte[] descargarDesdeS3(Long id, String codigoPermiso) {
        GuiaDespacho guiaDespacho = obtenerEntidad(id);
        validarCodigoPermiso(guiaDespacho, codigoPermiso);
        return guiaS3StorageService.descargarArchivo(guiaDespacho);
    }

    public GuiaDespachoResponseDTO actualizar(Long id, GuiaDespachoRequestDTO requestDTO) {
        GuiaDespacho guiaDespacho = obtenerEntidad(id);
        validarNumeroGuiaUnico(requestDTO.getNumeroGuia(), id);

        String keyAnterior = guiaDespacho.getKeyS3();
        boolean estabaSubida = Boolean.TRUE.equals(guiaDespacho.getSubidaS3());

        aplicarCambios(guiaDespacho, requestDTO);
        guiaDespacho.setFechaActualizacion(LocalDateTime.now());
        guiaDespacho = guiaDespachoRepository.save(guiaDespacho);

        Path archivo = guiaArchivoService.regenerarArchivo(guiaDespacho);
        guiaDespacho.setNombreArchivo(guiaArchivoService.obtenerNombreArchivo(guiaDespacho));
        guiaDespacho.setRutaTemporalEfs(archivo.toAbsolutePath().toString());

        if (estabaSubida) {
            // Si la guia ya estaba en S3, se actualiza el archivo y su referencia actual.
            guiaS3StorageService.subirArchivo(guiaDespacho, archivo);
            String nuevaKey = guiaS3StorageService.construirKey(guiaDespacho);

            if (StringUtils.hasText(keyAnterior) && !keyAnterior.equals(nuevaKey)) {
                guiaS3StorageService.eliminarArchivoPorKey(keyAnterior);
            }

            guiaDespacho.setBucketS3(guiaS3StorageService.getBucketName());
            guiaDespacho.setKeyS3(nuevaKey);
            guiaDespacho.setSubidaS3(Boolean.TRUE);
        }

        guiaDespacho.setFechaActualizacion(LocalDateTime.now());
        return convertirAResponseDTO(guiaDespachoRepository.save(guiaDespacho));
    }

    public MensajeResponseDTO eliminar(Long id) {
        GuiaDespacho guiaDespacho = obtenerEntidad(id);

        if (StringUtils.hasText(guiaDespacho.getKeyS3())) {
            guiaS3StorageService.eliminarArchivoPorKey(guiaDespacho.getKeyS3());
        }

        guiaArchivoService.eliminarArchivoTemporal(guiaDespacho);
        guiaDespachoRepository.delete(guiaDespacho);

        return new MensajeResponseDTO("Guia eliminada correctamente");
    }

    private GuiaDespacho obtenerEntidad(Long id) {
        return guiaDespachoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Guia no encontrada"));
    }

    private void validarNumeroGuiaUnico(String numeroGuia, Long guiaActualId) {
        guiaDespachoRepository.findByNumeroGuiaIgnoreCase(numeroGuia)
                .filter(guiaDespacho -> !guiaDespacho.getId().equals(guiaActualId))
                .ifPresent(guiaDespacho -> {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El numero de guia ya existe");
                });
    }

    private void validarCodigoPermiso(GuiaDespacho guiaDespacho, String codigoPermiso) {
        if (!guiaDespacho.getCodigoPermisoDescarga().equals(codigoPermiso)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "El codigo de permiso no es valido para descargar esta guia"
            );
        }
    }

    private void aplicarCambios(GuiaDespacho guiaDespacho, GuiaDespachoRequestDTO requestDTO) {
        guiaDespacho.setNumeroGuia(requestDTO.getNumeroGuia());
        guiaDespacho.setNumeroPedido(requestDTO.getNumeroPedido());
        guiaDespacho.setTransportista(requestDTO.getTransportista());
        guiaDespacho.setFechaGuia(requestDTO.getFechaGuia());
        guiaDespacho.setOrigen(requestDTO.getOrigen());
        guiaDespacho.setDestino(requestDTO.getDestino());
        guiaDespacho.setDestinatario(requestDTO.getDestinatario());
        guiaDespacho.setDescripcionCarga(requestDTO.getDescripcionCarga());
        guiaDespacho.setEstado(requestDTO.getEstado());
        guiaDespacho.setCodigoPermisoDescarga(requestDTO.getCodigoPermisoDescarga());
    }

    private GuiaDespachoResponseDTO convertirAResponseDTO(GuiaDespacho guiaDespacho) {
        return new GuiaDespachoResponseDTO(
                guiaDespacho.getId(),
                guiaDespacho.getNumeroGuia(),
                guiaDespacho.getNumeroPedido(),
                guiaDespacho.getTransportista(),
                guiaDespacho.getFechaGuia(),
                guiaDespacho.getOrigen(),
                guiaDespacho.getDestino(),
                guiaDespacho.getDestinatario(),
                guiaDespacho.getDescripcionCarga(),
                guiaDespacho.getEstado(),
                guiaDespacho.getRutaTemporalEfs(),
                guiaDespacho.getBucketS3(),
                guiaDespacho.getKeyS3(),
                guiaDespacho.getNombreArchivo(),
                guiaDespacho.getSubidaS3(),
                guiaDespacho.getFechaCreacion(),
                guiaDespacho.getFechaActualizacion()
        );
    }
}
