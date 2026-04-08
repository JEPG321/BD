package com.umg.pedidos.dto;

public record PagoRequest(
        Long pedidoId,
        String producto,
        Integer cantidad) {
}
