package com.solea.web.model;

import java.time.LocalDateTime;

public class Contacto {
    private String nombre;
    private String email;
    private String asunto;
    private String mensaje;
    private LocalDateTime fecha;

    public Contacto() {}

    public Contacto(String nombre, String email, String asunto, String mensaje, LocalDateTime fecha) {
        this.nombre = nombre;
        this.email = email;
        this.asunto = asunto;
        this.mensaje = mensaje;
        this.fecha = fecha;
    }

    // getters y setters
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getAsunto() { return asunto; }
    public void setAsunto(String asunto) { this.asunto = asunto; }
    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    @Override
    public String toString() {
        return "Fecha: " + fecha + "\nNombre: " + nombre + "\nEmail: " + email + "\nAsunto: " + asunto + "\nMensaje: " + mensaje + "\n-----------------------------\n";
    }
}

