package com.solea.web.repositorios;

import com.solea.web.model.Pedido;
import com.solea.web.model.Pedido.EstadoPedido;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Integer> {

    List<Pedido> findByUsuario_IdOrderByIdDesc(int usuarioId);

    @Query("select p from Pedido p where p.estado = :estado and p.usuario.id = :usuarioId")
    List<Pedido> findActivosPorUsuario(
            @Param("estado") EstadoPedido estado,
            @Param("usuarioId") int usuarioId
    );

    Page<Pedido> findAllByOrderByIdDesc(Pageable pageable);
}
