package com.solea.web.controladores;

import com.solea.web.model.Usuario;
import com.solea.web.servicios.ServicioUsuarios;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.web.csrf.CsrfToken;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private final ServicioUsuarios servicioUsuarios;
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    public AuthController(ServicioUsuarios servicioUsuarios) {
        this.servicioUsuarios = servicioUsuarios;
    }

    // ===========================
    // VISTA LOGIN
    // ===========================
    @GetMapping("/login")
    public String login() {
        return "login"; // Thymeleaf buscará login.html
    }

    // ===========================
    // VISTA REGISTRO
    // ===========================
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
    @PostMapping("/registro")
    public String procesarRegistro(@ModelAttribute Usuario usuario, Model model, HttpServletRequest request) {

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
