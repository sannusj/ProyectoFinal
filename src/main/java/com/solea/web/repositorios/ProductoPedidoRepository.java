package com.solea.web.repositorios;

import com.solea.web.model.ProductoPedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductoPedidoRepository extends JpaRepository<ProductoPedido, Integer> {
    List<ProductoPedido> findByPedido_Id(int pedidoId);
}
