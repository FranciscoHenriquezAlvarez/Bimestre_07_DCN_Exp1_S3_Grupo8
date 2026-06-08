package com.duoc.cloudnativeapp.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

// DTO que expone la informacion de una guia en las respuestas de la API
public class GuiaDespachoResponseDTO {

    private Long id;
    private String numeroGuia;
    private String numeroPedido;
    private String transportista;
    private LocalDate fechaGuia;
    private String origen;
    private String destino;
    private String destinatario;
    private String descripcionCarga;
    private String estado;
    private String rutaTemporalEfs;
    private String bucketS3;
    private String keyS3;
    private String nombreArchivo;
    private Boolean subidaS3;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    public GuiaDespachoResponseDTO() {
    }

    public GuiaDespachoResponseDTO(Long id,
                                   String numeroGuia,
                                   String numeroPedido,
                                   String transportista,
                                   LocalDate fechaGuia,
                                   String origen,
                                   String destino,
                                   String destinatario,
                                   String descripcionCarga,
                                   String estado,
                                   String rutaTemporalEfs,
                                   String bucketS3,
                                   String keyS3,
                                   String nombreArchivo,
                                   Boolean subidaS3,
                                   LocalDateTime fechaCreacion,
                                   LocalDateTime fechaActualizacion) {
        this.id = id;
        this.numeroGuia = numeroGuia;
        this.numeroPedido = numeroPedido;
        this.transportista = transportista;
        this.fechaGuia = fechaGuia;
        this.origen = origen;
        this.destino = destino;
        this.destinatario = destinatario;
        this.descripcionCarga = descripcionCarga;
        this.estado = estado;
        this.rutaTemporalEfs = rutaTemporalEfs;
        this.bucketS3 = bucketS3;
        this.keyS3 = keyS3;
        this.nombreArchivo = nombreArchivo;
        this.subidaS3 = subidaS3;
        this.fechaCreacion = fechaCreacion;
        this.fechaActualizacion = fechaActualizacion;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumeroGuia() {
        return numeroGuia;
    }

    public void setNumeroGuia(String numeroGuia) {
        this.numeroGuia = numeroGuia;
    }

    public String getNumeroPedido() {
        return numeroPedido;
    }

    public void setNumeroPedido(String numeroPedido) {
        this.numeroPedido = numeroPedido;
    }

    public String getTransportista() {
        return transportista;
    }

    public void setTransportista(String transportista) {
        this.transportista = transportista;
    }

    public LocalDate getFechaGuia() {
        return fechaGuia;
    }

    public void setFechaGuia(LocalDate fechaGuia) {
        this.fechaGuia = fechaGuia;
    }

    public String getOrigen() {
        return origen;
    }

    public void setOrigen(String origen) {
        this.origen = origen;
    }

    public String getDestino() {
        return destino;
    }

    public void setDestino(String destino) {
        this.destino = destino;
    }

    public String getDestinatario() {
        return destinatario;
    }

    public void setDestinatario(String destinatario) {
        this.destinatario = destinatario;
    }

    public String getDescripcionCarga() {
        return descripcionCarga;
    }

    public void setDescripcionCarga(String descripcionCarga) {
        this.descripcionCarga = descripcionCarga;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getRutaTemporalEfs() {
        return rutaTemporalEfs;
    }

    public void setRutaTemporalEfs(String rutaTemporalEfs) {
        this.rutaTemporalEfs = rutaTemporalEfs;
    }

    public String getBucketS3() {
        return bucketS3;
    }

    public void setBucketS3(String bucketS3) {
        this.bucketS3 = bucketS3;
    }

    public String getKeyS3() {
        return keyS3;
    }

    public void setKeyS3(String keyS3) {
        this.keyS3 = keyS3;
    }

    public String getNombreArchivo() {
        return nombreArchivo;
    }

    public void setNombreArchivo(String nombreArchivo) {
        this.nombreArchivo = nombreArchivo;
    }

    public Boolean getSubidaS3() {
        return subidaS3;
    }

    public void setSubidaS3(Boolean subidaS3) {
        this.subidaS3 = subidaS3;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }
}
