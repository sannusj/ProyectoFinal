package com.solea.web.controladores;

import com.solea.web.dto.ProductoCarritoDto;
import com.solea.web.model.Usuario;
import com.solea.web.servicios.ServicioCarrito;
import com.solea.web.servicios.ServicioUsuarios;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/carrito")
@Tag(
    name = "Carrito", 
    description = "API de gestión del carrito de compras del usuario. " +
                  "Permite agregar, actualizar, eliminar productos y gestionar el proceso de checkout. " +
                  "Requiere autenticación de usuario. Los administradores no pueden usar el carrito. " +
                  "Calcula totales automáticamente y mantiene sincronización con el inventario."
)
@SecurityRequirement(name = "session-auth")
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
    @Operation(
            summary = "Ver carrito de compras del usuario",
            description = "Muestra todos los productos en el carrito del usuario autenticado con detalles completos: " +
                         "nombre del producto, imagen, precio unitario, cantidad seleccionada, subtotal por producto, " +
                         "y total general del carrito. Si el usuario no está autenticado, redirige al login. " +
                         "Los administradores no tienen acceso al carrito."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Carrito obtenido exitosamente con productos y totales"),
            @ApiResponse(responseCode = "302", description = "Usuario no autenticado - redirige al login")
    })
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
    @Operation(
            summary = "Agregar producto al carrito",
            description = "Agrega un producto al carrito del usuario con la cantidad especificada. " +
                         "Si el producto ya existe en el carrito, incrementa su cantidad. " +
                         "Valida que haya stock disponible antes de agregar. " +
                         "Los usuarios administradores no pueden agregar productos al carrito. " +
                         "Si el usuario no está autenticado, redirige al login."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "302", 
                    description = "Producto agregado exitosamente - redirige al carrito actualizado"
            ),
            @ApiResponse(
                    responseCode = "403", 
                    description = "Acceso denegado - los administradores no pueden usar el carrito"
            ),
            @ApiResponse(
                    responseCode = "400", 
                    description = "Stock insuficiente o cantidad inválida"
            )
    })
    @PostMapping("/agregar")
    public String agregarProducto(
            @Parameter(description = "ID del producto/prenda a agregar al carrito", required = true, example = "1")
            @RequestParam("prendaId") int prendaId,
            @Parameter(description = "Cantidad de unidades a agregar", required = true, example = "2")
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
    @Operation(
            summary = "Actualizar cantidad de producto en carrito",
            description = "Actualiza la cantidad de un producto específico que ya está en el carrito. " +
                         "Valida que la nueva cantidad no exceda el stock disponible. " +
                         "Si la cantidad es 0 o negativa, elimina el producto del carrito. " +
                         "Recalcula automáticamente los totales del carrito."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "302", 
                    description = "Cantidad actualizada exitosamente - redirige al carrito"
            ),
            @ApiResponse(
                    responseCode = "400", 
                    description = "Cantidad inválida o stock insuficiente"
            )
    })
    @PostMapping("/actualizar")
    public String actualizarCantidad(
            @Parameter(description = "ID del producto en el carrito", required = true, example = "1")
            @RequestParam("prendaId") int prendaId,
            @Parameter(description = "Nueva cantidad deseada", required = true, example = "3")
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
    @Operation(
            summary = "Eliminar producto del carrito",
            description = "Elimina completamente un producto específico del carrito del usuario. " +
                         "Esta acción no afecta el stock del producto. " +
                         "Recalcula automáticamente el total del carrito después de la eliminación."
    )
    @ApiResponse(
            responseCode = "302", 
            description = "Producto eliminado exitosamente del carrito - redirige al carrito actualizado"
    )
    @PostMapping("/eliminar")
    public String eliminarProducto(
            @Parameter(description = "ID del producto a eliminar del carrito", required = true, example = "1")
            @RequestParam("prendaId") int prendaId) {

        Usuario usuario = getUsuarioActual();
        if (usuario == null) return "redirect:/auth/login";

        if (usuario.getRol() == com.solea.web.model.Rol.ADMIN) {
            throw new AccessDeniedException("Los administradores no pueden usar el carrito");
        }

        servicioCarrito.eliminarProductoDelCarrito(usuario.getId(), prendaId);

        return "redirect:/carrito";
    }

    // Vaciar carrito
    @Operation(
            summary = "Vaciar carrito completamente",
            description = "Elimina todos los productos del carrito del usuario en una sola operación. " +
                         "Esta acción no afecta el stock de los productos. " +
                         "Útil para cancelar una compra o empezar de nuevo. " +
                         "No se puede deshacer esta acción."
    )
    @ApiResponse(
            responseCode = "302", 
            description = "Carrito vaciado exitosamente - redirige al carrito vacío"
    )
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
    @Operation(
            summary = "Iniciar proceso de checkout",
            description = "Redirige al usuario al primer paso del proceso de compra (checkout). " +
                         "Valida que el carrito no esté vacío antes de proceder. " +
                         "El proceso de checkout incluye: datos de envío, información de pago, " +
                         "opciones adicionales y confirmación final. " +
                         "Los administradores no pueden realizar compras."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "302", 
                    description = "Redirige al paso 1 del proceso de pedido/checkout"
            ),
            @ApiResponse(
                    responseCode = "400", 
                    description = "Carrito vacío - no se puede proceder al checkout"
            ),
            @ApiResponse(
                    responseCode = "403", 
                    description = "Administradores no pueden realizar compras"
            )
    })
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
