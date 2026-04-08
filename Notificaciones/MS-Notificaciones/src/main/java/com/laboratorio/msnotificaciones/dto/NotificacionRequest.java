package com.laboratorio.msnotificaciones.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class NotificacionRequest {

    @NotNull(message = "pedidoId es obligatorio")
    private Long pedidoId;

    @NotBlank(message = "producto es obligatorio")
    private String producto;

    @NotNull(message = "cantidad es obligatoria")
    @Min(value = 1, message = "cantidad debe ser mayor o igual a 1")
    private Integer cantidad;

    @NotBlank(message = "estado es obligatorio")
    private String estado;

    public Long getPedidoId() {
        return pedidoId;
    }

    public void setPedidoId(Long pedidoId) {
        this.pedidoId = pedidoId;
    }

    public String getProducto() {
        return producto;
    }

    public void setProducto(String producto) {
        this.producto = producto;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
