package com.duoc.cloudnativeapp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;

// Configuracion encargada de crear el cliente de AWS S3
@Configuration
public class S3Config {

    @Bean
    public S3Client s3Client(@Value("${aws.region:us-east-1}") String region,
                             @Value("${aws.access-key:}") String accessKey,
                             @Value("${aws.secret-key:}") String secretKey,
                             @Value("${aws.session-token:}") String sessionToken) {
        String regionLimpia = limpiarValor(region);
        String accessKeyLimpia = limpiarValor(accessKey);
        String secretKeyLimpia = limpiarValor(secretKey);
        String sessionTokenLimpio = limpiarValor(sessionToken);

        // Si no se informa una region, se usa us-east-1 como valor por defecto.
        S3ClientBuilder builder = S3Client.builder()
                .region(Region.of(StringUtils.hasText(regionLimpia) ? regionLimpia : "us-east-1"));

        builder.credentialsProvider(crearCredenciales(accessKeyLimpia, secretKeyLimpia, sessionTokenLimpio));
        return builder.build();
    }

    private AwsCredentialsProvider crearCredenciales(String accessKey, String secretKey, String sessionToken) {
        // Si no hay credenciales configuradas, se intenta usar el proveedor por defecto del entorno.
        if (!StringUtils.hasText(accessKey) || !StringUtils.hasText(secretKey)) {
            return DefaultCredentialsProvider.create();
        }

        // AWS Academy entrega credenciales temporales, por eso se considera el session token.
        if (StringUtils.hasText(sessionToken)) {
            return StaticCredentialsProvider.create(
                    AwsSessionCredentials.create(accessKey, secretKey, sessionToken)
            );
        }

        return StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey));
    }

    private String limpiarValor(String valor) {
        return valor != null ? valor.trim() : null;
    }
}
