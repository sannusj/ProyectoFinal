package com.solea.web.controladores;

import com.solea.web.model.Rol;
import com.solea.web.model.Usuario;
import com.solea.web.dto.EditarPerfilDto;
import com.solea.web.dto.CambiarPasswordDto;
import com.solea.web.servicios.ServicioPedidos;
import com.solea.web.servicios.ServicioUsuarios;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Controller
@RequestMapping("/perfil")
@Tag(
    name = "Perfil", 
    description = "API de gestión del perfil de usuario autenticado. " +
                  "Permite ver, editar datos personales, cambiar contraseña y consultar historial de pedidos. " +
                  "Incluye vistas diferenciadas para usuarios normales y administradores. " +
                  "Requiere autenticación. Maneja cuentas locales y OAuth."
)
@SecurityRequirement(name = "session-auth")
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

    @Operation(
            summary = "Ver perfil de usuario autenticado",
            description = "Muestra la página de perfil del usuario actualmente autenticado con toda su información personal: " +
                         "nombre, email, teléfono, país, fecha de registro, rol y avatar. " +
                         "Si el usuario no está autenticado, redirige al login. " +
                         "Si el usuario tiene rol ADMIN, muestra la vista de perfil de administrador. " +
                         "Los usuarios normales ven su perfil estándar con opciones de edición y cambio de contraseña."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Perfil cargado exitosamente - vista según rol del usuario"),
            @ApiResponse(responseCode = "302", description = "Usuario no autenticado - redirige al login")
    })
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

    @Operation(
            summary = "Formulario de edición de perfil",
            description = "Muestra el formulario para editar los datos personales del usuario autenticado. " +
                         "Permite modificar: nombre, teléfono y país. " +
                         "El email no es editable ya que es el identificador único del usuario. " +
                         "Los campos se pre-cargan con los valores actuales del usuario."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Formulario de edición cargado con datos actuales"),
            @ApiResponse(responseCode = "302", description = "Usuario no autenticado - redirige al login")
    })
    @GetMapping("/editar")
    public String editarForm(Model model) {
        Usuario u = usuarioActual();
        if (u == null) return "redirect:/auth/login";

        model.addAttribute("usuario", u);
        return "perfil/editar";
    }

    @Operation(
            summary = "Guardar cambios del perfil editado",
            description = "Actualiza los datos personales del usuario autenticado en la base de datos. " +
                         "Valida que todos los campos requeridos estén completos. " +
                         "Mantiene la contraseña actual sin modificaciones (se cambia por endpoint separado). " +
                         "Una vez guardado exitosamente, redirige a la página de perfil con los datos actualizados.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Nuevos datos del perfil del usuario",
                    required = true,
                    content = @Content(
                            mediaType = "application/x-www-form-urlencoded",
                            schema = @Schema(implementation = EditarPerfilDto.class)
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "302", 
                    description = "Datos actualizados exitosamente - redirige al perfil"
            ),
            @ApiResponse(
                    responseCode = "400", 
                    description = "Datos inválidos o incompletos"
            )
    })
    @PostMapping("/editar")
    public String guardarEdicion(
            @Parameter(hidden = true) @RequestParam String nombre,
            @Parameter(hidden = true) @RequestParam String telefono,
            @Parameter(hidden = true) @RequestParam String pais,
            Model model) {

        Usuario u = usuarioActual();
        if (u == null) return "redirect:/auth/login";

        servicioUsuarios.actualizarDatos(u.getId(), nombre, u.getPass(), telefono, pais);

        return "redirect:/perfil";
    }

    // ----------------------------------------------------
    // CAMBIAR PASSWORD
    // ----------------------------------------------------

    @Operation(
            summary = "Formulario de cambio de contraseña",
            description = "Muestra el formulario para cambiar la contraseña del usuario autenticado. " +
                         "Requiere ingresar: contraseña actual (para validación), nueva contraseña y confirmación. " +
                         "IMPORTANTE: Este endpoint solo funciona para cuentas creadas localmente (Provider.LOCAL). " +
                         "Los usuarios que ingresaron con OAuth (Google, Facebook, etc.) no pueden cambiar contraseña " +
                         "ya que se gestiona por el proveedor externo. Se muestra mensaje informativo en esos casos."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Formulario de cambio de contraseña cargado"),
            @ApiResponse(responseCode = "302", description = "Usuario OAuth - redirige al perfil con mensaje de error")
    })
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

    @Operation(
            summary = "Procesar cambio de contraseña",
            description = "Cambia la contraseña del usuario después de validaciones de seguridad: " +
                         "1. Verifica que la contraseña actual ingresada sea correcta (usando BCrypt) " +
                         "2. Valida que las nuevas contraseñas coincidan " +
                         "3. Encripta la nueva contraseña con BCrypt antes de guardar " +
                         "4. Solo funciona para usuarios con Provider.LOCAL (cuentas locales) " +
                         "Si alguna validación falla, retorna al formulario con mensaje descriptivo.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos para cambio de contraseña",
                    required = true,
                    content = @Content(
                            mediaType = "application/x-www-form-urlencoded",
                            schema = @Schema(implementation = CambiarPasswordDto.class)
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", 
                    description = "Contraseña cambiada exitosamente - muestra formulario con mensaje de éxito"
            ),
            @ApiResponse(
                    responseCode = "200", 
                    description = "Error en validación - muestra formulario con mensaje de error"
            ),
            @ApiResponse(
                    responseCode = "403", 
                    description = "Usuario OAuth - no permitido cambiar contraseña"
            )
    })
    @PostMapping("/password")
    public String cambiarPass(
            @Parameter(hidden = true) @RequestParam String actual,
            @Parameter(hidden = true) @RequestParam String nueva,
            @Parameter(hidden = true) @RequestParam String repetir,
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

    @Operation(
            summary = "Ver historial de pedidos del usuario",
            description = "Muestra una lista completa de todos los pedidos realizados por el usuario autenticado, " +
                         "ordenados por fecha (más recientes primero). Cada pedido incluye: " +
                         "número de pedido, fecha, productos ordenados, total pagado, estado actual (pendiente/enviado/entregado), " +
                         "dirección de envío y opciones para ver detalles completos. " +
                         "Útil para hacer seguimiento de compras y descargar comprobantes."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", 
                    description = "Historial de pedidos cargado exitosamente"
            ),
            @ApiResponse(
                    responseCode = "302", 
                    description = "Usuario no autenticado - redirige al login"
            )
    })
    @GetMapping("/mis-pedidos")
    public String misPedidos(Model model) {
        Usuario u = usuarioActual();
        if (u == null) return "redirect:/auth/login";

        model.addAttribute("usuario", u);
        model.addAttribute("pedidos", servicioPedidos.obtenerPedidosDeCliente(u.getId()));
        return "perfil/mis-pedidos";
    }

}