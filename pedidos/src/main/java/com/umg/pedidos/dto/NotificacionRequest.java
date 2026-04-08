package com.umg.pedidos.dto;

public record NotificacionRequest(
        Long pedidoId,
        String producto,
        Integer cantidad,
        String estado,
        String tipo) {
}
