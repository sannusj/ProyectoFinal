package com.solea.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

    public SecurityConfig(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
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
                        .requestMatchers("/auth/**", "/", "/css/**", "/img/**").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/auth/login")
                        .loginProcessingUrl("/auth/login-process")
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/auth/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/auth/logout")
                        .logoutSuccessUrl("/auth/login?logout=true")
                        .permitAll()
                );

        return http.build();
    }
}
