package com.solea.web.servicios;

import com.solea.web.dto.ProductoCarritoDto;
import java.util.List;

public interface ServicioCarrito {

    List<ProductoCarritoDto> obtenerProductosDelCarrito(int usuarioId);

    void agregarProductoAlCarrito(int usuarioId, int prendaId, int cantidad);

    void eliminarProductoDelCarrito(int usuarioId, int prendaId);

    void vaciarCarrito(int usuarioId);

    void actualizarCantidad(int usuarioId, int prendaId, int nuevaCantidad);
}
