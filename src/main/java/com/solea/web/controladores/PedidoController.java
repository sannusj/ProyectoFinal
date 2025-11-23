package com.solea.web.controladores;

import com.solea.web.datos.serviciosWeb.ResumenPedido;
import com.solea.web.dto.ProductoCarritoDto;
import com.solea.web.model.Usuario;
import com.solea.web.servicios.ServicioCarrito;
import com.solea.web.servicios.ServicioPedidos;
import com.solea.web.servicios.ServicioUsuarios;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/pedido")
public class PedidoController {

    private final ServicioCarrito servicioCarrito;
    private final ServicioPedidos servicioPedidos;
    private final ServicioUsuarios servicioUsuarios;

    public PedidoController(ServicioCarrito servicioCarrito,
                             ServicioPedidos servicioPedidos,
                             ServicioUsuarios servicioUsuarios) {
        this.servicioCarrito = servicioCarrito;
        this.servicioPedidos = servicioPedidos;
        this.servicioUsuarios = servicioUsuarios;
    }

    private Usuario getUsuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;
        Object principal = auth.getPrincipal();
        // Evitar devolver el usuario 'anonymousUser' o principal nulo
        if (principal == null) return null;
        String name = auth.getName();
        if (name == null || "anonymousUser".equals(name)) return null;
        return servicioUsuarios.obtenerUserPorEmail(name);
    }

    // Paso 1: datos de envío
    @GetMapping("/paso1")
    public String mostrarPaso1(Model model) {
        Usuario u = getUsuarioActual();
        if (u == null) return "redirect:/auth/login";
        model.addAttribute("usuario", u);
        return "pedido/paso1";
    }

    @PostMapping("/paso1")
    public String procesarPaso1(@RequestParam String nombre,
                                @RequestParam String direccion,
                                @RequestParam String provincia) {
        Usuario u = getUsuarioActual();
        if (u == null) return "redirect:/auth/login";

        servicioPedidos.procesarPaso1(nombre, direccion, provincia, u.getId());
        return "redirect:/pedido/paso2";
    }

    // Paso 2: datos de tarjeta
    @GetMapping("/paso2")
    public String mostrarPaso2(Model model) {
        Usuario u = getUsuarioActual();
        if (u == null) return "redirect:/auth/login";
        model.addAttribute("usuario", u);
        return "pedido/paso2";
    }

    @PostMapping("/paso2")
    public String procesarPaso2(@RequestParam String titular,
                                @RequestParam String numero,
                                @RequestParam String tipoTarjeta) {
        Usuario u = getUsuarioActual();
        if (u == null) return "redirect:/auth/login";

        servicioPedidos.procesarPaso2(titular, numero, tipoTarjeta, u.getId());
        return "redirect:/pedido/paso3";
    }

    // Paso 3: regalo / observaciones
    @GetMapping("/paso3")
    public String mostrarPaso3(Model model) {
        Usuario u = getUsuarioActual();
        if (u == null) return "redirect:/auth/login";
        model.addAttribute("usuario", u);
        return "pedido/paso3";
    }

    @PostMapping("/paso3")
    public String procesarPaso3(@RequestParam(name = "regalo", required = false) String regalo,
                                @RequestParam(name = "observaciones", required = false) String observaciones) {
        Usuario u = getUsuarioActual();
        if (u == null) return "redirect:/auth/login";

        // Normalizar valor del checkbox/parametro regalo a "si" o "no"
        String regaloNorm = "no";
        if (regalo != null) {
            String r = regalo.trim().toLowerCase();
            if (r.equals("on") || r.equals("true") || r.equals("si") || r.equals("1")) {
                regaloNorm = "si";
            }
        }

        servicioPedidos.procesarPaso3(regaloNorm, observaciones != null ? observaciones : "", u.getId());
        return "redirect:/pedido/resumen";
    }

    // Resumen
    @GetMapping("/resumen")
    public String resumen(Model model) {
        Usuario u = getUsuarioActual();
        if (u == null) return "redirect:/auth/login";

        // resumen con los datos temporales
        ResumenPedido resumen = servicioPedidos.obtenerResumenDelPedido(u.getId());

        // productos del carrito
        List<ProductoCarritoDto> productos = servicioCarrito.obtenerProductosDelCarrito(u.getId());
        double total = productos.stream()
                .mapToDouble(p -> (p.getPrecio() != null ? p.getPrecio() : 0.0) * p.getCantidad())
                .sum();

        if (resumen == null) {
            // construir un resumen mínimo si no existe PedidoTemp
            resumen = new ResumenPedido();
            resumen.setNombre(u.getNombre());
            resumen.setDireccion("");
            resumen.setProvincia("");
            resumen.setProductos(List.of());
            resumen.setTotal(total);
            resumen.setRegalo(false);
        }

        model.addAttribute("resumen", resumen);
        model.addAttribute("productos", productos);
        model.addAttribute("total", total);
        return "pedido/resumen";
    }

    // Confirmar pedido
    @PostMapping("/confirmar")
    public String confirmarPedido(Model model) {
        Usuario u = getUsuarioActual();
        if (u == null) return "redirect:/auth/login";

        try {
            // confirmarPedido ya crea el Pedido definitivo usando PedidoTemp y vacía el carrito
            servicioPedidos.confirmarPedido(u.getId());
        } catch (RuntimeException ex) {
            // en caso de error, volver al resumen con mensaje
            model.addAttribute("error", ex.getMessage());
            return "pedido/resumen";
        }

        // recuperar pedidos del usuario para mostrar confirmación (opcional)
        model.addAttribute("pedidos", servicioPedidos.obtenerPedidosDeCliente(u.getId()));
        return "pedido/confirmado";
    }

    // Endpoint para generar PDF (no implementado todavía)
    @GetMapping("/pdf")
    public String descargarPdf() {
        // Actualmente no implementado; redirigir al resumen
        return "redirect:/pedido/resumen?error=pdf_no_implementado";
    }
}
