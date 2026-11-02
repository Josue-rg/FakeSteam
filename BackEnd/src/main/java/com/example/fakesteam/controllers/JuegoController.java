package com.example.fakesteam.controllers;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.fakesteam.entity.Juego;
import com.example.fakesteam.repository.JuegoRepository;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/juegos")
public class JuegoController {

    private static final Logger logger = LoggerFactory.getLogger(JuegoController.class);

    @Autowired
    private JuegoRepository repository;

    // Obtener todos los juegos
    @GetMapping
    public List<Juego> getAll() {
        return repository.findAll();
    }

    @PostMapping("/crear")
    public ResponseEntity<Juego> create(
            @RequestParam("nombre") String nombre,
            @RequestParam("precio") double precio,
            @RequestParam("categoria") String categoria,
            @RequestParam(value = "img", required = false) MultipartFile file) {
        try {
            Juego juego = new Juego();
            juego.setNombre(nombre);
            juego.setPrecio(precio);
            juego.setCategoria(categoria);

            if (file != null && !file.isEmpty()) {
                juego.setImg(file.getBytes());
            } else {
                juego.setImg(null);
            }

            Juego savedJuego = repository.save(juego);
            return ResponseEntity.status(201).body(savedJuego);
        } catch (IOException e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    @PutMapping("/editar/{id}")
    public ResponseEntity<?> updateJuego(
            @PathVariable Long id,
            @RequestParam("nombre") String nombre,
            @RequestParam("precio") double precio,
            @RequestParam("categoria") String categoria,
            @RequestParam(value = "img", required = false) MultipartFile file) {
        try {
            Juego juego = repository.findById(id).orElseThrow(() -> new RuntimeException("Juego no encontrado"));
            juego.setNombre(nombre);
            juego.setPrecio(precio);
            juego.setCategoria(categoria);
            // Manejo de la imagen
            if (file != null && !file.isEmpty()) {
                byte[] imgBytes = file.getBytes();
                juego.setImg(imgBytes);
                
            }

            Juego updatedJuego = repository.save(juego);
            return ResponseEntity.status(200).body(updatedJuego);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error al procesar la imagen: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al actualizar el juego: " + e.getMessage());
        }
    }

    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<Void> deleteJuego(@PathVariable Long id) {
        try {
            Juego juego = repository.findById(id).orElseThrow(() -> new RuntimeException("Juego no encontrado"));
            repository.delete(juego);
            return ResponseEntity.status(204).build(); // 204 No Content
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/imagen/{id}")
    public ResponseEntity<byte[]> getImagen(@PathVariable Long id) {
        try {
            Juego juego = repository.findById(id).orElseThrow(() -> new RuntimeException("Juego no encontrado"));
            if (juego.getImg() == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok()
                    .header("Content-Type", "image/jpeg")
                    .header("Content-Disposition", "inline")
                    .header("Cache-Control", "public, max-age=3600")
                    .body(juego.getImg());
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
}