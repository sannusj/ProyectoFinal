package com.solea.web.config;

import com.solea.web.security.CustomOAuth2UserService;
import com.solea.web.security.CustomOidcUserService;
import com.solea.web.security.OAuth2LoginSuccessHandler;
import jakarta.servlet.ServletException;
import java.io.IOException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2LoginSuccessHandler oauth2LoginSuccessHandler;
    private final CustomOidcUserService customOidcUserService;

    public SecurityConfig(UserDetailsService userDetailsService, @Lazy CustomOAuth2UserService customOAuth2UserService, @Lazy CustomOidcUserService customOidcUserService, @Lazy OAuth2LoginSuccessHandler oauth2LoginSuccessHandler) {
        this.userDetailsService = userDetailsService;
        this.customOAuth2UserService = customOAuth2UserService;
        this.customOidcUserService = customOidcUserService;
        this.oauth2LoginSuccessHandler = oauth2LoginSuccessHandler;
    }

    // BCrypt para cifrado
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Autenticación basada en BD
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        // En la versión del proyecto el DaoAuthenticationProvider exige el UserDetailsService en el constructor
        DaoAuthenticationProvider auth = new DaoAuthenticationProvider(userDetailsService);
        auth.setPasswordEncoder(passwordEncoder());
        return auth;
    }

    // Seguridad de rutas + configuración de login
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {

        http
                .csrf(csrf -> csrf.disable())
                .authenticationProvider(authenticationProvider())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/carrito/**", "/pedido/**").hasRole("USER")
                        .requestMatchers("/perfil/**").hasAnyRole("USER","ADMIN")
                        // Permitir recursos estáticos y endpoints públicos
                        .requestMatchers("/auth/**", "/", "/css/**", "/img/**", "/imagen/**", "/images/**", "/accesos/**", "/uploads/**", "/fonts/**", "/js/**", "/favicon.ico").permitAll()
                        // Permitir acceso a Swagger UI y documentación OpenAPI
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html", "/api-docs/**").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/auth/login")
                        .loginProcessingUrl("/auth/login-process")
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/auth/login?error=true")
                        .permitAll()
                )
                .oauth2Login(oauth -> oauth
                        .loginPage("/auth/login")
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService).oidcUserService(customOidcUserService))
                         .successHandler(oauth2LoginSuccessHandler)
                )
                .logout(logout -> logout
                        .logoutUrl("/auth/logout")
                        .logoutSuccessUrl("/auth/login?logout=true")
                        .permitAll()
                )
                // Manejo de AccessDenied (403) -> reenviar a /error/403 para que Thymeleaf muestre error/403.html
                .exceptionHandling(ex -> ex.accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(403);
                    try {
                        request.getRequestDispatcher("/error/403").forward(request, response);
                    } catch (ServletException | IOException e) {
                        throw new RuntimeException(e);
                    }
                }));

        return http.build();
    }
}
