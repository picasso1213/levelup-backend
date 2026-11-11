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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;
    @Mock
    private CategoriaRepository categoriaRepository;
    @Mock
    private DetalleBoletaRepository detalleBoletaRepository;

    private ProductoService servicio;
    private Categoria consolas;

    @BeforeEach
    void preparar() {
        servicio = new ProductoService(productoRepository, categoriaRepository, detalleBoletaRepository);
        consolas = new Categoria("Consolas");
        consolas.setId(1L);
    }

    private Producto producto(Long id) {
        Producto p = new Producto();
        p.setId(id);
        p.setNombre("PlayStation 5");
        p.setDescripcion("Consola");
        p.setPrecio(549990);
        p.setStock(8);
        p.setImagen("/images/ps5.jpg");
        p.setCategoria(consolas);
        return p;
    }

    private ProductoRequest request(Long categoriaId) {
        return new ProductoRequest("PlayStation 5", "Consola", 549990, 8, "/images/ps5.jpg", categoriaId);
    }

    @Test
    void listarDevuelveProductosConNombreDeCategoria() {
        when(productoRepository.findAllConCategoria()).thenReturn(List.of(producto(1L)));

        List<ProductoDto> lista = servicio.listar();

        assertEquals(1, lista.size());
        assertEquals("Consolas", lista.get(0).categoria());
        assertEquals(1L, lista.get(0).categoriaId());
    }

    @Test
    void obtenerInexistenteLanzaNoEncontrado() {
        when(productoRepository.findConCategoriaById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> servicio.obtener(99L));
        assertEquals("No existe el producto 99", ex.getMessage());
    }

    @Test
    void crearConCategoriaInexistenteLanzaNoEncontrado() {
        when(categoriaRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> servicio.crear(request(99L)));
        assertEquals("No existe la categoría 99", ex.getMessage());
        verify(productoRepository, never()).save(any(Producto.class));
    }

    @Test
    void crearGuardaYDevuelveDto() {
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(consolas));
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> {
            Producto p = inv.getArgument(0);
            p.setId(10L);
            return p;
        });

        ProductoDto dto = servicio.crear(request(1L));

        assertEquals(10L, dto.id());
        assertEquals("PlayStation 5", dto.nombre());
        assertEquals("Consolas", dto.categoria());
        assertEquals(549990, dto.precio());
        assertEquals(8, dto.stock());
    }

    @Test
    void eliminarProductoEnBoletasLanzaConflicto() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto(1L)));
        when(detalleBoletaRepository.existsByProductoId(1L)).thenReturn(true);

        assertThrows(ConflictException.class, () -> servicio.eliminar(1L));
        verify(productoRepository, never()).delete(any(Producto.class));
    }

    @Test
    void eliminarProductoLibreLoBorra() {
        Producto p = producto(1L);
        when(productoRepository.findById(1L)).thenReturn(Optional.of(p));
        when(detalleBoletaRepository.existsByProductoId(1L)).thenReturn(false);

        servicio.eliminar(1L);

        verify(productoRepository).delete(p);
    }
}
