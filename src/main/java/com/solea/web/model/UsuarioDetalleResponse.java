package com.solea.web.model;

public class UsuarioDetalleResponse {
    private int id;
    private String nombre;
    private String email;
    private String tel;
    private String pais;
    private boolean tieneAvatar;
    private String rol; // agregado para exponer el rol como texto

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTel() { return tel; }
    public void setTel(String tel) { this.tel = tel; }
    public String getPais() { return pais; }
    public void setPais(String pais) { this.pais = pais; }
    public boolean isTieneAvatar() { return tieneAvatar; }
    public void setTieneAvatar(boolean tieneAvatar) { this.tieneAvatar = tieneAvatar; }

    // Getter y setter para el rol
    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
}
