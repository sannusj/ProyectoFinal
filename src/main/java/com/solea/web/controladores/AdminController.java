package com.solea.web.controladores;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.solea.web.model.Rol;
import com.solea.web.model.Usuario;
import com.solea.web.servicios.ServicioUsuarios;
import com.solea.web.servicios.ServicioPedidos;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private ServicioUsuarios servicioUsuarios;

    @Autowired
    private ServicioPedidos servicioPedidos;

    // -----------------------------------------------------------
    // PANEL PRINCIPAL
    // -----------------------------------------------------------
    @GetMapping("")
    public String adminHome() {
        return "admin/index"; // admin/index.html
    }

    // -----------------------------------------------------------
    // USUARIOS
    // -----------------------------------------------------------
    @GetMapping("/usuarios")
    public String listarUsuarios(Model modelo) {
        modelo.addAttribute("usuarios", servicioUsuarios.obtenerUsuarios());
        return "admin/usuarios"; // admin/usuarios.html
    }

    @GetMapping("/usuarios/{id}")
    public String detalleUsuario(@PathVariable int id, Model modelo) {
        Usuario u = servicioUsuarios.obtenerUserPorId(id);
        if (u == null) {
            modelo.addAttribute("error", "El usuario no existe.");
            return "admin/usuarios";
        }

        modelo.addAttribute("usuario", u);
        return "admin/usuario-detalle"; // admin/usuario-detalle.html
    }

    @PostMapping("/usuarios/{id}/cambiar-rol")
    public String cambiarRol(
            @PathVariable Integer id,
            @RequestParam Rol nuevoRol,
            Model modelo) {

        // Si quieren impedir eliminar al último ADMIN:
        if (nuevoRol != Rol.ADMIN && servicioUsuarios.esUltimoAdmin(id)) {
            modelo.addAttribute("error", "No puedes quitar el rol ADMIN al último administrador.");
            return "redirect:/admin/usuarios";
        }

        servicioUsuarios.cambiarRolUsuario(id, nuevoRol);
        return "redirect:/admin/usuarios";
    }

    // -----------------------------------------------------------
    // PEDIDOS
    // -----------------------------------------------------------
    @GetMapping("/pedidos")
    public String listarPedidos(Model modelo) {
        modelo.addAttribute("pedidos", servicioPedidos.obtenerPedidos());
        return "admin/pedidos"; // admin/pedidos.html
    }

    @GetMapping("/pedidos/{id}")
    public String verPedido(@PathVariable int id, Model modelo) {
        var pedido = servicioPedidos.obtenerPedidoPorId(id);

        if (pedido == null) {
            modelo.addAttribute("error", "El pedido no existe.");
            return "admin/pedidos";
        }

        modelo.addAttribute("pedido", pedido);
        return "admin/pedido-detalle"; // admin/pedido-detalle.html
    }

    @PostMapping("/pedidos/{id}/estado")
    public String cambiarEstadoPedido(
            @PathVariable int id,
            @RequestParam String estado) {

        servicioPedidos.actualizarEstadoPedido(id, estado);
        return "redirect:/admin/pedidos/" + id;
    }

    // -----------------------------------------------------------
    // PERFIL ADMIN (compatibilidad: redirigir a /perfil unificado)
    // -----------------------------------------------------------
    @GetMapping("/perfil")
    public String perfilAdminRedirect() {
        return "redirect:/perfil";
    }

}
