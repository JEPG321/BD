package com.umg.Inventario.controller;

import com.umg.Inventario.entity.Inventario;
import com.umg.Inventario.service.InventarioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventario")
@RequiredArgsConstructor
@Slf4j
public class InventarioController {

    private final InventarioService inventarioService;

    @GetMapping
    public ResponseEntity<List<Inventario>> obtenerInventario() {
        log.info("Solicitud recibida para obtener todo el inventario");
        List<Inventario> inventario = inventarioService.obtenerInventario();
        log.info("Inventario consultado correctamente. Registros encontrados: {}", inventario.size());
        return ResponseEntity.ok(inventario);
    }

    @GetMapping("/{idProducto}")
    public ResponseEntity<?> consultarInventario(@PathVariable Long idProducto) {
        try {
            log.info("Solicitud recibida para consultar inventario del producto {}", idProducto);
            Inventario inventario = inventarioService.consultarPorIdProducto(idProducto);
            log.info("Inventario consultado correctamente para producto {}", idProducto);
            return ResponseEntity.ok(inventario);
        } catch (IllegalArgumentException exception) {
            log.warn("No se pudo consultar inventario para producto {}: {}", idProducto, exception.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("mensaje", exception.getMessage()));
        }
    }

    @PostMapping("/reservar")
    public ResponseEntity<?> reservarStock(
            @RequestParam Long idProducto,
            @RequestParam Integer cantidad) {
        try {
            log.info("Solicitud recibida para reservar stock. Producto: {}, cantidad: {}", idProducto, cantidad);
            Inventario inventario = inventarioService.reservarStock(idProducto, cantidad);
            log.info("Stock reservado correctamente. Producto: {}, stock actual: {}", idProducto, inventario.getStock());
            return ResponseEntity.ok(inventario);
        } catch (IllegalArgumentException exception) {
            HttpStatus status = exception.getMessage().contains("No existe inventario")
                    ? HttpStatus.NOT_FOUND
                    : HttpStatus.BAD_REQUEST;

            log.warn("No se pudo reservar stock para producto {}: {}", idProducto, exception.getMessage());
            return ResponseEntity.status(status)
                    .body(Map.of("mensaje", exception.getMessage()));
        } catch (IllegalStateException exception) {
            log.warn("Conflicto al reservar stock para producto {}: {}", idProducto, exception.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("mensaje", exception.getMessage()));
        }
    }

    @PostMapping("/liberar")
    public ResponseEntity<?> liberarStock(
            @RequestParam Long idProducto,
            @RequestParam Integer cantidad) {
        try {
            log.info("Solicitud recibida para liberar stock. Producto: {}, cantidad: {}", idProducto, cantidad);
            Inventario inventario = inventarioService.liberarStock(idProducto, cantidad);
            log.info("Stock liberado correctamente. Producto: {}, stock actual: {}", idProducto, inventario.getStock());
            return ResponseEntity.ok(inventario);
        } catch (IllegalArgumentException exception) {
            HttpStatus status = exception.getMessage().contains("No existe inventario")
                    ? HttpStatus.NOT_FOUND
                    : HttpStatus.BAD_REQUEST;

            log.warn("No se pudo liberar stock para producto {}: {}", idProducto, exception.getMessage());
            return ResponseEntity.status(status)
                    .body(Map.of("mensaje", exception.getMessage()));
        }
    }
}
