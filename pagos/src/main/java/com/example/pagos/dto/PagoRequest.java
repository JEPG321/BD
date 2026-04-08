package com.example.pagos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PagoRequest(
        @NotNull(message = "pedidoId es obligatorio")
        @Positive(message = "pedidoId debe ser mayor que cero")
        Long pedidoId,

        @NotBlank(message = "producto es obligatorio")
        String producto,

        @NotNull(message = "cantidad es obligatoria")
        @Positive(message = "cantidad debe ser mayor que cero")
        Integer cantidad
) {
}
