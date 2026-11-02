package com.example.fakesteam.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "juegos")
public class Juego {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    private double precio;
    // La imagen se almacena en la misma BD (tipo bytea)
    @Lob
    private byte[] img;
    private String categoria;

    public Juego() {}

    public Juego(Long id, String nombre, double precio, byte[] img, String categoria) {
        this.id = id;
        this.nombre = nombre;
        this.precio = precio;
        this.img = img;
        this.categoria = categoria;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }

    public byte[] getImg() { return img; }
    public void setImg(byte[] img) { this.img = img; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
}
