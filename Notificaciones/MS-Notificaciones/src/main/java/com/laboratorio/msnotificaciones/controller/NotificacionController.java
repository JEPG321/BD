package com.laboratorio.msnotificaciones.controller;

import com.laboratorio.msnotificaciones.dto.NotificacionRequest;
import com.laboratorio.msnotificaciones.dto.NotificacionResponse;
import com.laboratorio.msnotificaciones.service.NotificacionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notificaciones")
public class NotificacionController {

    private final NotificacionService notificacionService;

    public NotificacionController(NotificacionService notificacionService) {
        this.notificacionService = notificacionService;
    }

    @PostMapping
    public ResponseEntity<NotificacionResponse> registrar(@Valid @RequestBody NotificacionRequest request) {
        NotificacionResponse response = notificacionService.registrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
