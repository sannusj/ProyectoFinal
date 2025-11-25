package com.solea.web.controladores;

import com.solea.web.model.Contacto;
import com.solea.web.servicios.ServicioContactos;
import com.solea.web.dto.ContactoDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/contacto")
@Tag(
    name = "Contacto", 
    description = "API para gestión de mensajes de contacto del sitio web. " +
                  "Permite a los visitantes y usuarios enviar mensajes al equipo de soporte. " +
                  "Los mensajes se almacenan en la base de datos para su seguimiento y respuesta."
)
public class ContactoController {

    private final ServicioContactos servicioContactos;

    public ContactoController(ServicioContactos servicioContactos) {
        this.servicioContactos = servicioContactos;
    }

    @Operation(
            summary = "Enviar mensaje de contacto",
            description = "Procesa y almacena un mensaje de contacto enviado desde el formulario del sitio web. " +
                         "Valida todos los campos requeridos (nombre, email, asunto, mensaje), " +
                         "registra la fecha y hora del mensaje, y lo guarda en la base de datos para seguimiento. " +
                         "Si el envío es exitoso, limpia el formulario y muestra mensaje de confirmación. " +
                         "En caso de error, mantiene los datos ingresados y muestra mensaje descriptivo.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Formulario de contacto con todos los datos del mensaje",
                    required = true,
                    content = @Content(
                            mediaType = "application/x-www-form-urlencoded",
                            schema = @Schema(implementation = ContactoDto.class)
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", 
                    description = "Mensaje enviado exitosamente - formulario limpiado con mensaje de confirmación"
            ),
            @ApiResponse(
                    responseCode = "200", 
                    description = "Error al enviar - formulario con datos preservados y mensaje de error"
            )
    })
    @PostMapping("/enviar")
    public String enviar(
            @Parameter(hidden = true) @RequestParam String nombre,
            @Parameter(hidden = true) @RequestParam String email,
            @Parameter(hidden = true) @RequestParam String asunto,
            @Parameter(hidden = true) @RequestParam String mensaje,
            Model model) {
        try {
            Contacto c = new Contacto(nombre, email, asunto, mensaje, LocalDateTime.now());
            servicioContactos.guardar(c);
            model.addAttribute("exito", "Mensaje enviado correctamente. Gracias por contactarnos.");

            // limpiar campos en la vista tras éxito
            model.addAttribute("nombre", "");
            model.addAttribute("email", "");
            model.addAttribute("asunto", "");
            model.addAttribute("mensaje", "");
        } catch (Exception ex) {
            model.addAttribute("error", "Ocurrió un error al enviar el mensaje: " + ex.getMessage());

            // mantener valores para que el usuario no los pierda
            model.addAttribute("nombre", nombre);
            model.addAttribute("email", email);
            model.addAttribute("asunto", asunto);
            model.addAttribute("mensaje", mensaje);
        }

        // devolver a la página de contacto
        model.addAttribute("usuario", null);
        return "home/contacto";
    }
}
