package com.example.pagos.exception;

import com.example.pagos.dto.PagoResponse;

public class PaymentRejectedException extends RuntimeException {

    private final PagoResponse pagoResponse;

    public PaymentRejectedException(PagoResponse pagoResponse) {
        super("Pago rechazado: " + pagoResponse.mensaje());
        this.pagoResponse = pagoResponse;
    }

    public PagoResponse getPagoResponse() {
        return pagoResponse;
    }
}
