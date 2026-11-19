package com.levelup.backend.service;

import com.levelup.backend.dto.BoletaDto;
import com.levelup.backend.dto.BoletaRequest;
import com.levelup.backend.dto.ItemBoletaRequest;
import com.levelup.backend.exception.BusinessException;
import com.levelup.backend.exception.ConflictException;
import com.levelup.backend.exception.ResourceNotFoundException;
import com.levelup.backend.model.Boleta;
import com.levelup.backend.model.Categoria;
import com.levelup.backend.model.Producto;
import com.levelup.backend.model.Rol;
import com.levelup.backend.model.Usuario;
import com.levelup.backend.repository.BoletaRepository;
import com.levelup.backend.repository.ProductoRepository;
import com.levelup.backend.repository.UsuarioRepository;
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
class BoletaServiceTest {

    private static final String CORREO = "ana@duoc.cl";

    @Mock
    private BoletaRepository boletaRepository;
    @Mock
    private ProductoRepository productoRepository;
    @Mock
    private UsuarioRepository usuarioRepository;

    private BoletaService servicio;
    private Usuario usuario;
    private Categoria categoria;

    @BeforeEach
    void preparar() {
        servicio = new BoletaService(boletaRepository, productoRepository, usuarioRepository);
        categoria = new Categoria("Accesorios");
        categoria.setId(1L);
        usuario = crearUsuario(20);
        when(usuarioRepository.findByCorreoIgnoreCase(CORREO)).thenReturn(Optional.of(usuario));
        when(boletaRepository.save(any(Boleta.class))).thenAnswer(inv -> {
            Boleta b = inv.getArgument(0);
            b.setId(100L);
            return b;
        });
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private Usuario crearUsuario(int descuento) {
        Usuario u = new Usuario();
        u.setId(5L);
        u.setNombre("Ana");
        u.setApellidos("Pérez Soto");
        u.setCorreo(CORREO);
        u.setRol(Rol.CLIENTE);
        u.setDescuento(descuento);
        u.setEsDuoc(descuento == 20);
        u.setPuntosLevelUp(0);
        return u;
    }

    private Producto producto(Long id, String nombre, int precio, int stock) {
        Producto p = new Producto();
        p.setId(id);
        p.setNombre(nombre);
        p.setPrecio(precio);
        p.setStock(stock);
        p.setCategoria(categoria);
        when(productoRepository.findByIdForUpdate(id)).thenReturn(Optional.of(p));
        return p;
    }

    private BoletaRequest pedido(ItemBoletaRequest... items) {
        return new BoletaRequest(List.of(items));
    }

    @Test
    void aplicaDescuentoDuocDel20PorCientoYCalculaPuntos() {
        producto(1L, "Mouse", 100000, 10);

        BoletaDto dto = servicio.crear(CORREO, pedido(new ItemBoletaRequest(1L, 3)));

        assertEquals(300000, dto.subtotal());
        assertEquals(60000, dto.descuento());
        assertEquals(240000, dto.total());
        assertEquals(240, dto.puntosGanados());
        assertEquals("Ana Pérez Soto", dto.usuarioNombre());
        assertEquals(240, usuario.getPuntosLevelUp());
    }

    @Test
    void descuentoSeRedondeaHaciaArribaEnLaMitad() {
        usuario.setDescuento(15);
        producto(1L, "Cable", 10010, 10);

        BoletaDto dto = servicio.crear(CORREO, pedido(new ItemBoletaRequest(1L, 1)));

        // 10010 * 15% = 1501,5 -> 1502
        assertEquals(1502, dto.descuento());
        assertEquals(8508, dto.total());
        assertEquals(8, dto.puntosGanados());
    }

    @Test
    void puntosSonLaParteEnteraDelTotalDividido1000() {
        usuario.setDescuento(0);
        producto(1L, "Polera", 14990, 10);

        BoletaDto dto = servicio.crear(CORREO, pedido(new ItemBoletaRequest(1L, 1)));

        assertEquals(0, dto.descuento());
        assertEquals(14990, dto.total());
        assertEquals(14, dto.puntosGanados());
    }

    @Test
    void descuentaStockDelProducto() {
        Producto p = producto(1L, "Mouse", 49990, 20);

        servicio.crear(CORREO, pedido(new ItemBoletaRequest(1L, 4)));

        assertEquals(16, p.getStock());
    }

    @Test
    void stockInsuficienteLanzaConflictoYNoGuarda() {
        producto(1L, "Mouse", 49990, 2);

        ConflictException ex = assertThrows(ConflictException.class,
                () -> servicio.crear(CORREO, pedido(new ItemBoletaRequest(1L, 5))));

        assertEquals("Stock insuficiente para Mouse (disponible: 2).", ex.getMessage());
        verify(boletaRepository, never()).save(any(Boleta.class));
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void idsRepetidosSeSuman() {
        Producto p = producto(1L, "Mouse", 10000, 20);

        BoletaDto dto = servicio.crear(CORREO,
                pedido(new ItemBoletaRequest(1L, 2), new ItemBoletaRequest(1L, 3)));

        assertEquals(1, dto.detalles().size());
        assertEquals(5, dto.detalles().get(0).cantidad());
        assertEquals(50000, dto.subtotal());
        assertEquals(15, p.getStock());
    }

    @Test
    void listaVaciaLanzaErrorDeNegocio() {
        assertThrows(BusinessException.class, () -> servicio.crear(CORREO, new BoletaRequest(List.of())));
        assertThrows(BusinessException.class, () -> servicio.crear(CORREO, new BoletaRequest(null)));
        verify(boletaRepository, never()).save(any(Boleta.class));
    }

    @Test
    void cantidadMenorAUnoLanzaErrorDeNegocio() {
        producto(1L, "Mouse", 10000, 20);

        assertThrows(BusinessException.class,
                () -> servicio.crear(CORREO, pedido(new ItemBoletaRequest(1L, 0))));
    }

    @Test
    void productoInexistenteLanzaNoEncontrado() {
        when(productoRepository.findByIdForUpdate(77L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> servicio.crear(CORREO, pedido(new ItemBoletaRequest(77L, 1))));
        assertEquals("No existe el producto 77", ex.getMessage());
    }
}
