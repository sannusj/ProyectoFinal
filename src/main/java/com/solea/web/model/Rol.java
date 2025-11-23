package com.solea.web.model;

public enum Rol {
    ADMIN("Administrador"),
    USER("Usuario");

    private final String descripcion;

    Rol(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
