package com.solea.web.controladores;

import com.solea.web.model.Rol;
import com.solea.web.model.Usuario;
import com.solea.web.servicios.ServicioPedidos;
import com.solea.web.servicios.ServicioUsuarios;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Controller
@RequestMapping("/perfil")
public class PerfilController {

    private final ServicioUsuarios servicioUsuarios;
    private final ServicioPedidos servicioPedidos;
    private final BCryptPasswordEncoder passwordEncoder;

    public PerfilController(ServicioUsuarios servicioUsuarios, ServicioPedidos servicioPedidos, BCryptPasswordEncoder passwordEncoder) {
        this.servicioUsuarios = servicioUsuarios;
        this.servicioPedidos = servicioPedidos;
        this.passwordEncoder = passwordEncoder;
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

        // Si el usuario tiene rol ADMIN, mostrar la vista del admin, si no la vista normal
        if (u.getRol() == Rol.ADMIN) {
            return "admin/perfil"; // admin/perfil.html
        }

        return "perfil/perfil"; // perfil/perfil.html
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

        // Si el usuario viene de un proveedor externo (OAuth), no permitir cambiar contraseña
        if (u.getProvider() != Usuario.Provider.LOCAL) {
            model.addAttribute("error", "No es posible cambiar la contraseña para cuentas creadas con proveedor externo.");
            return "redirect:/perfil";
        }

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

        // Si el usuario viene de OAuth no permitir cambio
        if (u.getProvider() != Usuario.Provider.LOCAL) {
            model.addAttribute("error", "No es posible cambiar la contraseña para cuentas creadas con proveedor externo.");
            return "redirect:/perfil";
        }

        // Validar contraseña actual usando BCrypt
        if (!passwordEncoder.matches(actual, u.getPass())) {
            model.addAttribute("error", "La contraseña actual es incorrecta");
            model.addAttribute("usuario", u);
            return "perfil/cambiar-pass";
        }

        // Validar que ambas nuevas coincidan
        if (!nueva.equals(repetir)) {
            model.addAttribute("error", "Las nuevas contraseñas no coinciden");
            model.addAttribute("usuario", u);
            return "perfil/cambiar-pass";
        }

        // Guardar nueva contraseña (servicio se encarga de encriptar si es necesario)
        u.setPass(nueva);
        servicioUsuarios.guardarCambiosUsuario(u);

        model.addAttribute("exito", "Contraseña actualizada correctamente");
        model.addAttribute("usuario", u);
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
