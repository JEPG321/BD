package com.example.pagos.controller;

import com.example.pagos.dto.PagoRequest;
import com.example.pagos.dto.PagoResponse;
import com.example.pagos.service.PagoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@Validated
public class PagoController {

    private final PagoService pagoService;

    public PagoController(PagoService pagoService) {
        this.pagoService = pagoService;
    }

    @PostMapping("/pagos")
    public ResponseEntity<PagoResponse> crearPago(
            @RequestHeader(name = "Idempotency-Key") @NotBlank(message = "Idempotency-Key es obligatorio")
            String idempotencyKey,
            @Valid @RequestBody PagoRequest pagoRequest
    ) {
        PagoResponse response = pagoService.procesarPago(idempotencyKey, pagoRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/pagos")
    public ResponseEntity<List<PagoResponse>> obtenerHistorialPagos() {
        List<PagoResponse> historial = pagoService.obtenerIdempotencyStore()
                .values()
                .stream()
                .map(PagoService.StoredPayment::response)
                .toList();
        return ResponseEntity.ok(historial);
    }
}
