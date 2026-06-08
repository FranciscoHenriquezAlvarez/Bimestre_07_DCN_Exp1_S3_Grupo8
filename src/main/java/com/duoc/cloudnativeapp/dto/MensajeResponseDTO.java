package com.duoc.cloudnativeapp.dto;

// DTO simple para devolver mensajes breves desde la API
public class MensajeResponseDTO {

    private String mensaje;

    public MensajeResponseDTO() {
    }

    public MensajeResponseDTO(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}
