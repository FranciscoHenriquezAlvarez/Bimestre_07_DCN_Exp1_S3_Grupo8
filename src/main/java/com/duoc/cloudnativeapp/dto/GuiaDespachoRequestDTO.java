package com.duoc.cloudnativeapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

// DTO que recibe los datos necesarios para crear o actualizar una guia
public class GuiaDespachoRequestDTO {

    @NotBlank(message = "El numero de guia es obligatorio")
    private String numeroGuia;

    @NotBlank(message = "El numero de pedido es obligatorio")
    private String numeroPedido;

    @NotBlank(message = "El transportista es obligatorio")
    private String transportista;

    @NotNull(message = "La fecha de la guia es obligatoria")
    private LocalDate fechaGuia;

    @NotBlank(message = "El origen es obligatorio")
    private String origen;

    @NotBlank(message = "El destino es obligatorio")
    private String destino;

    @NotBlank(message = "El destinatario es obligatorio")
    private String destinatario;

    @NotBlank(message = "La descripcion de carga es obligatoria")
    private String descripcionCarga;

    @NotBlank(message = "El estado es obligatorio")
    private String estado;

    @NotBlank(message = "El codigo de permiso de descarga es obligatorio")
    private String codigoPermisoDescarga;

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
}
