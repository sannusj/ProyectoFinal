package com.solea.web.controladores;

import com.solea.web.model.Usuario;
import com.solea.web.servicios.ServicioUsuarios;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.web.csrf.CsrfToken;

@Controller
@RequestMapping("/auth")
@Tag(
    name = "Autenticación", 
    description = "API de autenticación que gestiona el login, registro y autenticación de usuarios en el sistema. " +
                  "Incluye formularios de registro con validación, manejo de sesiones y redirección según roles."
)
public class AuthController {

    private final ServicioUsuarios servicioUsuarios;
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    public AuthController(ServicioUsuarios servicioUsuarios) {
        this.servicioUsuarios = servicioUsuarios;
    }

    // ===========================
    // VISTA LOGIN
    // ===========================
    @Operation(
            summary = "Página de login",
            description = "Muestra el formulario de inicio de sesión del sistema. " +
                         "Los usuarios pueden autenticarse con su email y contraseña. " +
                         "Después del login exitoso, son redirigidos según su rol (admin o usuario normal)."
    )
    @ApiResponse(
            responseCode = "200", 
            description = "Página de login renderizada exitosamente con formulario de autenticación"
    )
    @GetMapping("/login")
    public String login() {
        return "login"; // Thymeleaf buscará login.html
    }

    // ===========================
    // VISTA REGISTRO
    // ===========================
    @Operation(
            summary = "Página de registro",
            description = "Muestra el formulario de registro para nuevos usuarios. " +
                         "El formulario incluye campos para nombre, email, contraseña, teléfono y país. " +
                         "Incluye protección CSRF para seguridad."
    )
    @ApiResponse(
            responseCode = "200", 
            description = "Página de registro renderizada con formulario vacío y token CSRF"
    )
    @GetMapping("/registro")
    public String registro(Model model, HttpServletRequest request) {
        model.addAttribute("usuario", new Usuario());
        // pasar token CSRF explícitamente si está disponible para evitar NPE en plantilla
        Object token = request.getAttribute("_csrf");
        if (token instanceof CsrfToken) {
            model.addAttribute("_csrf", token);
        }
        return "registro"; // debes crear registro.html
    }

    // ===========================
    // PROCESAR REGISTRO
    // ===========================
    @Operation(
            summary = "Procesar registro de usuario",
            description = "Procesa el formulario de registro y crea un nuevo usuario en el sistema. " +
                         "Valida que el email no esté duplicado, encripta la contraseña con BCrypt y " +
                         "asigna el rol USER por defecto. Si hay errores de validación, muestra mensajes específicos.",
            requestBody = @RequestBody(
                    description = "Datos del nuevo usuario a registrar. Completa todos los campos del formulario.",
                    required = true,
                    content = @Content(
                            mediaType = "application/x-www-form-urlencoded",
                            schema = @Schema(implementation = Usuario.class),
                            examples = @ExampleObject(
                                    name = "Ejemplo de registro",
                                    summary = "Usuario de ejemplo",
                                    description = "Datos de ejemplo para registrar un nuevo usuario en el sistema",
                                    value = "nombre=Juan Pérez García&email=juan.perez@ejemplo.com&pass=MiPassword123!&tel=+57 300 123 4567&pais=Colombia"
                            )
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "302", 
                    description = "Usuario registrado exitosamente - redirige a /auth/login?registrado=true"
            ),
            @ApiResponse(
                    responseCode = "200", 
                    description = "Error en validación - retorna formulario con mensaje de error"
            ),
            @ApiResponse(
                    responseCode = "400", 
                    description = "Datos inválidos, email duplicado o campos obligatorios vacíos"
            )
    })
    @PostMapping("/registro")
    public String procesarRegistro(
            @Parameter(hidden = true) @ModelAttribute Usuario usuario, 
            @Parameter(hidden = true) Model model, 
            @Parameter(hidden = true) HttpServletRequest request) {

        // Si el binding falló, reconstruimos a partir de parámetros (fallback)
        if (usuario == null) {
            logger.warn("procesarRegistro recibido usuario null. Parámetros entrantes:");
            request.getParameterMap().forEach((k, v) -> logger.warn("param {} = {}", k, java.util.Arrays.toString(v)));

            String nombre = trimToNull(request.getParameter("nombre"));
            String email = trimToNull(request.getParameter("email"));
            String pass = trimToNull(request.getParameter("pass"));
            String tel = trimToNull(request.getParameter("tel"));
            String pais = trimToNull(request.getParameter("pais"));

            if (email == null || pass == null) {
                model.addAttribute("error", "Datos inválidos. Completa el formulario de registro.");
                model.addAttribute("usuario", new Usuario());
                return "registro";
            }

            usuario = new Usuario();
            usuario.setNombre(nombre);
            usuario.setEmail(email);
            usuario.setPass(pass);
            usuario.setTel(tel);
            usuario.setPais(pais);

            logger.info("Usuario reconstruido desde parámetros: email={}", email);
        }

        if (usuario.getEmail() == null || usuario.getEmail().isBlank()) {
            logger.warn("procesarRegistro email vacío o nulo");
            model.addAttribute("error", "El correo es obligatorio.");
            model.addAttribute("usuario", usuario);
            return "registro";
        }

        // Validar si ya existe
        Usuario existe = servicioUsuarios.obtenerUserPorEmail(usuario.getEmail());
        if (existe != null) {
            model.addAttribute("error", "El correo ya está registrado.");
            return "registro";
        }

        try {
            // Registrar usuario
            servicioUsuarios.registarUsuario(usuario);

            return "redirect:/auth/login?registrado=true";

        } catch (Exception e) {
            logger.error("Error al registrar usuario", e);
            model.addAttribute("error", "Ocurrió un error al registrar el usuario.");
            return "registro";
        }
    }

    private String trimToNull(String s) {
        if (s == null) return null;
        s = s.trim();
        return s.isEmpty() ? null : s;
    }
}
