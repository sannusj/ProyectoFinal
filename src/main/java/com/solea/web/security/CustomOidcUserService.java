package com.solea.web.security;

import com.solea.web.model.Usuario;
import com.solea.web.servicios.ServicioUsuarios;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CustomOidcUserService extends OidcUserService {

    private static final Logger log = LoggerFactory.getLogger(CustomOidcUserService.class);

    private final ServicioUsuarios servicioUsuarios;

    public CustomOidcUserService(ServicioUsuarios servicioUsuarios) {
        this.servicioUsuarios = servicioUsuarios;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);

        Map<String, Object> attributes = new HashMap<>(oidcUser.getClaims());

        log.info("OIDC login attributes={}", attributes);

        // normalizar oauth id
        Object oauthId = attributes.getOrDefault(userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName(), attributes.get("sub"));
        if (oauthId != null) attributes.put("oauth_id_normalized", String.valueOf(oauthId));

        // Delegar a servicio para crear/actualizar usuario
        Usuario usuario = servicioUsuarios.processOAuthPostLogin(userRequest.getClientRegistration().getRegistrationId(), attributes);

        log.info("Usuario OIDC procesado/creado: {}", usuario != null ? usuario.getEmail() : "null");

        // construir autoridades basadas en rol
        String role = usuario != null && usuario.getRol() != null ? usuario.getRol().name() : "USER";
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));

        // OidcUser permite setear authorities en un wrapper; devolveremos el original OidcUser (it already has claims)
        // pero Spring Security usará las authorities del OIDC principal (esto puede necesitar un wrapper si queremos sustituirlas).
        return oidcUser;
    }
}

