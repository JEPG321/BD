package com.example.pagos.service;

import com.example.pagos.dto.PagoRequest;
import com.example.pagos.dto.PagoResponse;
import com.example.pagos.exception.IdempotencyConflictException;
import com.example.pagos.exception.PaymentRejectedException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class PagoService {

    private static final String ESTADO_PAGADO = "PAGADO";
    private static final String ESTADO_RECHAZADO = "RECHAZADO";
    private static final String MENSAJE_OK = "Pago procesado correctamente";
    private static final String MENSAJE_RECHAZADO = "Pago rechazado, no se pudo procesar";

    private final Map<String, StoredPayment> idempotencyStore = new ConcurrentHashMap<>();

    public PagoResponse procesarPago(String idempotencyKey, PagoRequest pagoRequest) {
        if (!StringUtils.hasText(idempotencyKey)) {
            throw new IllegalArgumentException("El header Idempotency-Key es obligatorio");
        }

        StoredPayment storedPayment = idempotencyStore.compute(idempotencyKey, (key, existing) -> {
            if (existing == null) {
                boolean esExitoso = ThreadLocalRandom.current().nextDouble() < 0.90;
                PagoResponse response = esExitoso ?
                        new PagoResponse(pagoRequest.pedidoId(), ESTADO_PAGADO, MENSAJE_OK) :
                        new PagoResponse(pagoRequest.pedidoId(), ESTADO_RECHAZADO, MENSAJE_RECHAZADO);

                return new StoredPayment(pagoRequest, response, esExitoso);
            }

            if (!existing.request().equals(pagoRequest)) {
                throw new IdempotencyConflictException("El Idempotency-Key ya fue usado con un payload distinto");
            }

            return existing;
        });

        if (!storedPayment.exitoso()) {
            throw new PaymentRejectedException(storedPayment.response());
        }

        return storedPayment.response();
    }

    public Map<String, StoredPayment> obtenerIdempotencyStore() {
        return Map.copyOf(idempotencyStore);
    }

    public record StoredPayment(PagoRequest request, PagoResponse response, boolean exitoso) {
    }
}

