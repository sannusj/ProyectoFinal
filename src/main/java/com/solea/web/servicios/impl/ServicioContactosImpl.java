package com.solea.web.servicios.impl;

import com.solea.web.model.Contacto;
import com.solea.web.servicios.ServicioContactos;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;

@Service
public class ServicioContactosImpl implements ServicioContactos {

    @Value("${app.upload.dir:uploads}")
    private String uploadsDir;

    private Path contactosFile;

    // Inicialización perezosa: creamos directorio/archivo al guardar por primera vez
    private void ensureInit() throws Exception {
        if (contactosFile != null) return;

        Path uploadsPath = Path.of(uploadsDir);
        if (uploadsPath.getParent() != null) {
            // si app.upload.dir = uploads/prendas -> queremos uploads
            uploadsPath = uploadsPath.getParent();
        }

        if (!Files.exists(uploadsPath)) {
            Files.createDirectories(uploadsPath);
        }

        contactosFile = uploadsPath.resolve("contactos.txt");
        if (!Files.exists(contactosFile)) {
            Files.createFile(contactosFile);
        }
    }

    @Override
    public void guardar(Contacto c) throws Exception {
        ensureInit();
        // Añadir entrada legible con fecha
        try (FileWriter fw = new FileWriter(contactosFile.toFile(), true);
             PrintWriter pw = new PrintWriter(fw)) {

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            pw.println("Fecha: " + c.getFecha().format(fmt));
            pw.println("Nombre: " + c.getNombre());
            pw.println("Email: " + c.getEmail());
            pw.println("Asunto: " + c.getAsunto());
            pw.println("Mensaje: ");
            pw.println(c.getMensaje());
            pw.println("-------------------------------");
        }
    }
}
