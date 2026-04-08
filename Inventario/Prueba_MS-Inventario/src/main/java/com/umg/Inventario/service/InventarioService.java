package com.umg.Inventario.service;

import com.umg.Inventario.entity.Inventario;
import com.umg.Inventario.repository.InventarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventarioService {

    private final InventarioRepository inventarioRepository;

    @Transactional(readOnly = true)
    public List<Inventario> obtenerInventario() {
        log.info("Consultando registros de inventario en base de datos");
        return inventarioRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Inventario consultarPorIdProducto(Long idProducto) {
        log.info("Buscando inventario para producto {}", idProducto);
        return inventarioRepository.findById(idProducto)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe inventario para el producto con id: " + idProducto));
    }

    @Transactional
    public Inventario reservarStock(Long idProducto, Integer cantidad) {
        validarCantidad(cantidad);

        Inventario inventario = consultarPorIdProducto(idProducto);

        if (inventario.getStock() < cantidad) {
            log.warn("Stock insuficiente para producto {}. Stock actual: {}, solicitado: {}",
                    idProducto, inventario.getStock(), cantidad);
            throw new IllegalStateException(
                    "Stock insuficiente para el producto con id: " + idProducto);
        }

        inventario.setStock(inventario.getStock() - cantidad);
        log.info("Reservando stock para producto {}. Nuevo stock: {}", idProducto, inventario.getStock());
        return inventarioRepository.save(inventario);
    }

    @Transactional
    public Inventario liberarStock(Long idProducto, Integer cantidad) {
        validarCantidad(cantidad);

        Inventario inventario = consultarPorIdProducto(idProducto);
        inventario.setStock(inventario.getStock() + cantidad);
        log.info("Liberando stock para producto {}. Nuevo stock: {}", idProducto, inventario.getStock());

        return inventarioRepository.save(inventario);
    }

    private void validarCantidad(Integer cantidad) {
        if (cantidad == null || cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que 0");
        }
    }
}
