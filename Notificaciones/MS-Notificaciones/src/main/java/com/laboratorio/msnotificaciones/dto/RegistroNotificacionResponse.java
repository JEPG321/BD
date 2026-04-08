package com.laboratorio.msnotificaciones.dto;

import java.time.LocalDateTime;

public class RegistroNotificacionResponse {

    private Long id;
    private Long idPedido;
    private String mensaje;
    private LocalDateTime fecha;

    public RegistroNotificacionResponse() {
    }

    public RegistroNotificacionResponse(Long id, Long idPedido, String mensaje, LocalDateTime fecha) {
        this.id = id;
        this.idPedido = idPedido;
        this.mensaje = mensaje;
        this.fecha = fecha;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(Long idPedido) {
        this.idPedido = idPedido;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }
}