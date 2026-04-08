package com.example.pagos.dto;

public record PagoResponse(
        Long pedidoId,
        String estado,
        String mensaje
) {
}
