package com.levelup.backend.service;

import com.levelup.backend.dto.ProductoDto;
import com.levelup.backend.dto.ProductoRequest;
import com.levelup.backend.exception.ConflictException;
import com.levelup.backend.exception.ResourceNotFoundException;
import com.levelup.backend.model.Categoria;
import com.levelup.backend.model.Producto;
import com.levelup.backend.repository.CategoriaRepository;
import com.levelup.backend.repository.DetalleBoletaRepository;
import com.levelup.backend.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final DetalleBoletaRepository detalleBoletaRepository;

    public ProductoService(ProductoRepository productoRepository,
                           CategoriaRepository categoriaRepository,
                           DetalleBoletaRepository detalleBoletaRepository) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
        this.detalleBoletaRepository = detalleBoletaRepository;
    }

    @Transactional(readOnly = true)
    public List<ProductoDto> listar() {
        return productoRepository.findAllConCategoria().stream().map(this::aDto).toList();
    }

    @Transactional(readOnly = true)
    public ProductoDto obtener(Long id) {
        Producto producto = productoRepository.findConCategoriaById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe el producto " + id));
        return aDto(producto);
    }

    @Transactional
    public ProductoDto crear(ProductoRequest req) {
        Categoria categoria = buscarCategoria(req.categoriaId());
        Producto producto = new Producto();
        copiarDatos(producto, req, categoria);
        return aDto(productoRepository.save(producto));
    }

    @Transactional
    public ProductoDto actualizar(Long id, ProductoRequest req) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe el producto " + id));
        Categoria categoria = buscarCategoria(req.categoriaId());
        copiarDatos(producto, req, categoria);
        return aDto(productoRepository.save(producto));
    }

    @Transactional
    public void eliminar(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe el producto " + id));
        if (detalleBoletaRepository.existsByProductoId(id)) {
            throw new ConflictException("No se puede eliminar un producto que está en boletas.");
        }
        productoRepository.delete(producto);
    }

    private Categoria buscarCategoria(Long categoriaId) {
        return categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new ResourceNotFoundException("No existe la categoría " + categoriaId));
    }

    private void copiarDatos(Producto producto, ProductoRequest req, Categoria categoria) {
        producto.setNombre(req.nombre().trim());
        producto.setDescripcion(req.descripcion());
        producto.setPrecio(req.precio());
        producto.setStock(req.stock());
        producto.setImagen(req.imagen());
        producto.setCategoria(categoria);
    }

    private ProductoDto aDto(Producto p) {
        Categoria c = p.getCategoria();
        return new ProductoDto(
                p.getId(),
                p.getNombre(),
                c.getNombre(),
                c.getId(),
                p.getPrecio(),
                p.getStock(),
                p.getImagen(),
                p.getDescripcion()
        );
    }
}
