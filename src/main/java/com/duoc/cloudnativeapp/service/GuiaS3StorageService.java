package com.duoc.cloudnativeapp.service;

import com.duoc.cloudnativeapp.model.GuiaDespacho;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.nio.file.Path;
import java.text.Normalizer;
import java.util.Locale;

// Servicio que administra la subida y descarga de guias en AWS S3
@Service
public class GuiaS3StorageService {

    private static final Logger logger = LoggerFactory.getLogger(GuiaS3StorageService.class);

    private final S3Client s3Client;
    private final String bucketName;

    public GuiaS3StorageService(S3Client s3Client,
                                @Value("${aws.s3.bucket-name:}") String bucketName) {
        this.s3Client = s3Client;
        this.bucketName = limpiarValor(bucketName);
    }

    public void subirArchivo(GuiaDespacho guiaDespacho, Path archivo) {
        validarBucketConfigurado();
        String key = construirKey(guiaDespacho);

        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(key)
                            .contentType("application/pdf")
                            .build(),
                    RequestBody.fromFile(archivo)
            );
        } catch (S3Exception exception) {
            throw crearErrorS3("subir la guia a S3", exception);
        } catch (SdkClientException exception) {
            throw crearErrorConectividadS3("subir la guia a S3");
        }
    }

    public byte[] descargarArchivo(GuiaDespacho guiaDespacho) {
        validarBucketConfigurado();
        String key = resolverKey(guiaDespacho);

        if (!existeArchivo(key)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "La guia no existe en S3");
        }

        try {
            ResponseBytes<GetObjectResponse> archivo = s3Client.getObjectAsBytes(
                    GetObjectRequest.builder()
                            .bucket(bucketName)
                            .key(key)
                            .build()
            );
            return archivo.asByteArray();
        } catch (NoSuchKeyException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "La guia no existe en S3");
        } catch (S3Exception exception) {
            throw crearErrorS3("descargar la guia desde S3", exception);
        } catch (SdkClientException exception) {
            throw crearErrorConectividadS3("descargar la guia desde S3");
        }
    }

    public void eliminarArchivo(GuiaDespacho guiaDespacho) {
        eliminarArchivoPorKey(resolverKey(guiaDespacho));
    }

    public void eliminarArchivoPorKey(String key) {
        validarBucketConfigurado();
        if (!StringUtils.hasText(key) || !existeArchivo(key)) {
            return;
        }

        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build());
        } catch (S3Exception exception) {
            throw crearErrorS3("eliminar la guia desde S3", exception);
        } catch (SdkClientException exception) {
            throw crearErrorConectividadS3("eliminar la guia desde S3");
        }
    }

    public String construirKey(GuiaDespacho guiaDespacho) {
        return guiaDespacho.getFechaGuia()
                + "/"
                + normalizarTransportista(guiaDespacho.getTransportista())
                + "/"
                + obtenerNombreArchivo(guiaDespacho);
    }

    public String normalizarTransportista(String transportista) {
        String textoBase = StringUtils.hasText(transportista) ? transportista : "transportista";
        String normalizado = Normalizer.normalize(textoBase, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");

        return StringUtils.hasText(normalizado) ? normalizado : "transportista";
    }

    public String getBucketName() {
        return bucketName;
    }

    private String resolverKey(GuiaDespacho guiaDespacho) {
        return StringUtils.hasText(guiaDespacho.getKeyS3()) ? guiaDespacho.getKeyS3() : construirKey(guiaDespacho);
    }

    private String obtenerNombreArchivo(GuiaDespacho guiaDespacho) {
        return StringUtils.hasText(guiaDespacho.getNombreArchivo())
                ? guiaDespacho.getNombreArchivo()
                : "guia-" + guiaDespacho.getId() + ".pdf";
    }

    private boolean existeArchivo(String key) {
        try {
            s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build());
            return true;
        } catch (S3Exception exception) {
            // Se valida este caso para informar cuando el archivo no existe sin devolver un error generico.
            if (esArchivoNoEncontrado(exception)) {
                return false;
            }

            throw crearErrorS3("consultar la guia en S3", exception);
        } catch (SdkClientException exception) {
            throw crearErrorConectividadS3("consultar la guia en S3");
        }
    }

    private void validarBucketConfigurado() {
        if (!StringUtils.hasText(bucketName)) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "El bucket S3 no esta configurado");
        }
    }

    // Se separa este mapeo para distinguir permisos, bucket incorrecto y errores informados por AWS S3.
    private ResponseStatusException crearErrorS3(String accion, S3Exception exception) {
        // Se registra el detalle tecnico de AWS para facilitar el diagnostico sin exponer credenciales.
        logger.error(
                "Error S3 en accion='{}', statusCode={}, errorCode='{}', errorMessage='{}'",
                accion,
                exception.statusCode(),
                obtenerCodigoError(exception),
                obtenerMensajeError(exception)
        );

        if (exception.statusCode() == 403) {
            return new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No hay permisos o credenciales validas para " + accion
            );
        }

        if (esBucketNoEncontrado(exception)) {
            return new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "El bucket configurado en S3 no existe o no esta disponible"
            );
        }

        if (esArchivoNoEncontrado(exception)) {
            return new ResponseStatusException(HttpStatus.NOT_FOUND, "La guia no existe en S3");
        }

        return new ResponseStatusException(
                HttpStatus.BAD_GATEWAY,
                "AWS S3 informo un error al intentar " + accion
        );
    }

    // Se captura este caso para informar fallas de conectividad con AWS sin exponer detalles internos.
    private ResponseStatusException crearErrorConectividadS3(String accion) {
        return new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "No fue posible conectarse con AWS S3 para " + accion
        );
    }

    private boolean esArchivoNoEncontrado(S3Exception exception) {
        return exception.statusCode() == 404 && !esBucketNoEncontrado(exception);
    }

    private boolean esBucketNoEncontrado(S3Exception exception) {
        return exception.statusCode() == 404 && "NoSuchBucket".equals(obtenerCodigoError(exception));
    }

    private String obtenerCodigoError(S3Exception exception) {
        return exception.awsErrorDetails() != null ? exception.awsErrorDetails().errorCode() : null;
    }

    private String obtenerMensajeError(S3Exception exception) {
        return exception.awsErrorDetails() != null ? exception.awsErrorDetails().errorMessage() : exception.getMessage();
    }

    private String limpiarValor(String valor) {
        return valor != null ? valor.trim() : null;
    }
}
