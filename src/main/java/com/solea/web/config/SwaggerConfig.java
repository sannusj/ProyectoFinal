package com.solea.web.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Solea - Sistema de E-commerce")
                        .version("1.0.0")
                        .description("API REST completa para el sistema de comercio electrónico Solea. " +
                                    "Incluye gestión de usuarios, productos, carrito de compras, pedidos, " +
                                    "autenticación, administración y más. " +
                                    "\n\n**Características principales:**\n" +
                                    "- Autenticación con Spring Security\n" +
                                    "- Gestión de productos con categorías\n" +
                                    "- Carrito de compras con cálculo de totales\n" +
                                    "- Proceso de checkout en múltiples pasos\n" +
                                    "- Panel de administración completo\n" +
                                    "- Gestión de perfiles de usuario\n" +
                                    "- Sistema de contacto\n\n" +
                                    "**Nota:** Los formularios en esta documentación tienen ejemplos pre-cargados. " +
                                    "Puedes modificar los valores o usar el botón 'Try it out' para probar con los ejemplos.")
                        .contact(new Contact()
                                .name("Equipo Solea")
                                .email("soporte@solea.com")
                                .url("https://solea.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .components(new Components()
                        .addSecuritySchemes("session-auth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.COOKIE)
                                        .name("JSESSIONID")
                                        .description("Autenticación basada en sesión de Spring Security. " +
                                                   "Después de hacer login, la sesión se mantiene automáticamente mediante cookies.")));
    }
}
