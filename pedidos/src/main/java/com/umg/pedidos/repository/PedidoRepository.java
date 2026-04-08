package com.umg.pedidos.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.umg.pedidos.model.Pedido;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
}
