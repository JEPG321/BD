package com.laboratorio.msnotificaciones.dto;

public class NotificacionResponse {

    private Long pedidoId;
    private String estado;
    private String mensaje;

    public NotificacionResponse() {
    }

    public NotificacionResponse(Long pedidoId, String estado, String mensaje) {
        this.pedidoId = pedidoId;
        this.estado = estado;
        this.mensaje = mensaje;
    }

    public Long getPedidoId() {
        return pedidoId;
    }

    public void setPedidoId(Long pedidoId) {
        this.pedidoId = pedidoId;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}