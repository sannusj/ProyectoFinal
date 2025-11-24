package com.solea.web.controladores;

import com.solea.web.dto.ProductoCarritoDto;
import com.solea.web.model.Usuario;
import com.solea.web.servicios.ServicioCarrito;
import com.solea.web.servicios.ServicioUsuarios;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/carrito")
public class CarritoController {

    private final ServicioCarrito servicioCarrito;
    private final ServicioUsuarios servicioUsuarios;

    public CarritoController(ServicioCarrito servicioCarrito, ServicioUsuarios servicioUsuarios) {
        this.servicioCarrito = servicioCarrito;
        this.servicioUsuarios = servicioUsuarios;
    }

    // Obtener usuario autenticado
    private Usuario getUsuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;
        String name = auth.getName();
        if (name == null || "anonymousUser".equals(name)) return null;
        return servicioUsuarios.obtenerUserPorEmail(name);
    }

    // Mostrar el carrito
    @GetMapping
    public String verCarrito(Model model) {
        Usuario usuario = getUsuarioActual();
        if (usuario == null) return "redirect:/auth/login";

        List<ProductoCarritoDto> productos = servicioCarrito.obtenerProductosDelCarrito(usuario.getId());
        // Calcular total localmente porque la interfaz del servicio no define calcularTotal
        double total = productos.stream()
                .mapToDouble(p -> (p.getPrecio() != null ? p.getPrecio() : 0.0) * p.getCantidad())
                .sum();

        model.addAttribute("productos", productos);
        model.addAttribute("total", total);

        return "carrito/index"; // Vista Thymeleaf que luego crearemos
    }

    // Agregar producto desde la tienda
    @PostMapping("/agregar")
    public String agregarProducto(
            @RequestParam("prendaId") int prendaId,
            @RequestParam("cantidad") int cantidad) {

        Usuario usuario = getUsuarioActual();
        if (usuario == null) return "redirect:/auth/login";

        if (usuario.getRol() == com.solea.web.model.Rol.ADMIN) {
            throw new AccessDeniedException("Los administradores no pueden usar el carrito");
        }

        // Llamada al método correcto del servicio
        servicioCarrito.agregarProductoAlCarrito(usuario.getId(), prendaId, cantidad);

        return "redirect:/carrito";
    }

    // Cambiar cantidad (AJAX o formularios)
    @PostMapping("/actualizar")
    public String actualizarCantidad(
            @RequestParam("prendaId") int prendaId,
            @RequestParam("cantidad") int cantidad) {

        Usuario usuario = getUsuarioActual();
        if (usuario == null) return "redirect:/auth/login";

        if (usuario.getRol() == com.solea.web.model.Rol.ADMIN) {
            throw new AccessDeniedException("Los administradores no pueden usar el carrito");
        }

        servicioCarrito.actualizarCantidad(usuario.getId(), prendaId, cantidad);

        return "redirect:/carrito";
    }

    // Eliminar producto
    @PostMapping("/eliminar")
    public String eliminarProducto(@RequestParam("prendaId") int prendaId) {

        Usuario usuario = getUsuarioActual();
        if (usuario == null) return "redirect:/auth/login";

        if (usuario.getRol() == com.solea.web.model.Rol.ADMIN) {
            throw new AccessDeniedException("Los administradores no pueden usar el carrito");
        }

        servicioCarrito.eliminarProductoDelCarrito(usuario.getId(), prendaId);

        return "redirect:/carrito";
    }

    // Vaciar carrito
    @PostMapping("/vaciar")
    public String vaciarCarrito() {

        Usuario usuario = getUsuarioActual();
        if (usuario == null) return "redirect:/auth/login";

        if (usuario.getRol() == com.solea.web.model.Rol.ADMIN) {
            throw new AccessDeniedException("Los administradores no pueden usar el carrito");
        }

        servicioCarrito.vaciarCarrito(usuario.getId());

        return "redirect:/carrito";
    }

    // Ir al checkout (pedido paso 1)
    @GetMapping("/checkout")
    public String irAlCheckout() {
        Usuario usuario = getUsuarioActual();
        if (usuario == null) return "redirect:/auth/login";

        if (usuario.getRol() == com.solea.web.model.Rol.ADMIN) {
            throw new AccessDeniedException("Los administradores no pueden usar el carrito");
        }
        return "redirect:/pedido/paso1";
    }
}
