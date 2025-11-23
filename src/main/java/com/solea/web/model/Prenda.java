package com.solea.web.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "prenda")
public class Prenda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private Double precio;

    @Column(nullable = false)
    private int stock = 0;

    @Column(nullable = false)
    private boolean alta = true;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "imagen_prenda", columnDefinition = "LONGBLOB")
    private byte[] imagenPrenda;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id", nullable = true)
    private Categoria categoria;

    // Nuevo: descripción, talla(s) y ruta de imagen en filesystem
    @Column(columnDefinition = "TEXT")
    private String descripcion;

    private String talla;

    // Ruta relativa o absoluta del archivo de imagen (por ejemplo: uploads/prendas/prenda-1.png)
    private String imagePath;

    public Prenda() {}

    public Prenda(String nombre, Double precio) {
        this.nombre = nombre;
        this.precio = precio;
    }

    // getters & setters con validaciones
    public void setPrecio(Double precio) {
        this.precio = Math.max(0, precio);
    }

    public void setStock(int stock) {
        this.stock = Math.max(0, stock);
    }

    public Integer getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public Double getPrecio() {
        return precio;
    }

    public int getStock() {
        return stock;
    }

    public boolean isAlta() {
        return alta;
    }

    public void setAlta(boolean alta) { this.alta = alta; }

    public byte[] getImagenPrenda() {
        return imagenPrenda;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setImagenPrenda(byte[] imagenPrenda) {
        this.imagenPrenda = imagenPrenda;
    }

    // setter para categoria
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }

    // --- Nuevos getters/setters ---
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getTalla() { return talla; }
    public void setTalla(String talla) { this.talla = talla; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
}
