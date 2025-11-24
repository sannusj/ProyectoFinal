package com.solea.web.controladores;

import com.solea.web.model.Contacto;
import com.solea.web.servicios.ServicioContactos;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/contacto")
public class ContactoController {

    private final ServicioContactos servicioContactos;

    public ContactoController(ServicioContactos servicioContactos) {
        this.servicioContactos = servicioContactos;
    }

    @PostMapping("/enviar")
    public String enviar(@RequestParam String nombre,
                         @RequestParam String email,
                         @RequestParam String asunto,
                         @RequestParam String mensaje,
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
