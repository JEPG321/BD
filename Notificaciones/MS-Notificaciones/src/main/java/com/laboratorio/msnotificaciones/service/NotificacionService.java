package com.laboratorio.msnotificaciones.service;

import com.laboratorio.msnotificaciones.dto.NotificacionRequest;
import com.laboratorio.msnotificaciones.dto.NotificacionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class NotificacionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificacionService.class);

    private final List<NotificacionResponse> historial = new ArrayList<>();

    public NotificacionResponse registrar(NotificacionRequest request) {
        LOGGER.info("pedido recibido: pedidoId={}", request.getPedidoId());

        NotificacionResponse response = new NotificacionResponse(
                request.getPedidoId(),
                "ENVIADA",
                "Notificacion enviada correctamente"
        );

        historial.add(response);

        LOGGER.info("notificacion procesada: pedidoId={}, estado={}", request.getPedidoId(), response.getEstado());
        return response;
    }

    public List<NotificacionResponse> listarTodas() {
        return Collections.unmodifiableList(historial);
    }
}
