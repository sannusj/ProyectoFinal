package com.solea.web.repositorios;

import com.solea.web.model.ProductoCarrito;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductoCarritoRepository extends JpaRepository<ProductoCarrito, Integer> {

    List<ProductoCarrito> findByCarrito_Id(int carritoId);

    Optional<ProductoCarrito> findByCarrito_IdAndPrenda_Id(int carritoId, int prendaId);

    void deleteByCarrito_IdAndPrenda_Id(int carritoId, int prendaId);

    void deleteByCarrito_Id(int carritoId); // IMPORTANTE PARA VACIAR CARRITO
}
