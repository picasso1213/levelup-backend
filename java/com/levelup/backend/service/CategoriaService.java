package com.levelup.backend.service;

import com.levelup.backend.dto.CategoriaDto;
import com.levelup.backend.dto.CategoriaRequest;
import com.levelup.backend.exception.ConflictException;
import com.levelup.backend.exception.ResourceNotFoundException;
import com.levelup.backend.model.Categoria;
import com.levelup.backend.repository.CategoriaRepository;
import com.levelup.backend.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;

    public CategoriaService(CategoriaRepository categoriaRepository, ProductoRepository productoRepository) {
        this.categoriaRepository = categoriaRepository;
        this.productoRepository = productoRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoriaDto> listar() {
        return categoriaRepository.findAll().stream().map(this::aDto).toList();
    }

    @Transactional(readOnly = true)
    public CategoriaDto obtener(Long id) {
        return aDto(buscar(id));
    }

    @Transactional
    public CategoriaDto crear(CategoriaRequest req) {
        String nombre = req.nombre().trim();
        if (categoriaRepository.existsByNombreIgnoreCase(nombre)) {
            throw new ConflictException("Ya existe una categoría con ese nombre.");
        }
        return aDto(categoriaRepository.save(new Categoria(nombre)));
    }

    @Transactional
    public CategoriaDto actualizar(Long id, CategoriaRequest req) {
        Categoria categoria = buscar(id);
        String nombre = req.nombre().trim();
        categoriaRepository.findByNombreIgnoreCase(nombre).ifPresent(otra -> {
            if (!otra.getId().equals(id)) {
                throw new ConflictException("Ya existe una categoría con ese nombre.");
            }
        });
        categoria.setNombre(nombre);
        return aDto(categoriaRepository.save(categoria));
    }

    @Transactional
    public void eliminar(Long id) {
        Categoria categoria = buscar(id);
        if (productoRepository.existsByCategoriaId(id)) {
            throw new ConflictException("No se puede eliminar una categoría que tiene productos.");
        }
        categoriaRepository.delete(categoria);
    }

    private Categoria buscar(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe la categoría " + id));
    }

    private CategoriaDto aDto(Categoria c) {
        return new CategoriaDto(c.getId(), c.getNombre());
    }
}
