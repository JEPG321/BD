package com.umg.pedidos.service;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.umg.pedidos.config.SagaServiceProperties;
import com.umg.pedidos.dto.CrearPedidoRequest;
import com.umg.pedidos.dto.NotificacionRequest;
import com.umg.pedidos.dto.PagoRequest;
import com.umg.pedidos.exception.SagaOrchestratorException;
import com.umg.pedidos.model.EstadoPedido;
import com.umg.pedidos.model.Pedido;
import com.umg.pedidos.repository.PedidoRepository;

@Service
public class PedidoService {

    private static final Logger logger = LoggerFactory.getLogger(PedidoService.class);
    private static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";

    private final PedidoRepository pedidoRepository;
    private final RestTemplate restTemplate;
    private final SagaServiceProperties serviceProperties;

    public PedidoService(PedidoRepository pedidoRepository,
                         RestTemplate restTemplate,
                         SagaServiceProperties serviceProperties) {
        this.pedidoRepository = pedidoRepository;
        this.restTemplate = restTemplate;
        this.serviceProperties = serviceProperties;
    }

    @Transactional(readOnly = true)
    public List<Pedido> obtenerTodos() {
        return pedidoRepository.findAll();
    }

    @Transactional(noRollbackFor = SagaOrchestratorException.class)
    public Pedido crearPedido(CrearPedidoRequest request) {
        logger.info("[PEDIDO] Iniciando creacion de pedido: producto={}, cantidad={}",
                request.producto(), request.cantidad());

        Pedido pedido = new Pedido();
        pedido.setProducto(request.producto());
        pedido.setCantidad(request.cantidad());
        pedido.setEstado(EstadoPedido.PENDIENTE);
        pedido = pedidoRepository.saveAndFlush(pedido);

        logger.info("[PEDIDO] Pedido {} creado en estado PENDIENTE", pedido.getId());

        try {
            String urlReserva = buildInventarioUrl(
                    serviceProperties.getInventario().getBaseUrl(),
                    serviceProperties.getInventario().getReservePath(),
                    request.idProducto(),
                    request.cantidad());
            logger.info("[INVENTARIO] Intentando reservar inventario para pedido {}", pedido.getId());
            logger.info("[INVENTARIO] URL usada para reservar inventario: {}", urlReserva);
            reservarInventario(urlReserva);
            logger.info("[INVENTARIO] Inventario reservado correctamente para pedido {}", pedido.getId());
        } catch (RestClientException ex) {
            logger.error("[INVENTARIO] Error al reservar inventario para pedido {}", pedido.getId(), ex);
            pedido.setEstado(EstadoPedido.CANCELADO);
            pedidoRepository.save(pedido);
            throw buildInventarioException(ex, pedido.getId());
        }

        String idempotencyKey = UUID.randomUUID().toString();
        logger.info("[PAGOS] Idempotency-Key generado para pedido {}: {}", pedido.getId(), idempotencyKey);

        try {
            logger.info("[PAGOS] Procesando pago para pedido {}", pedido.getId());
            procesarPago(pedido, idempotencyKey);
            logger.info("[PAGOS] Pago procesado correctamente para pedido {}", pedido.getId());
        } catch (RestClientException ex) {
            logger.error("[PAGOS] Error al procesar pago para pedido {}", pedido.getId(), ex);
            liberarInventario(request.idProducto(), pedido);
            pedido.setEstado(EstadoPedido.CANCELADO);
            pedidoRepository.save(pedido);
            try {
                logger.info("[NOTIFICACION] Comprobando resultado del pago para pedido {}", pedido.getId());
                logger.info("[NOTIFICACION] Pago rechazado para pedido {}", pedido.getId());
                enviarNotificacion(pedido, EstadoPedido.CANCELADO, "PAGO_RECHAZADO");
                logger.info("[NOTIFICACION] Notificacion de pago rechazado enviada correctamente para pedido {}", pedido.getId());
            } catch (RestClientException notificationEx) {
                logger.error("[NOTIFICACION] Error al enviar notificacion de pago rechazado para pedido {}",
                        pedido.getId(), notificationEx);
            }
            throw buildPagoException(ex, pedido.getId());
        }

        try {
            logger.info("[NOTIFICACION] Comprobando resultado del pago para pedido {}", pedido.getId());
            logger.info("[NOTIFICACION] Pago exitoso para pedido {}", pedido.getId());
            enviarNotificacion(pedido, EstadoPedido.COMPLETADO, "PAGO_EXITOSO");
            logger.info("[NOTIFICACION] Notificacion de pago exitoso enviada correctamente para pedido {}", pedido.getId());
        } catch (RestClientException ex) {
            logger.error("[NOTIFICACION] Error al enviar notificacion para pedido {}", pedido.getId(), ex);
            throw new SagaOrchestratorException(
                    "El pago se proceso para el pedido " + pedido.getId()
                            + ", pero fallo el envio de la notificacion.",
                    HttpStatus.BAD_GATEWAY);
        }

        pedido.setEstado(EstadoPedido.COMPLETADO);
        logger.info("[PEDIDO] Pedido {} marcado como COMPLETADO", pedido.getId());
        return pedidoRepository.save(pedido);
    }

    private void reservarInventario(String urlReserva) {
        restTemplate.exchange(
                urlReserva,
                HttpMethod.POST,
                new HttpEntity<>(new HttpHeaders()),
                Void.class);
    }

    private void liberarInventario(Long idProducto, Pedido pedido) {
        String urlLiberacion = buildInventarioUrl(
                serviceProperties.getInventario().getBaseUrl(),
                serviceProperties.getInventario().getReleasePath(),
                idProducto,
                pedido.getCantidad());

        logger.info("[INVENTARIO] Liberando inventario para pedido {}", pedido.getId());
        logger.info("[INVENTARIO] URL usada para liberar inventario: {}", urlLiberacion);

        try {
            restTemplate.exchange(
                    urlLiberacion,
                    HttpMethod.POST,
                    new HttpEntity<>(new HttpHeaders()),
                    Void.class);
            logger.info("[INVENTARIO] Inventario liberado correctamente para pedido {}", pedido.getId());
        } catch (RestClientException ex) {
            logger.error("[INVENTARIO] Error al liberar inventario para pedido {}", pedido.getId(), ex);
            throw ex;
        }
    }

    private void procesarPago(Pedido pedido, String idempotencyKey) {
        restTemplate.exchange(
                buildUrl(serviceProperties.getPagos().getBaseUrl(), serviceProperties.getPagos().getChargePath()),
                HttpMethod.POST,
                buildJsonEntity(new PagoRequest(pedido.getId(), pedido.getProducto(), pedido.getCantidad()), idempotencyKey),
                Void.class);
    }

    private void enviarNotificacion(Pedido pedido, EstadoPedido estado, String tipo) {
        restTemplate.exchange(
                buildUrl(serviceProperties.getNotificaciones().getBaseUrl(), serviceProperties.getNotificaciones().getSendPath()),
                HttpMethod.POST,
                buildJsonEntity(new NotificacionRequest(
                        pedido.getId(),
                        pedido.getProducto(),
                        pedido.getCantidad(),
                        estado.name(),
                        tipo), null),
                Void.class);
    }

    private HttpEntity<Object> buildJsonEntity(Object body, String idempotencyKey) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (idempotencyKey != null) {
            headers.set(IDEMPOTENCY_KEY_HEADER, idempotencyKey);
        }
        return new HttpEntity<>(body, headers);
    }

    private String buildUrl(String baseUrl, String path) {
        return baseUrl + path;
    }

    private String buildInventarioUrl(String baseUrl, String path, Long idProducto, Integer cantidad) {
        return UriComponentsBuilder.fromUriString(buildUrl(baseUrl, path))
                .queryParam("idProducto", idProducto)
                .queryParam("cantidad", cantidad)
                .toUriString();
    }

    private SagaOrchestratorException buildInventarioException(RestClientException ex, Long pedidoId) {
        if (ex instanceof RestClientResponseException responseException) {
            int statusCode = responseException.getStatusCode().value();
            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                return new SagaOrchestratorException(
                        "No existe el producto en inventario para el pedido " + pedidoId,
                        HttpStatus.NOT_FOUND);
            }
            if (statusCode == HttpStatus.BAD_REQUEST.value()) {
                return new SagaOrchestratorException(
                        "La cantidad solicitada es invalida para el pedido " + pedidoId,
                        HttpStatus.BAD_REQUEST);
            }
            if (statusCode == HttpStatus.CONFLICT.value()) {
                return new SagaOrchestratorException(
                        "No hay stock suficiente para procesar el pedido " + pedidoId,
                        HttpStatus.CONFLICT);
            }
        }

        return new SagaOrchestratorException(
                "No fue posible reservar inventario para el pedido " + pedidoId,
                HttpStatus.BAD_GATEWAY);
    }

    private SagaOrchestratorException buildPagoException(RestClientException ex, Long pedidoId) {
        if (ex instanceof RestClientResponseException responseException) {
            int statusCode = responseException.getStatusCode().value();
            if (statusCode == HttpStatus.BAD_REQUEST.value()) {
                return new SagaOrchestratorException(
                        "Los datos enviados a pagos son invalidos para el pedido " + pedidoId,
                        HttpStatus.BAD_REQUEST);
            }
            if (statusCode == HttpStatus.CONFLICT.value()) {
                return new SagaOrchestratorException(
                        "El pago fue rechazado para el pedido " + pedidoId,
                        HttpStatus.CONFLICT);
            }
        }

        return new SagaOrchestratorException(
                "El pago fallo para el pedido " + pedidoId + ". Saga compensada.",
                HttpStatus.BAD_GATEWAY);
    }
}
