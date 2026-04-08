package com.umg.pedidos.exception;

import org.springframework.http.HttpStatus;

public class SagaOrchestratorException extends RuntimeException {

    private final HttpStatus status;

    public SagaOrchestratorException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
