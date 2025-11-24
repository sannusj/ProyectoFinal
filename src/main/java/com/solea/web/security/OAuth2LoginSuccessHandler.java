package com.solea.web.security;

import com.solea.web.model.Usuario;
import com.solea.web.repositorios.UsuarioRepository;
import com.solea.web.servicios.ServicioUsuarios;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger log = LoggerFactory.getLogger(OAuth2LoginSuccessHandler.class);

    private final UsuarioRepository usuarioRepository;
    private final ServicioUsuarios servicioUsuarios;

    public OAuth2LoginSuccessHandler(UsuarioRepository usuarioRepository, ServicioUsuarios servicioUsuarios) {
        this.usuarioRepository = usuarioRepository;
        this.servicioUsuarios = servicioUsuarios;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        Object principal = authentication.getPrincipal();

        if (principal instanceof org.springframework.security.oauth2.core.user.OAuth2User) {
            org.springframework.security.oauth2.core.user.OAuth2User oauth2User = (org.springframework.security.oauth2.core.user.OAuth2User) principal;
            java.util.Map<String, Object> attrs = oauth2User.getAttributes();
            String email = attrs != null ? (String) attrs.get("email") : null;

            log.info("OAuth2LoginSuccessHandler - attributes={}", attrs);
            log.info("OAuth2LoginSuccessHandler - authentication class={}", authentication != null ? authentication.getClass().getName() : "null");

            // Intentar crear/actualizar el usuario siempre (idempotente) antes de buscarlo en BD
            String registrationId = null;
            if (authentication instanceof org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken token) {
                registrationId = token.getAuthorizedClientRegistrationId();
            }
            if (registrationId == null) {
                registrationId = "google"; // fallback
            }
            try {
                log.info("OAuth2LoginSuccessHandler - invoking processOAuthPostLogin registrationId={} email={}", registrationId, attrs != null ? attrs.get("email") : null);
                servicioUsuarios.processOAuthPostLogin(registrationId, attrs);
            } catch (Exception ex) {
                log.error("OAuth2LoginSuccessHandler - error creating/updating user via processOAuthPostLogin", ex);
            }

            if (email != null) {
                Usuario u = usuarioRepository.findByEmail(email).orElse(null);
                log.info("OAuth2LoginSuccessHandler - usuario encontrado por email tras processOAuthPostLogin: {}", u != null ? u.getEmail() : "null");
                if (u != null) {
                    // Construir autenticación basada en UserDetails (clase existente)
                    com.solea.web.servicios.impl.UserDetailsImpl userDetails = new com.solea.web.servicios.impl.UserDetailsImpl(u);
                    org.springframework.security.authentication.UsernamePasswordAuthenticationToken tokenAuth =
                            new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(tokenAuth);
                }
                // si aún es null, no hacemos nada (redirigir a home sin sesión)
            }
        }

        response.sendRedirect(request.getContextPath() + "/");
    }
}
