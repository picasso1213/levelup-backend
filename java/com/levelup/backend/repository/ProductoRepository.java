package com.levelup.backend.repository;

import com.levelup.backend.model.Producto;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    boolean existsByCategoriaId(Long categoriaId);

    @Query("select p from Producto p join fetch p.categoria order by p.id")
    List<Producto> findAllConCategoria();

    @Query("select p from Producto p join fetch p.categoria where p.id = :id")
    Optional<Producto> findConCategoriaById(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Producto p where p.id = :id")
    Optional<Producto> findByIdForUpdate(@Param("id") Long id);
}
