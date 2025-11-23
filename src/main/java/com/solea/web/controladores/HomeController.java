package com.solea.web.controladores;

import com.solea.web.model.Usuario;
import com.solea.web.model.Rol;
import com.solea.web.servicios.ServicioPrendas;
import com.solea.web.servicios.ServicioUsuarios;
import com.solea.web.model.Prenda;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    private final ServicioUsuarios servicioUsuarios;
    private final ServicioPrendas servicioPrendas;

    public HomeController(ServicioUsuarios servicioUsuarios,
                          ServicioPrendas servicioPrendas) {
        this.servicioUsuarios = servicioUsuarios;
        this.servicioPrendas = servicioPrendas;
    }

    /**
     * Método para obtener el usuario autenticado.
     */
    private Usuario usuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || auth.getName().equals("anonymousUser"))
            return null;

        return servicioUsuarios.obtenerUserPorEmail(auth.getName());
    }

    // ---------------------------------------------------------
    // HOME PÚBLICO
    // ---------------------------------------------------------

    @GetMapping("/")
    public String home(Model model) {
        Usuario u = usuarioActual();
        model.addAttribute("usuario", u);

        // Cargar productos destacados para el home (usar obtenerTodas por ahora)
        model.addAttribute("destacados", servicioPrendas.obtenerTodas());

        return "home/index";
    }

    // ---------------------------------------------------------
    // DASHBOARD DE USUARIO LOGUEADO
    // ---------------------------------------------------------

    @GetMapping("/inicio")
    public String inicioUsuario(Model model) {
        Usuario u = usuarioActual();

        if (u == null)
            return "redirect:/auth/login";

        // Redirigir si es admin
        if (u.getRol() == Rol.ADMIN)
            return "redirect:/admin";

        model.addAttribute("usuario", u);
        model.addAttribute("nombres", u.getNombre());
        return "home/inicio";
    }

    // ---------------------------------------------------------
    // OTRAS PÁGINAS
    // ---------------------------------------------------------

    @GetMapping("/catalogo")
    public String catalogo(Model model) {
        try {
            model.addAttribute("productos", servicioPrendas.obtenerTodas());
            model.addAttribute("usuario", usuarioActual());
            return "home/catalogo";
        } catch (Exception ex) {
            model.addAttribute("error", "Error al cargar catálogo: " + ex.getMessage());
            model.addAttribute("productos", java.util.Collections.emptyList());
            model.addAttribute("usuario", usuarioActual());
            return "home/catalogo";
        }
    }

    @GetMapping("/contacto")
    public String contacto(Model model) {
        model.addAttribute("usuario", usuarioActual());
        return "home/contacto";
    }

    @GetMapping("/nosotros")
    public String nosotros(Model model) {
        model.addAttribute("usuario", usuarioActual());
        return "home/nosotros";
    }

    @GetMapping("/detalle/{id}")
    public String detalle(@PathVariable int id, Model model) {

        Prenda prenda = servicioPrendas.obtenerPorId(id);
        if (prenda == null) return "redirect:/catalogo";

        // Construir una vista ligera que la plantilla espera
        PrendaView pv = new PrendaView(
                prenda.getId(),
                // para la vista usamos el id como nombre de imagen (el controlador que sirve la imagen puede interpretar esto)
                prenda.getImagenPrenda() != null ? prenda.getId().toString() : null,
                prenda.getNombre(),
                // Si no existe campo descripcion en la entidad, dejamos vacío
                "",
                prenda.getPrecio(),
                prenda.getCategoria() != null ? prenda.getCategoria().getNombre() : "",
                prenda.getStock()
        );

        model.addAttribute("prenda", pv);

        // relacionados: mapear a la misma vista ligera
        List<PrendaView> relacionados = servicioPrendas.obtenerTodas().stream()
                .filter(p -> !p.getId().equals(prenda.getId()))
                .limit(8)
                .map(p -> new PrendaView(
                        p.getId(),
                        p.getImagenPrenda() != null ? p.getId().toString() : null,
                        p.getNombre(),
                        "",
                        p.getPrecio(),
                        p.getCategoria() != null ? p.getCategoria().getNombre() : "",
                        p.getStock()
                ))
                .collect(Collectors.toList());

        model.addAttribute("relacionados", relacionados);

        return "home/detalle";
    }


    @GetMapping("/ayuda")
    public String ayuda(Model model) {
        model.addAttribute("usuario", usuarioActual());
        return "home/ayuda";
    }

    // Vista ligera para la plantilla detalle.html
    private static record PrendaView(Integer idPrenda, String imagen, String nombre, String descripcion, Double precio, String categoria, int stock) {}
}
