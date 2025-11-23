package com.solea.web.servicios.impl;

import com.solea.web.dto.ProductoCarritoDto;
import com.solea.web.model.Carrito;
import com.solea.web.model.Prenda;
import com.solea.web.model.ProductoCarrito;
import com.solea.web.model.Usuario;
import com.solea.web.repositorios.CarritoRepository;
import com.solea.web.repositorios.PrendaRepository;
import com.solea.web.repositorios.ProductoCarritoRepository;
import com.solea.web.repositorios.UsuarioRepository;
import com.solea.web.servicios.ServicioCarrito;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ServicioCarritoImpl implements ServicioCarrito {

    private final CarritoRepository carritoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PrendaRepository prendaRepository;
    private final ProductoCarritoRepository productoCarritoRepository;

    // ---------------------------------------------------------
    // LISTAR PRODUCTOS DEL CARRITO
    // ---------------------------------------------------------
    @Override
    public List<ProductoCarritoDto> obtenerProductosDelCarrito(int usuarioId) {

        if (!usuarioRepository.existsById(usuarioId)) {
            throw new RuntimeException("Usuario no encontrado");
        }

        Carrito carrito = carritoRepository.findByUsuario_Id(usuarioId)
                .orElse(null);

        if (carrito == null)
            return List.of();

        return productoCarritoRepository.findByCarrito_Id(carrito.getId())
                .stream()
                .map(pc -> new ProductoCarritoDto(
                        pc.getPrenda().getId(),
                        pc.getPrenda().getNombre(),
                        pc.getPrenda().getPrecio(),
                        pc.getCantidad(),
                        pc.getPrenda().getImagenPrenda()
                ))
                .collect(Collectors.toList());
    }

    // ---------------------------------------------------------
    // AGREGAR PRODUCTO
    // ---------------------------------------------------------
    @Override
    public void agregarProductoAlCarrito(int usuarioId, int prendaId, int cantidad) {

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Prenda prenda = prendaRepository.findById(prendaId)
                .orElseThrow(() -> new RuntimeException("Prenda no encontrada"));

        Carrito carrito = carritoRepository.findByUsuario_Id(usuarioId)
                .orElseGet(() -> carritoRepository.save(new Carrito(usuario)));

        var existente = productoCarritoRepository
                .findByCarrito_IdAndPrenda_Id(carrito.getId(), prendaId);

        if (existente.isPresent()) {
            ProductoCarrito pc = existente.get();
            pc.setCantidad(pc.getCantidad() + cantidad);
        } else {
            ProductoCarrito pc = new ProductoCarrito(prenda, carrito, cantidad);
            carrito.addProducto(pc);
            productoCarritoRepository.save(pc); // IMPORTANTE
        }
    }

    // ---------------------------------------------------------
    // ELIMINAR PRODUCTO
    // ---------------------------------------------------------
    @Override
    public void eliminarProductoDelCarrito(int usuarioId, int prendaId) {

        Carrito carrito = carritoRepository.findByUsuario_Id(usuarioId)
                .orElse(null);

        if (carrito != null) {
            productoCarritoRepository.deleteByCarrito_IdAndPrenda_Id(carrito.getId(), prendaId);
        }
    }

    // ---------------------------------------------------------
    // ACTUALIZAR CANTIDAD
    // ---------------------------------------------------------
    @Override
    public void actualizarCantidad(int usuarioId, int prendaId, int cantidad) {

        Carrito carrito = carritoRepository.findByUsuario_Id(usuarioId)
                .orElseThrow(() -> new RuntimeException("Carrito no encontrado"));

        ProductoCarrito item = productoCarritoRepository
                .findByCarrito_IdAndPrenda_Id(carrito.getId(), prendaId)
                .orElseThrow(() -> new RuntimeException("La prenda no está en el carrito"));

        item.setCantidad(Math.max(1, cantidad));
    }

    // ---------------------------------------------------------
    // VACIAR CARRITO
    // ---------------------------------------------------------
    @Override
    public void vaciarCarrito(int usuarioId) {

        Carrito carrito = carritoRepository.findByUsuario_Id(usuarioId)
                .orElse(null);

        if (carrito != null) {
            productoCarritoRepository.deleteByCarrito_Id(carrito.getId());
            carrito.getProductosCarrito().clear();
        }
    }
}
