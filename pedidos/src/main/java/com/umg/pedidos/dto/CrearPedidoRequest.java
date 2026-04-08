package com.umg.pedidos.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CrearPedidoRequest(
        @NotNull @Min(1) Long idProducto,
        @NotBlank String producto,
        @NotNull @Min(1) Integer cantidad) {
}
