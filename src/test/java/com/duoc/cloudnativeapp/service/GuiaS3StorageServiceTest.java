package com.duoc.cloudnativeapp.service;

import com.duoc.cloudnativeapp.model.GuiaDespacho;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

// Pruebas para validar respuestas del servicio de S3 ante distintos escenarios
class GuiaS3StorageServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void debeConstruirLaKeyEsperadaConTransportistaNormalizado() {
        GuiaS3StorageService guiaS3StorageService = new GuiaS3StorageService(mock(S3Client.class), "bucket-prueba");

        assertThat(guiaS3StorageService.construirKey(crearGuia()))
                .isEqualTo("2026-06-07/transportes-del-norte/guia-1.pdf");
    }

    @Test
    void debeResponderForbiddenCuandoS3RechazaLaSubidaPorPermisos() throws Exception {
        S3Client s3Client = mock(S3Client.class);
        GuiaS3StorageService guiaS3StorageService = new GuiaS3StorageService(s3Client, "bucket-prueba");
        Path archivo = Files.writeString(tempDir.resolve("guia-1.pdf"), "pdf");

        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenThrow(S3Exception.builder().statusCode(403).message("Forbidden").build());

        assertThatThrownBy(() -> guiaS3StorageService.subirArchivo(crearGuia(), archivo))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(exception -> {
                    ResponseStatusException responseStatusException = (ResponseStatusException) exception;
                    assertThat(responseStatusException.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
                    assertThat(responseStatusException.getReason())
                            .isEqualTo("No hay permisos o credenciales validas para subir la guia a S3");
                });
    }

    @Test
    void debeResponderNotFoundCuandoLaGuiaNoExisteEnS3() {
        S3Client s3Client = mock(S3Client.class);
        GuiaS3StorageService guiaS3StorageService = new GuiaS3StorageService(s3Client, "bucket-prueba");

        when(s3Client.headObject(any(HeadObjectRequest.class)))
                .thenThrow(S3Exception.builder().statusCode(404).build());

        assertThatThrownBy(() -> guiaS3StorageService.descargarArchivo(crearGuia()))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(exception -> {
                    ResponseStatusException responseStatusException = (ResponseStatusException) exception;
                    assertThat(responseStatusException.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(responseStatusException.getReason()).isEqualTo("La guia no existe en S3");
                });
    }

    @Test
    void debeResponderServiceUnavailableCuandoFallaLaConectividadConAws() {
        S3Client s3Client = mock(S3Client.class);
        GuiaS3StorageService guiaS3StorageService = new GuiaS3StorageService(s3Client, "bucket-prueba");

        when(s3Client.headObject(any(HeadObjectRequest.class)))
                .thenThrow(SdkClientException.create("Timeout"));

        assertThatThrownBy(() -> guiaS3StorageService.descargarArchivo(crearGuia()))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(exception -> {
                    ResponseStatusException responseStatusException = (ResponseStatusException) exception;
                    assertThat(responseStatusException.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
                    assertThat(responseStatusException.getReason())
                            .isEqualTo("No fue posible conectarse con AWS S3 para consultar la guia en S3");
                });
    }

    @Test
    void debeInformarCuandoElBucketConfiguradoNoExiste() {
        S3Client s3Client = mock(S3Client.class);
        GuiaS3StorageService guiaS3StorageService = new GuiaS3StorageService(s3Client, "bucket-prueba");

        when(s3Client.headObject(any(HeadObjectRequest.class))).thenThrow(
                S3Exception.builder()
                        .statusCode(404)
                        .awsErrorDetails(AwsErrorDetails.builder().errorCode("NoSuchBucket").build())
                        .build()
        );

        assertThatThrownBy(() -> guiaS3StorageService.descargarArchivo(crearGuia()))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(exception -> {
                    ResponseStatusException responseStatusException = (ResponseStatusException) exception;
                    assertThat(responseStatusException.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
                    assertThat(responseStatusException.getReason())
                            .isEqualTo("El bucket configurado en S3 no existe o no esta disponible");
                });
    }

    private GuiaDespacho crearGuia() {
        GuiaDespacho guiaDespacho = new GuiaDespacho();
        guiaDespacho.setId(1L);
        guiaDespacho.setNumeroGuia("GD-001");
        guiaDespacho.setTransportista("Transportes del Norte");
        guiaDespacho.setFechaGuia(LocalDate.of(2026, 6, 7));
        guiaDespacho.setNombreArchivo("guia-1.pdf");
        return guiaDespacho;
    }
}
