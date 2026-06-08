package com.duoc.cloudnativeapp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

// Entidad que representa una guia de despacho registrada en la base de datos
@Entity
@Table(name = "GUIAS_DESPACHO")
public class GuiaDespacho {

    @Id
    @SequenceGenerator(
            name = "guias_despacho_seq_generator",
            sequenceName = "GUIAS_DESPACHO_SEQ",
            allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "guias_despacho_seq_generator")
    private Long id;

    @Column(nullable = false, unique = true)
    private String numeroGuia;

    @Column(nullable = false)
    private String numeroPedido;

    @Column(nullable = false)
    private String transportista;

    @Column(nullable = false)
    private LocalDate fechaGuia;

    @Column(nullable = false)
    private String origen;

    @Column(nullable = false)
    private String destino;

    @Column(nullable = false)
    private String destinatario;

    @Column(nullable = false, length = 2000)
    private String descripcionCarga;

    @Column(nullable = false)
    private String estado;

    @Column(nullable = false)
    private String codigoPermisoDescarga;

    private String rutaTemporalEfs;

    private String bucketS3;

    private String keyS3;

    private String nombreArchivo;

    @Column(nullable = false)
    private Boolean subidaS3;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(nullable = false)
    private LocalDateTime fechaActualizacion;

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

    public String getCodigoPermisoDescarga() {
        return codigoPermisoDescarga;
    }

    public void setCodigoPermisoDescarga(String codigoPermisoDescarga) {
        this.codigoPermisoDescarga = codigoPermisoDescarga;
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
