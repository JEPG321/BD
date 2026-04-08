package com.umg.pedidos.dto;

public record InventarioRequest(
        Long pedidoId,
        String producto,
        Integer cantidad) {
}
