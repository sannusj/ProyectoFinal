package com.solea.web.controladores;

import com.solea.web.model.Usuario;
import com.solea.web.model.Rol;
import com.solea.web.servicios.ServicioPrendas;
import com.solea.web.servicios.ServicioUsuarios;
import com.solea.web.model.Prenda;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@Tag(
    name = "Home", 
    description = "Endpoints públicos y páginas principales del sitio web. " +
                  "Incluye página de inicio, catálogo de productos, detalles de productos, " +
                  "páginas informativas (nosotros, contacto, ayuda) y gestión de navegación del usuario."
)
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

    @Operation(
            summary = "Página principal del sitio",
            description = "Muestra la página de inicio (landing page) del sitio web con productos destacados. " +
                         "Detecta si el usuario está autenticado y muestra información personalizada. " +
                         "Es el punto de entrada principal para visitantes y usuarios registrados."
    )
    @ApiResponse(
            responseCode = "200", 
            description = "Página principal cargada exitosamente con productos destacados y estado de autenticación"
    )
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

    @Operation(
            summary = "Dashboard de usuario autenticado",
            description = "Muestra el panel principal (dashboard) del usuario autenticado. " +
                         "Si el usuario no está autenticado, redirige a la página de login. " +
                         "Si el usuario tiene rol ADMIN, redirige automáticamente al panel de administración. " +
                         "Para usuarios normales, muestra su dashboard personalizado con opciones y accesos rápidos."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dashboard de usuario cargado exitosamente"),
            @ApiResponse(responseCode = "302", description = "Redirige a /auth/login si no está autenticado o a /admin si es administrador")
    })
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

    @Operation(
            summary = "Catálogo de productos con filtros",
            description = "Muestra el catálogo completo de productos disponibles con opciones de filtrado avanzado. " +
                         "Permite filtrar por categoría (Hombre, Mujer, Accesorios), por nombre de producto, " +
                         "o combinar ambos filtros. Si no se especifica ningún filtro, muestra todos los productos. " +
                         "Maneja errores de búsqueda devolviendo una lista vacía con mensaje informativo."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Catálogo cargado exitosamente con productos filtrados"),
            @ApiResponse(responseCode = "200", description = "Catálogo cargado con lista vacía si hay error en la búsqueda")
    })
    @GetMapping("/catalogo")
    public String catalogo(Model model, 
            @Parameter(description = "Filtro por categoría de producto", example = "Hombre", required = false)
            @org.springframework.web.bind.annotation.RequestParam(name = "cat", required = false) String cat, 
            @Parameter(description = "Filtro por nombre o texto en el producto", example = "camisa", required = false)
            @org.springframework.web.bind.annotation.RequestParam(name = "nombre", required = false) String nombre) {
        try {
            String catTrim = (cat != null && !cat.trim().isEmpty()) ? cat.trim() : null;
            String nombreTrim = (nombre != null && !nombre.trim().isEmpty()) ? nombre.trim() : null;

            List<Prenda> resultados;

            if (nombreTrim != null && catTrim != null) {
                // Buscar por nombre y luego filtrar por categoría (ambos filtros combinados)
                resultados = servicioPrendas.buscarPorNombre(nombreTrim).stream()
                        .filter(p -> p.getCategoria() != null && catTrim.equalsIgnoreCase(p.getCategoria().getNombre()))
                        .toList();
            } else if (nombreTrim != null) {
                resultados = servicioPrendas.buscarPorNombre(nombreTrim);
            } else if (catTrim != null) {
                resultados = servicioPrendas.buscarPorCategoria(catTrim);
            } else {
                resultados = servicioPrendas.obtenerTodas();
            }

            model.addAttribute("productos", resultados);
            model.addAttribute("usuario", usuarioActual());
            model.addAttribute("categoriaSeleccionada", catTrim);
            model.addAttribute("nombreBusqueda", nombreTrim);
            return "home/catalogo";
        } catch (Exception ex) {
            model.addAttribute("error", "Error al cargar catálogo: " + ex.getMessage());
            model.addAttribute("productos", java.util.Collections.emptyList());
            model.addAttribute("usuario", usuarioActual());
            return "home/catalogo";
        }
    }

    @Operation(
            summary = "Página de contacto",
            description = "Muestra la página de contacto con formulario para enviar mensajes al equipo del sitio. " +
                         "Incluye campos para nombre, email, asunto y mensaje."
    )
    @ApiResponse(responseCode = "200", description = "Página de contacto cargada exitosamente")
    @GetMapping("/contacto")
    public String contacto(Model model) {
        model.addAttribute("usuario", usuarioActual());
        return "home/contacto";
    }

    @Operation(
            summary = "Página 'Nosotros'",
            description = "Muestra información sobre la empresa, misión, visión y equipo. " +
                         "Página informativa institucional del sitio web."
    )
    @ApiResponse(responseCode = "200", description = "Página 'Nosotros' cargada exitosamente")
    @GetMapping("/nosotros")
    public String nosotros(Model model) {
        model.addAttribute("usuario", usuarioActual());
        return "home/nosotros";
    }

    @Operation(
            summary = "Detalle de producto",
            description = "Muestra la página de detalle de un producto específico incluyendo: " +
                         "imagen, nombre, descripción, precio, categoría, stock disponible y productos relacionados. " +
                         "Si el producto no existe, redirige al catálogo. " +
                         "Muestra hasta 8 productos relacionados de la misma categoría."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Detalle del producto cargado exitosamente con productos relacionados"),
            @ApiResponse(responseCode = "302", description = "Producto no encontrado - redirige al catálogo")
    })
    @GetMapping("/detalle/{id}")
    public String detalle(
            @Parameter(description = "ID del producto a mostrar", required = true, example = "1")
            @PathVariable int id, Model model) {

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
        model.addAttribute("usuario", usuarioActual());

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


    @Operation(
            summary = "Página de ayuda",
            description = "Muestra la página de ayuda con preguntas frecuentes, guías de uso, " +
                         "políticas de envío y devolución, y métodos de contacto para soporte."
    )
    @ApiResponse(responseCode = "200", description = "Página de ayuda cargada exitosamente")
    @GetMapping("/ayuda")
    public String ayuda(Model model) {
        model.addAttribute("usuario", usuarioActual());
        return "home/ayuda";
    }

    // Vista ligera para la plantilla detalle.html
    private static record PrendaView(Integer idPrenda, String imagen, String nombre, String descripcion, Double precio, String categoria, int stock) {}
}