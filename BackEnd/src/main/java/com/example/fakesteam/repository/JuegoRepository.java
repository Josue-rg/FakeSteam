package com.example.fakesteam.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.fakesteam.entity.Juego;

public interface JuegoRepository extends JpaRepository<Juego, Long> {
    List<Juego> findByNombreContaining(String nombre);
    List<Juego> findByCategoria(String categoria);
}
