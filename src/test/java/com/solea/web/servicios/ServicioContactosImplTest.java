package com.solea.web.servicios;

import com.solea.web.model.Contacto;
import com.solea.web.servicios.impl.ServicioContactosImpl;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ServicioContactosImplTest {

    @Test
    public void testGuardarCreaArchivoYEscribe() throws Exception {
        ServicioContactosImpl svc = new ServicioContactosImpl();
        // For test, we set uploadsDir via reflection (simple approach)
        java.lang.reflect.Field f = ServicioContactosImpl.class.getDeclaredField("uploadsDir");
        f.setAccessible(true);
        f.set(svc, "uploads/pruebas");

        Contacto c = new Contacto("Test User", "test@example.com", "Prueba", "Mensaje de prueba", LocalDateTime.now());
        svc.guardar(c);

        Path p = Path.of("uploads").resolve("contactos.txt");
        assertTrue(Files.exists(p), "El archivo contactos.txt debe existir");

        String content = Files.readString(p);
        assertTrue(content.contains("Test User"), "El contenido debe incluir el nombre");
        assertTrue(content.contains("Mensaje de prueba"), "El contenido debe incluir el mensaje");
    }
}

