package com.example.pagos.exception;

import com.example.pagos.dto.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.time.OffsetDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({
            MissingRequestHeaderException.class,
            MethodArgumentNotValidException.class,
            ConstraintViolationException.class,
            HttpMessageNotReadableException.class,
            IllegalArgumentException.class
    })
    public ResponseEntity<ErrorResponse> handleBadRequest(Exception ex) {
        String message = resolveMessage(ex);
        return buildError(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(IdempotencyConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(IdempotencyConflictException ex) {
        return buildError(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(com.example.pagos.exception.PaymentRejectedException.class)
    public ResponseEntity<ErrorResponse> handlePaymentRejected(com.example.pagos.exception.PaymentRejectedException ex) {
        return buildError(HttpStatus.CONFLICT, ex.getPagoResponse().mensaje());
    }

    private ResponseEntity<ErrorResponse> buildError(HttpStatus status, String message) {
        ErrorResponse body = new ErrorResponse(
                OffsetDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message
        );
        return ResponseEntity.status(status).body(body);
    }

    private String resolveMessage(Exception ex) {
        if (ex instanceof MethodArgumentNotValidException manv) {
            return manv.getBindingResult()
                    .getFieldErrors()
                    .stream()
                    .map(this::formatFieldError)
                    .collect(Collectors.joining("; "));
        }

        if (ex instanceof ConstraintViolationException cve) {
            return cve.getConstraintViolations()
                    .stream()
                    .map(v -> v.getMessage())
                    .collect(Collectors.joining("; "));
        }

        if (ex instanceof MissingRequestHeaderException mrh) {
            return "El header " + mrh.getHeaderName() + " es obligatorio";
        }

        if (ex instanceof HttpMessageNotReadableException) {
            return "El body de la solicitud es invalido o incompleto";
        }

        return ex.getMessage();
    }

    private String formatFieldError(FieldError fieldError) {
        return fieldError.getField() + ": " + fieldError.getDefaultMessage();
    }
}
