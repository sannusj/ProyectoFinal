package com.solea.web.controladores;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.solea.web.model.Rol;
import com.solea.web.model.Usuario;
import com.solea.web.servicios.ServicioUsuarios;
import com.solea.web.servicios.ServicioPedidos;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Controller
@RequestMapping("/admin")
@Tag(
    name = "Administración", 
    description = "API de administración del sistema con acceso restringido para usuarios con rol ADMIN. " +
                  "Gestiona usuarios, pedidos, roles y configuración general del sistema. " +
                  "Todas las operaciones requieren autenticación y privilegios de administrador."
)
@SecurityRequirement(name = "session-auth")
public class AdminController {

    @Autowired
    private ServicioUsuarios servicioUsuarios;

    @Autowired
    private ServicioPedidos servicioPedidos;

    // -----------------------------------------------------------
    // PANEL PRINCIPAL
    // -----------------------------------------------------------
    @Operation(
            summary = "Panel principal de administración",
            description = "Muestra el dashboard principal del administrador con estadísticas generales, " +
                         "resumen de actividad del sistema, accesos rápidos a gestión de usuarios, " +
                         "productos, pedidos y configuración. Requiere rol ADMIN."
    )
    @ApiResponse(
            responseCode = "200", 
            description = "Panel de administración cargado exitosamente con métricas y resumen del sistema"
    )
    @GetMapping("")
    public String adminHome() {
        return "admin/index"; // admin/index.html
    }

    // -----------------------------------------------------------
    // USUARIOS
    // -----------------------------------------------------------
    @Operation(
            summary = "Listar todos los usuarios del sistema",
            description = "Muestra una tabla completa de todos los usuarios registrados en el sistema incluyendo: " +
                         "ID, nombre, email, rol, fecha de registro, estado de cuenta y país. " +
                         "Permite acciones de gestión como editar rol, ver detalles y administrar permisos."
    )
    @ApiResponse(
            responseCode = "200", 
            description = "Lista de usuarios obtenida exitosamente con toda su información básica"
    )
    @GetMapping("/usuarios")
    public String listarUsuarios(Model modelo) {
        modelo.addAttribute("usuarios", servicioUsuarios.obtenerUsuarios());
        return "admin/usuarios"; // admin/usuarios.html
    }

    @Operation(
            summary = "Ver detalle completo de usuario",
            description = "Muestra información detallada de un usuario específico incluyendo: " +
                         "datos personales, historial de pedidos, actividad en el sistema, " +
                         "fecha de registro y opciones de administración. " +
                         "Si el usuario no existe, muestra mensaje de error y redirige al listado."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", 
                    description = "Detalle del usuario obtenido exitosamente con historial completo"
            ),
            @ApiResponse(
                    responseCode = "404", 
                    description = "Usuario no encontrado - muestra mensaje de error"
            )
    })
    @GetMapping("/usuarios/{id}")
    public String detalleUsuario(
            @Parameter(description = "ID único del usuario en el sistema", required = true, example = "1")
            @PathVariable int id, Model modelo) {
        Usuario u = servicioUsuarios.obtenerUserPorId(id);
        if (u == null) {
            modelo.addAttribute("error", "El usuario no existe.");
            return "admin/usuarios";
        }

        modelo.addAttribute("usuario", u);
        return "admin/usuario-detalle"; // admin/usuario-detalle.html
    }

    @Operation(
            summary = "Cambiar rol de usuario",
            description = "Permite cambiar el rol de un usuario entre USER (usuario normal) y ADMIN (administrador). " +
                         "Incluye validación de seguridad para evitar que se elimine el rol ADMIN del último administrador del sistema, " +
                         "garantizando que siempre haya al menos un administrador disponible. " +
                         "Esta acción afecta inmediatamente los permisos y accesos del usuario."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "302", 
                    description = "Rol cambiado exitosamente - redirige a la lista de usuarios con mensaje de confirmación"
            ),
            @ApiResponse(
                    responseCode = "400", 
                    description = "Error: No se puede quitar el rol ADMIN al último administrador del sistema"
            )
    })
    @PostMapping("/usuarios/{id}/cambiar-rol")
    public String cambiarRol(
            @Parameter(description = "ID único del usuario al que se le cambiará el rol", required = true, example = "1")
            @PathVariable Integer id,
            @Parameter(description = "Nuevo rol a asignar (USER o ADMIN)", required = true, example = "ADMIN")
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
    @Operation(
            summary = "Listar todos los pedidos del sistema",
            description = "Muestra una lista completa de todos los pedidos realizados en el sistema con información resumida: " +
                         "número de pedido, cliente, fecha, total, estado (pendiente, enviado, entregado, cancelado), " +
                         "y opciones de gestión. Permite filtrar y ordenar pedidos por diferentes criterios."
    )
    @ApiResponse(
            responseCode = "200", 
            description = "Lista de pedidos obtenida exitosamente con información resumida de cada pedido"
    )
    @GetMapping("/pedidos")
    public String listarPedidos(Model modelo) {
        modelo.addAttribute("pedidos", servicioPedidos.obtenerPedidos());
        return "admin/pedidos"; // admin/pedidos.html
    }

    @Operation(
            summary = "Ver detalle completo de pedido",
            description = "Muestra información detallada de un pedido específico incluyendo: " +
                         "datos del cliente, productos ordenados con cantidades y precios, " +
                         "dirección de envío, método de pago, estado actual, historial de cambios de estado, " +
                         "y opciones para actualizar el estado del pedido. " +
                         "Si el pedido no existe, muestra mensaje de error."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", 
                    description = "Detalle del pedido obtenido exitosamente con toda la información"
            ),
            @ApiResponse(
                    responseCode = "404", 
                    description = "Pedido no encontrado - muestra mensaje de error"
            )
    })
    @GetMapping("/pedidos/{id}")
    public String verPedido(
            @Parameter(description = "ID único del pedido en el sistema", required = true, example = "1")
            @PathVariable int id, Model modelo) {
        var pedido = servicioPedidos.obtenerPedidoPorId(id);

        if (pedido == null) {
            modelo.addAttribute("error", "El pedido no existe.");
            return "admin/pedidos";
        }

        modelo.addAttribute("pedido", pedido);
        return "admin/pedido-detalle"; // admin/pedido-detalle.html
    }

    @Operation(
            summary = "Cambiar estado de pedido",
            description = "Actualiza el estado de un pedido a través de su ciclo de vida: " +
                         "pendiente → procesando → enviado → entregado. " +
                         "También permite marcar como cancelado en cualquier etapa. " +
                         "El cambio de estado puede desencadenar notificaciones al cliente " +
                         "y actualizar el inventario según corresponda."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "302", 
                    description = "Estado del pedido actualizado exitosamente - redirige al detalle del pedido"
            ),
            @ApiResponse(
                    responseCode = "400", 
                    description = "Estado inválido o transición no permitida"
            )
    })
    @PostMapping("/pedidos/{id}/estado")
    public String cambiarEstadoPedido(
            @Parameter(description = "ID único del pedido", required = true, example = "1")
            @PathVariable int id,
            @Parameter(
                    description = "Nuevo estado del pedido", 
                    required = true, 
                    example = "enviado"
            )
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
