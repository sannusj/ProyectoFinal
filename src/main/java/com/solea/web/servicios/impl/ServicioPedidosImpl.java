package com.solea.web.servicios.impl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.solea.web.datos.serviciosWeb.ResumenPedido;
import com.solea.web.model.Carrito;
import com.solea.web.model.Pedido;
import com.solea.web.model.Pedido.EstadoPedido;
import com.solea.web.model.PedidoTemp;
import com.solea.web.model.ProductoCarrito;
import com.solea.web.model.ProductoPedido;
import com.solea.web.model.Usuario;
import com.solea.web.repositorios.CarritoRepository;
import com.solea.web.repositorios.PedidoRepository;
import com.solea.web.repositorios.PedidoTempRepository;
import com.solea.web.repositorios.ProductoCarritoRepository;
import com.solea.web.repositorios.UsuarioRepository;

@Service
@Transactional
public class ServicioPedidosImpl implements com.solea.web.servicios.ServicioPedidos {

    private final PedidoRepository pedidoRepo;
    private final PedidoTempRepository tempRepo;
    private final UsuarioRepository usuarioRepo;
    private final CarritoRepository carritoRepo;
    private final ProductoCarritoRepository productoCarritoRepo;

    public ServicioPedidosImpl(
            PedidoRepository pedidoRepo,
            PedidoTempRepository tempRepo,
            UsuarioRepository usuarioRepo,
            CarritoRepository carritoRepo,
            ProductoCarritoRepository productoCarritoRepo) {

        this.pedidoRepo = pedidoRepo;
        this.tempRepo = tempRepo;
        this.usuarioRepo = usuarioRepo;
        this.carritoRepo = carritoRepo;
        this.productoCarritoRepo = productoCarritoRepo;
    }

    // ================================================================
    // ADMINISTRACIÓN
    // ================================================================
    @Override
    public List<Pedido> obtenerPedidos() {
        return pedidoRepo.findAll();
    }

    @Override
    public Pedido obtenerPedidoPorId(int idPedido) {
        Pedido pedido = pedidoRepo.findByIdWithProductosAndPrendas(idPedido).orElse(null);
        if (pedido != null && pedido.getProductos() != null) {
            // Forzar inicialización de la colección y de las prendas asociadas
            pedido.getProductos().forEach(pp -> {
                // acceder a campos para inicializar proxies
                if (pp.getPrenda() != null) {
                    pp.getPrenda().getNombre();
                }
            });
        }
        return pedido;
    }

    @Override
    public void actualizarEstadoPedido(int idPedido, String estado) {
        Pedido pedido = pedidoRepo.findById(idPedido).orElse(null);
        if (pedido == null) return;

        try {
            pedido.setEstado(EstadoPedido.valueOf(estado));
            pedidoRepo.save(pedido);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Estado inválido: " + estado);
        }
    }

    // ================================================================
    // PASOS DEL FORMULARIO MULTIPASO (AJAX)
    // ================================================================
    private PedidoTemp obtenerOCrearTemp(int idUsuario) {
        Usuario usuario = usuarioRepo.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return tempRepo.findByUsuario_Id(idUsuario)
                .orElseGet(() -> {
                    PedidoTemp nuevo = new PedidoTemp();
                    nuevo.setUsuario(usuario);
                    return tempRepo.save(nuevo);
                });
    }

    // -------- PASO 1 --------
    @Override
    public void procesarPaso1(String nombre, String direccion, String provincia, int idUsuario) {
        PedidoTemp temp = obtenerOCrearTemp(idUsuario);

        temp.setNombre(nombre);
        temp.setDireccion(direccion);
        temp.setProvincia(provincia);

        tempRepo.save(temp);
    }

    // -------- PASO 2 --------
    @Override
    public void procesarPaso2(String titular, String numero, String tipoTarjeta, int idUsuario) {
        PedidoTemp temp = obtenerOCrearTemp(idUsuario);

        temp.setTitularTarjeta(titular);
        temp.setNumeroTarjeta(numero);
        temp.setTipoTarjeta(tipoTarjeta);

        tempRepo.save(temp);
    }

    // -------- PASO 3 --------
    @Override
    public void procesarPaso3(String regalo, String observaciones, int idUsuario) {
        PedidoTemp temp = obtenerOCrearTemp(idUsuario);

        temp.setParaRegalo(regalo);
        temp.setObservaciones(observaciones);

        tempRepo.save(temp);
    }

    // ================================================================
    // RESUMEN
    // ================================================================
    @Override
    public ResumenPedido obtenerResumenDelPedido(int idUsuario) {
        Optional<PedidoTemp> optTemp = tempRepo.findByUsuario_Id(idUsuario);
        PedidoTemp temp = optTemp.orElse(null);

        if (temp == null) return null;

        ResumenPedido resumen = new ResumenPedido();
        resumen.setNombre(temp.getNombre());
        resumen.setDireccion(temp.getDireccion());
        resumen.setProvincia(temp.getProvincia());
        resumen.setObservaciones(temp.getObservaciones());

        // obtener productos desde el carrito del usuario (si existe)
        Optional<Carrito> optCarrito = carritoRepo.findByUsuario_Id(idUsuario);
        List<ProductoCarrito> items = optCarrito.map(Carrito::getProductosCarrito).orElse(List.of());

        List<ResumenPedido.ProductoResumen> productosResumen = items.stream()
                .map(pc -> {
                    String nombre = pc.getPrenda() != null ? pc.getPrenda().getNombre() : "";
                    int cantidad = pc.getCantidad();
                    double precioUnitario = pc.getPrenda() != null ? pc.getPrenda().getPrecio() : 0.0;
                    Double subtotal = precioUnitario * cantidad;
                    return new ResumenPedido.ProductoResumen(nombre, cantidad, precioUnitario, subtotal);
                })
                .collect(Collectors.toList());

        resumen.setProductos(productosResumen);

        double total = productosResumen.stream().mapToDouble(ResumenPedido.ProductoResumen::getSubtotal).sum();
        resumen.setTotal(total);

        resumen.setRegalo("si".equalsIgnoreCase(temp.getParaRegalo()));

        return resumen;
    }

    // ================================================================
    // CONFIRMAR PEDIDO
    // ================================================================
    @Override
    public void confirmarPedido(int idUsuario) {
        PedidoTemp temp = tempRepo.findByUsuario_Id(idUsuario)
                .orElseThrow(() -> new RuntimeException("No hay pedido temporal para confirmar"));

        Usuario usuario = temp.getUsuario();

        // Crear pedido definitivo
        Pedido pedido = new Pedido();
        pedido.setUsuario(usuario);

        // PASO 1
        pedido.setNombreCompleto(temp.getNombre());
        pedido.setDireccion(temp.getDireccion());
        pedido.setProvincia(temp.getProvincia());

        // PASO 2
        pedido.setTitularTarjeta(temp.getTitularTarjeta());
        pedido.setNumeroTarjeta(temp.getNumeroTarjeta());
        pedido.setTipoTarjeta(temp.getTipoTarjeta());

        // PASO 3
        pedido.setObservaciones(temp.getObservaciones());
        pedido.setRegalo("si".equalsIgnoreCase(temp.getParaRegalo()));

        // AGREGAR PRODUCTOS DEL CARRITO (convertir ProductoCarrito a ProductoPedido)
        Optional<Carrito> optCarrito = carritoRepo.findByUsuario_Id(idUsuario);
        List<ProductoCarrito> items = optCarrito.map(Carrito::getProductosCarrito).orElse(List.of());

        List<ProductoPedido> productosParaPedido = items.stream().map(pc -> {
            ProductoPedido pp = new ProductoPedido();
            pp.setPedido(pedido);
            pp.setPrenda(pc.getPrenda());
            pp.setCantidad(pc.getCantidad());
            pp.setPrecioUnitario(pc.getPrenda() != null ? pc.getPrenda().getPrecio() : 0.0);
            pp.calcularSubtotal();
            return pp;
        }).collect(Collectors.toList());

        pedido.setProductos(productosParaPedido);

        // Calcular total
        double total = productosParaPedido.stream()
                .mapToDouble(p -> p.getSubtotal() != null ? p.getSubtotal() : 0.0)
                .sum();
        pedido.setTotal(total);

        pedido.setEstado(EstadoPedido.NUEVO);

        pedidoRepo.save(pedido);

        // Borrar pedido temporal
        tempRepo.delete(temp);

        // Vaciar carrito (borrar productos del carrito)
        optCarrito.ifPresent(c -> productoCarritoRepo.deleteByCarrito_Id(c.getId()));
    }

    // ================================================================
    // PEDIDOS DEL CLIENTE
    // ================================================================
    @Override
    public List<Pedido> obtenerPedidosDeCliente(int idUsuario) {
        // usar el método definido en PedidoRepository
        return pedidoRepo.findByUsuario_IdOrderByIdDesc(idUsuario);
    }
}
