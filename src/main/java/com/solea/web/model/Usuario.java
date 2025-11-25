package com.solea.web.model;

import jakarta.persistence.*;
import io.swagger.v3.oas.annotations.media.Schema;

@Entity
@Table(name = "usuarios")
@Schema(description = "Entidad de usuario del sistema")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID único del usuario", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer id;

    @Schema(description = "Nombre completo del usuario", example = "Juan Pérez García", required = true)
    private String nombre;

    @Column(unique = true, nullable = false)
    @Schema(description = "Correo electrónico del usuario (identificador único)", example = "juan.perez@ejemplo.com", required = true)
    private String email;

    @Column(nullable = true)
    @Schema(description = "Contraseña del usuario (será encriptada)", example = "MiPassword123!", required = true, format = "password")
    private String pass;

    @Schema(description = "Número de teléfono del usuario", example = "+57 300 123 4567")
    private String tel;

    @Schema(description = "País de residencia del usuario", example = "Colombia")
    private String pais;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rol rol = Rol.USER; // valor por defecto

    @Lob
    private byte[] avatar;

    // Proveedor de autenticación (LOCAL, GOOGLE, ...)
    public enum Provider { LOCAL, GOOGLE }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Provider provider = Provider.LOCAL;

    // id del usuario en el proveedor OAuth (p.ej. sub de Google)
    @Column(name = "oauth_id")
    private String oauthId;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPass() { return pass; }
    public void setPass(String pass) { this.pass = pass; }

    public String getTel() { return tel; }
    public void setTel(String tel) { this.tel = tel; }

    public String getPais() { return pais; }
    public void setPais(String pais) { this.pais = pais; }

    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }

    public byte[] getAvatar() { return avatar; }
    public void setAvatar(byte[] avatar) { this.avatar = avatar; }

    public Provider getProvider() { return provider; }
    public void setProvider(Provider provider) { this.provider = provider; }

    public String getOauthId() { return oauthId; }
    public void setOauthId(String oauthId) { this.oauthId = oauthId; }
}
