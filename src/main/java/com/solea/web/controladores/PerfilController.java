package com.solea.web.controladores;

import com.solea.web.model.Usuario;
import com.solea.web.servicios.ServicioPedidos;
import com.solea.web.servicios.ServicioUsuarios;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/perfil")
public class PerfilController {

    private final ServicioUsuarios servicioUsuarios;
    private final ServicioPedidos servicioPedidos;

    public PerfilController(ServicioUsuarios servicioUsuarios, ServicioPedidos servicioPedidos) {
        this.servicioUsuarios = servicioUsuarios;
        this.servicioPedidos = servicioPedidos;
    }

    /**
     * Obtiene el usuario autenticado desde Spring Security.
     */
    private Usuario usuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || "anonymousUser".equals(auth.getName())) {
            return null;
        }

        return servicioUsuarios.obtenerUserPorEmail(auth.getName());
    }

    // ----------------------------------------------------
    // PERFIL
    // ----------------------------------------------------

    @GetMapping
    public String perfil(Model model) {
        Usuario u = usuarioActual();
        if (u == null) return "redirect:/auth/login";

        model.addAttribute("usuario", u);
        return "perfil/perfil";
    }

    // ----------------------------------------------------
    // EDITAR DATOS
    // ----------------------------------------------------

    @GetMapping("/editar")
    public String editarForm(Model model) {
        Usuario u = usuarioActual();
        if (u == null) return "redirect:/auth/login";

        model.addAttribute("usuario", u);
        return "perfil/editar";
    }

    @PostMapping("/editar")
    public String guardarEdicion(@RequestParam String nombre,
                                 @RequestParam String telefono,
                                 @RequestParam String pais,
                                 Model model) {

        Usuario u = usuarioActual();
        if (u == null) return "redirect:/auth/login";

        servicioUsuarios.actualizarDatos(u.getId(), nombre, u.getPass(), telefono, pais);

        return "redirect:/perfil";
    }

    // ----------------------------------------------------
    // CAMBIAR PASSWORD
    // ----------------------------------------------------

    @GetMapping("/password")
    public String cambiarPassForm(Model model) {
        Usuario u = usuarioActual();
        if (u == null) return "redirect:/auth/login";

        model.addAttribute("usuario", u);
        return "perfil/cambiar-pass";
    }

    @PostMapping("/password")
    public String cambiarPass(@RequestParam String actual,
                              @RequestParam String nueva,
                              @RequestParam String repetir,
                              Model model) {

        Usuario u = usuarioActual();
        if (u == null) return "redirect:/auth/login";

        // Validar contraseña actual
        if (!u.getPass().equals(actual)) {
            model.addAttribute("error", "La contraseña actual es incorrecta");
            return "perfil/cambiar-pass";
        }

        // Validar que ambas nuevas coincidan
        if (!nueva.equals(repetir)) {
            model.addAttribute("error", "Las nuevas contraseñas no coinciden");
            return "perfil/cambiar-pass";
        }

        // Guardar nueva contraseña
        u.setPass(nueva);
        servicioUsuarios.guardarCambiosUsuario(u);

        model.addAttribute("exito", "Contraseña actualizada correctamente");
        return "perfil/cambiar-pass";
    }

    // ----------------------------------------------------
    // MIS PEDIDOS
    // ----------------------------------------------------

    @GetMapping("/mis-pedidos")
    public String misPedidos(Model model) {
        Usuario u = usuarioActual();
        if (u == null) return "redirect:/auth/login";

        model.addAttribute("usuario", u);
        model.addAttribute("pedidos", servicioPedidos.obtenerPedidosDeCliente(u.getId()));
        return "perfil/mis-pedidos";
    }
}
