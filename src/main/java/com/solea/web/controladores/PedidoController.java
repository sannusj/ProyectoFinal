package com.solea.web.controladores;

import com.solea.web.datos.serviciosWeb.ResumenPedido;
import com.solea.web.dto.ProductoCarritoDto;
import com.solea.web.dto.DatosEnvioDto;
import com.solea.web.dto.DatosPagoDto;
import com.solea.web.dto.OpcionesPedidoDto;
import com.solea.web.model.Usuario;
import com.solea.web.servicios.ServicioCarrito;
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

import java.util.List;

@Controller
@RequestMapping("/pedido")
@Tag(
    name = "Pedidos", 
    description = "API de gestión del proceso completo de pedido/checkout en múltiples pasos. " +
                  "Incluye: recopilación de datos de envío, información de pago, opciones adicionales (regalo, observaciones), " +
                  "revisión de resumen y confirmación final. Maneja pedidos temporales durante el proceso " +
                  "y crea el pedido definitivo al confirmar. Requiere autenticación de usuario."
)
@SecurityRequirement(name = "session-auth")
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
    @Operation(
            summary = "Paso 1 del pedido - Formulario de datos de envío",
            description = "Muestra el primer paso del proceso de checkout donde el usuario ingresa información de envío: " +
                         "nombre del destinatario, dirección completa y provincia/estado. " +
                         "Estos datos se almacenan temporalmente para el proceso de compra."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Formulario de datos de envío cargado exitosamente"),
            @ApiResponse(responseCode = "302", description = "Usuario no autenticado - redirige al login")
    })
    @GetMapping("/paso1")
    public String mostrarPaso1(Model model) {
        Usuario u = getUsuarioActual();
        if (u == null) return "redirect:/auth/login";
        model.addAttribute("usuario", u);
        return "pedido/paso1";
    }

    @Operation(
            summary = "Procesar datos de envío del paso 1",
            description = "Valida y almacena temporalmente los datos de envío ingresados por el usuario. " +
                         "Verifica que todos los campos requeridos estén completos y sean válidos. " +
                         "Una vez guardado, redirige al paso 2 (datos de pago).",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos de envío del pedido",
                    required = true,
                    content = @Content(
                            mediaType = "application/x-www-form-urlencoded",
                            schema = @Schema(implementation = DatosEnvioDto.class)
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "302", 
                    description = "Datos guardados exitosamente - redirige al paso 2"
            ),
            @ApiResponse(
                    responseCode = "400", 
                    description = "Datos inválidos o incompletos"
            )
    })
    @PostMapping("/paso1")
    public String procesarPaso1(
            @Parameter(hidden = true) @RequestParam String nombre,
            @Parameter(hidden = true) @RequestParam String direccion,
            @Parameter(hidden = true) @RequestParam String provincia) {
        Usuario u = getUsuarioActual();
        if (u == null) return "redirect:/auth/login";

        servicioPedidos.procesarPaso1(nombre, direccion, provincia, u.getId());
        return "redirect:/pedido/paso2";
    }

    // Paso 2: datos de tarjeta
    @Operation(
            summary = "Paso 2 del pedido - Formulario de datos de pago",
            description = "Muestra el segundo paso del proceso de checkout donde el usuario ingresa información de pago: " +
                         "nombre del titular de la tarjeta, número de tarjeta y tipo (Visa, MasterCard, etc.). " +
                         "Los datos se almacenan temporalmente para procesamiento seguro."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Formulario de datos de pago cargado exitosamente"),
            @ApiResponse(responseCode = "302", description = "Usuario no autenticado - redirige al login")
    })
    @GetMapping("/paso2")
    public String mostrarPaso2(Model model) {
        Usuario u = getUsuarioActual();
        if (u == null) return "redirect:/auth/login";
        model.addAttribute("usuario", u);
        return "pedido/paso2";
    }

    @Operation(
            summary = "Procesar datos de pago del paso 2",
            description = "Valida y almacena temporalmente los datos de pago ingresados por el usuario. " +
                         "Verifica formato de número de tarjeta y datos del titular. " +
                         "Una vez validado, redirige al paso 3 (opciones adicionales).",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos de pago con tarjeta",
                    required = true,
                    content = @Content(
                            mediaType = "application/x-www-form-urlencoded",
                            schema = @Schema(implementation = DatosPagoDto.class)
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "302", 
                    description = "Datos de pago validados y guardados - redirige al paso 3"
            ),
            @ApiResponse(
                    responseCode = "400", 
                    description = "Datos de tarjeta inválidos o incompletos"
            )
    })
    @PostMapping("/paso2")
    public String procesarPaso2(
            @Parameter(hidden = true) @RequestParam String titular,
            @Parameter(hidden = true) @RequestParam String numero,
            @Parameter(hidden = true) @RequestParam String tipoTarjeta) {
        Usuario u = getUsuarioActual();
        if (u == null) return "redirect:/auth/login";

        servicioPedidos.procesarPaso2(titular, numero, tipoTarjeta, u.getId());
        return "redirect:/pedido/paso3";
    }

    // Paso 3: regalo / observaciones
    @Operation(
            summary = "Paso 3 del pedido - Formulario de opciones adicionales",
            description = "Muestra el tercer paso del proceso de checkout donde el usuario puede: " +
                         "indicar si el pedido es un regalo (para incluir mensaje especial), " +
                         "agregar observaciones o instrucciones especiales de entrega. " +
                         "Este paso es opcional pero permite personalización del pedido."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Formulario de opciones adicionales cargado exitosamente"),
            @ApiResponse(responseCode = "302", description = "Usuario no autenticado - redirige al login")
    })
    @GetMapping("/paso3")
    public String mostrarPaso3(Model model) {
        Usuario u = getUsuarioActual();
        if (u == null) return "redirect:/auth/login";
        model.addAttribute("usuario", u);
        return "pedido/paso3";
    }

    @Operation(
            summary = "Procesar opciones adicionales del paso 3",
            description = "Procesa las opciones adicionales del pedido (regalo y observaciones). " +
                         "Almacena temporalmente esta información para incluirla en el pedido final. " +
                         "Una vez completado, redirige al resumen del pedido para revisión final.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Opciones adicionales del pedido",
                    required = false,
                    content = @Content(
                            mediaType = "application/x-www-form-urlencoded",
                            schema = @Schema(implementation = OpcionesPedidoDto.class)
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "302", 
                    description = "Opciones guardadas exitosamente - redirige al resumen del pedido"
            )
    })
    @PostMapping("/paso3")
    public String procesarPaso3(
            @Parameter(hidden = true) @RequestParam(name = "regalo", required = false) String regalo,
            @Parameter(hidden = true) @RequestParam(name = "observaciones", required = false) String observaciones) {
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
    @Operation(
            summary = "Resumen del pedido antes de confirmar",
            description = "Muestra el resumen completo del pedido antes de la confirmación final, incluyendo: " +
                         "datos de envío, información de pago, productos del carrito con cantidades y precios, " +
                         "total a pagar, opciones de regalo y observaciones. " +
                         "Permite al usuario revisar toda la información antes de confirmar la compra. " +
                         "Si no hay datos temporales del pedido, construye un resumen básico con la información disponible."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", 
                    description = "Resumen del pedido cargado exitosamente con todos los detalles"
            ),
            @ApiResponse(
                    responseCode = "302", 
                    description = "Usuario no autenticado - redirige al login"
            )
    })
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
    @Operation(
            summary = "Confirmar y finalizar pedido",
            description = "Procesa la confirmación final del pedido y completa la transacción: " +
                         "1. Valida que todos los productos tengan stock disponible " +
                         "2. Crea el pedido definitivo en la base de datos " +
                         "3. Actualiza el inventario (descuenta stock) " +
                         "4. Vacía el carrito del usuario " +
                         "5. Elimina los datos temporales del pedido " +
                         "6. Genera número de seguimiento " +
                         "Si hay algún error (stock insuficiente, error de pago, etc.), " +
                         "retorna al resumen con mensaje descriptivo sin realizar cambios."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", 
                    description = "Pedido confirmado exitosamente - muestra página de confirmación con detalles"
            ),
            @ApiResponse(
                    responseCode = "200", 
                    description = "Error al confirmar - retorna al resumen con mensaje de error"
            ),
            @ApiResponse(
                    responseCode = "400", 
                    description = "Stock insuficiente, datos inválidos o error en el proceso"
            )
    })
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
    @Operation(
            summary = "Descargar pedido en PDF (no implementado)",
            description = "Endpoint planificado para generar y descargar el comprobante del pedido en formato PDF. " +
                         "Actualmente no está implementado y redirige al resumen con mensaje informativo. " +
                         "Funcionalidad futura incluirá: factura detallada, términos y condiciones, " +
                         "y código QR para seguimiento."
    )
    @ApiResponse(
            responseCode = "302", 
            description = "Funcionalidad no implementada - redirige al resumen con parámetro de error"
    )
    @GetMapping("/pdf")
    public String descargarPdf() {
        // Actualmente no implementado; redirigir al resumen
        return "redirect:/pedido/resumen?error=pdf_no_implementado";
    }
}