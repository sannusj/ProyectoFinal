package com.solea.web.repositorios;

import com.solea.web.model.PedidoTemp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PedidoTempRepository extends JpaRepository<PedidoTemp, Integer> {

    Optional<PedidoTemp> findByUsuario_Id(int usuarioId);

    void deleteByUsuario_Id(int usuarioId);
}
