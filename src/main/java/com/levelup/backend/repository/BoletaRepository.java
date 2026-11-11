package com.levelup.backend.repository;

import com.levelup.backend.model.Boleta;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BoletaRepository extends JpaRepository<Boleta, Long> {

    boolean existsByUsuarioId(Long usuarioId);

    @EntityGraph(attributePaths = {"usuario", "detalles", "detalles.producto"})
    List<Boleta> findByUsuarioIdOrderByFechaDescIdDesc(Long usuarioId);

    @EntityGraph(attributePaths = {"usuario", "detalles", "detalles.producto"})
    List<Boleta> findAllByOrderByFechaDescIdDesc();

    @EntityGraph(attributePaths = {"usuario", "detalles", "detalles.producto"})
    Optional<Boleta> findConDetallesById(Long id);
}
