package com.solea.web.security;

import com.solea.web.model.Usuario;
import com.solea.web.servicios.ServicioUsuarios;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private static final Logger log = LoggerFactory.getLogger(CustomOAuth2UserService.class);

    private final ServicioUsuarios servicioUsuarios;

    public CustomOAuth2UserService(ServicioUsuarios servicioUsuarios) {
        this.servicioUsuarios = servicioUsuarios;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService delegate = new org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService();
        OAuth2User oauth2User = delegate.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId(); // e.g., "google"

        // Obtener la clave utilizada como "user name attribute" (p. ej. "sub" en Google)
        String userNameAttr = userRequest.getClientRegistration().getProviderDetails()
                .getUserInfoEndpoint().getUserNameAttributeName();

        Map<String, Object> attributes = new HashMap<>(oauth2User.getAttributes());

        // Extraer oauthId usando la clave apropiada
        Object oauthIdObj = attributes.get(userNameAttr);
        if (oauthIdObj == null) {
            // fallback a claves comunes
            oauthIdObj = attributes.getOrDefault("sub", attributes.get("id"));
        }

        // Add a normalized key so service can find it easily
        if (oauthIdObj != null) {
            attributes.put("oauth_id_normalized", String.valueOf(oauthIdObj));
        }

        log.info("OAuth login for registrationId={} attributes={}", registrationId, attributes);

        // Process or create user in local DB
        Usuario usuario = servicioUsuarios.processOAuthPostLogin(registrationId, attributes);

        log.info("Usuario procesado/creado a partir de OAuth: {}", usuario != null ? usuario.getEmail() : "null");

        // Map user's role to authorities
        String roleName = usuario.getRol() != null ? usuario.getRol().name() : "USER";
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + roleName));

        // Build a DefaultOAuth2User with the authorities and attributes.
        // Prefer using the user's email as the principal name (so auth.getName() == email) when available,
        // this simplifies lookup in the application which often expects the username to be an email.
        String nameAttributeKey;
        if (attributes.containsKey("email")) {
            nameAttributeKey = "email";
        } else {
            nameAttributeKey = userNameAttr != null ? userNameAttr : "sub";
        }

        return new DefaultOAuth2User(authorities, attributes, nameAttributeKey);
    }
}
