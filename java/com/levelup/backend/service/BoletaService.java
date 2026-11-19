package com.levelup.backend.service;

import com.levelup.backend.dto.BoletaDto;
import com.levelup.backend.dto.BoletaRequest;
import com.levelup.backend.dto.DetalleBoletaDto;
import com.levelup.backend.dto.ItemBoletaRequest;
import com.levelup.backend.exception.BusinessException;
import com.levelup.backend.exception.ConflictException;
import com.levelup.backend.exception.ResourceNotFoundException;
import com.levelup.backend.model.Boleta;
import com.levelup.backend.model.DetalleBoleta;
import com.levelup.backend.model.Producto;
import com.levelup.backend.model.Rol;
import com.levelup.backend.model.Usuario;
import com.levelup.backend.repository.BoletaRepository;
import com.levelup.backend.repository.ProductoRepository;
import com.levelup.backend.repository.UsuarioRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
public class BoletaService {

    private final BoletaRepository boletaRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;

    public BoletaService(BoletaRepository boletaRepository,
                         ProductoRepository productoRepository,
                         UsuarioRepository usuarioRepository) {
        this.boletaRepository = boletaRepository;
        this.productoRepository = productoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public BoletaDto crear(String correo, BoletaRequest req) {
        if (req == null || req.items() == null || req.items().isEmpty()) {
            throw new BusinessException("La boleta debe incluir al menos un producto.");
        }

        // Se suman las cantidades de ids repetidos; TreeMap deja los ids ordenados (evita interbloqueos).
        Map<Long, Integer> cantidades = new TreeMap<>();
        for (ItemBoletaRequest item : req.items()) {
            if (item == null || item.productoId() == null) {
                throw new BusinessException("Cada ítem debe indicar el producto.");
            }
            if (item.cantidad() == null || item.cantidad() < 1) {
                throw new BusinessException("La cantidad debe ser al menos 1.");
            }
            cantidades.merge(item.productoId(), item.cantidad(), Integer::sum);
        }

        Usuario usuario = usuarioRepository.findByCorreoIgnoreCase(correo)
                .orElseThrow(() -> new ResourceNotFoundException("No existe el usuario con correo " + correo));

        Boleta boleta = new Boleta();
        boleta.setFecha(LocalDateTime.now());
        boleta.setUsuario(usuario);

        long subtotal = 0;
        for (Map.Entry<Long, Integer> entrada : cantidades.entrySet()) {
            Long productoId = entrada.getKey();
            int cantidad = entrada.getValue();

            Producto producto = productoRepository.findByIdForUpdate(productoId)
                    .orElseThrow(() -> new ResourceNotFoundException("No existe el producto " + productoId));
            if (producto.getStock() < cantidad) {
                throw new ConflictException("Stock insuficiente para " + producto.getNombre()
                        + " (disponible: " + producto.getStock() + ").");
            }
            producto.setStock(producto.getStock() - cantidad);

            long subtotalLinea = producto.getPrecio().longValue() * cantidad;
            DetalleBoleta detalle = new DetalleBoleta();
            detalle.setProducto(producto);
            detalle.setCantidad(cantidad);
            detalle.setPrecioUnitario(producto.getPrecio());
            detalle.setSubtotal((int) subtotalLinea);
            boleta.agregarDetalle(detalle);

            subtotal += subtotalLinea;
        }

        long descuento = (subtotal * usuario.getDescuento() + 50) / 100;
        long total = subtotal - descuento;
        int puntos = (int) (total / 1000);

        boleta.setSubtotal((int) subtotal);
        boleta.setDescuento((int) descuento);
        boleta.setTotal((int) total);
        boleta.setPuntosGanados(puntos);

        Boleta guardada = boletaRepository.save(boleta);

        usuario.setPuntosLevelUp(usuario.getPuntosLevelUp() + puntos);
        usuarioRepository.save(usuario);

        return aDto(guardada);
    }

    @Transactional(readOnly = true)
    public List<BoletaDto> misCompras(String correo) {
        Usuario usuario = usuarioRepository.findByCorreoIgnoreCase(correo)
                .orElseThrow(() -> new ResourceNotFoundException("No existe el usuario con correo " + correo));
        return boletaRepository.findByUsuarioIdOrderByFechaDescIdDesc(usuario.getId())
                .stream().map(this::aDto).toList();
    }

    @Transactional(readOnly = true)
    public List<BoletaDto> listarTodas() {
        return boletaRepository.findAllByOrderByFechaDescIdDesc().stream().map(this::aDto).toList();
    }

    @Transactional(readOnly = true)
    public BoletaDto obtener(Long id, String correo) {
        Boleta boleta = boletaRepository.findConDetallesById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe la boleta " + id));
        Usuario solicitante = usuarioRepository.findByCorreoIgnoreCase(correo)
                .orElseThrow(() -> new AccessDeniedException("Usuario no autorizado."));
        boolean personal = solicitante.getRol() == Rol.ADMIN || solicitante.getRol() == Rol.VENDEDOR;
        boolean esDueno = boleta.getUsuario().getId().equals(solicitante.getId());
        if (!personal && !esDueno) {
            throw new AccessDeniedException("No puedes ver una boleta que no es tuya.");
        }
        return aDto(boleta);
    }

    private BoletaDto aDto(Boleta b) {
        Usuario u = b.getUsuario();
        List<DetalleBoletaDto> detalles = b.getDetalles().stream()
                .map(d -> new DetalleBoletaDto(
                        d.getProducto().getId(),
                        d.getProducto().getNombre(),
                        d.getCantidad(),
                        d.getPrecioUnitario(),
                        d.getSubtotal()))
                .toList();
        return new BoletaDto(
                b.getId(),
                b.getFecha() == null ? null : b.getFecha().toString(),
                u.getId(),
                u.getNombre() + " " + u.getApellidos(),
                u.getCorreo(),
                b.getSubtotal(),
                b.getDescuento(),
                b.getTotal(),
                b.getPuntosGanados(),
                detalles
        );
    }
}
