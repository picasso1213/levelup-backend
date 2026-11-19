package com.levelup.backend.service;

import com.levelup.backend.dto.ActualizarPerfilRequest;
import com.levelup.backend.dto.CanjeDto;
import com.levelup.backend.dto.CanjeResponse;
import com.levelup.backend.dto.UsuarioDto;
import com.levelup.backend.exception.BusinessException;
import com.levelup.backend.exception.ConflictException;
import com.levelup.backend.exception.ResourceNotFoundException;
import com.levelup.backend.model.Rol;
import com.levelup.backend.model.Usuario;
import com.levelup.backend.repository.BoletaRepository;
import com.levelup.backend.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class UsuarioService {

    private static final Map<String, CanjeDto> CATALOGO_CANJES = new LinkedHashMap<>();

    static {
        CATALOGO_CANJES.put("cupon5", new CanjeDto("cupon5", "Cupón 5% de descuento", 100));
        CATALOGO_CANJES.put("cupon10", new CanjeDto("cupon10", "Cupón 10% de descuento", 250));
        CATALOGO_CANJES.put("mouse", new CanjeDto("mouse", "Mouse Logitech G502 HERO gratis", 400));
        CATALOGO_CANJES.put("polera", new CanjeDto("polera", "Polera Personalizada Level-Up gratis", 600));
    }

    private final UsuarioRepository usuarioRepository;
    private final BoletaRepository boletaRepository;
    private final UsuarioMapper usuarioMapper;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          BoletaRepository boletaRepository,
                          UsuarioMapper usuarioMapper) {
        this.usuarioRepository = usuarioRepository;
        this.boletaRepository = boletaRepository;
        this.usuarioMapper = usuarioMapper;
    }

    @Transactional(readOnly = true)
    public UsuarioDto obtenerPerfil(String correo) {
        return usuarioMapper.aDto(buscarPorCorreo(correo));
    }

    @Transactional
    public UsuarioDto actualizarPerfil(String correo, ActualizarPerfilRequest req) {
        Usuario usuario = buscarPorCorreo(correo);
        usuario.setTelefono(req.telefono() == null || req.telefono().isBlank() ? null : req.telefono().trim());
        usuario.setDireccion(req.direccion().trim());
        usuario.setRegion(req.region().trim());
        usuario.setComuna(req.comuna().trim());
        usuario.setPreferencias(UsuarioMapper.unirPreferencias(req.preferencias()));
        return usuarioMapper.aDto(usuarioRepository.save(usuario));
    }

    @Transactional
    public CanjeResponse canjear(String correo, String idCanje) {
        CanjeDto canje = CATALOGO_CANJES.get(idCanje);
        if (canje == null) {
            throw new ResourceNotFoundException("No existe el canje " + idCanje);
        }
        Usuario usuario = buscarPorCorreo(correo);
        if (usuario.getPuntosLevelUp() < canje.costo()) {
            throw new BusinessException("No tienes puntos suficientes para este canje.");
        }
        usuario.setPuntosLevelUp(usuario.getPuntosLevelUp() - canje.costo());
        Usuario guardado = usuarioRepository.save(usuario);
        return new CanjeResponse(canje, usuarioMapper.aDto(guardado));
    }

    @Transactional(readOnly = true)
    public List<UsuarioDto> listar() {
        return usuarioRepository.findAll().stream().map(usuarioMapper::aDto).toList();
    }

    @Transactional
    public UsuarioDto cambiarRol(Long id, String rolTexto, String correoActual) {
        Usuario usuario = buscarPorId(id);
        if (usuario.getCorreo().equalsIgnoreCase(correoActual)) {
            throw new BusinessException("No puedes cambiar tu propio rol.");
        }
        Rol rol = parsearRol(rolTexto);
        usuario.setRol(rol);
        return usuarioMapper.aDto(usuarioRepository.save(usuario));
    }

    @Transactional
    public void eliminar(Long id, String correoActual) {
        Usuario usuario = buscarPorId(id);
        if (usuario.getCorreo().equalsIgnoreCase(correoActual)) {
            throw new BusinessException("No puedes eliminar tu propia cuenta.");
        }
        if (boletaRepository.existsByUsuarioId(id)) {
            throw new ConflictException("No se puede eliminar un usuario que tiene boletas.");
        }
        usuarioRepository.delete(usuario);
    }

    private Rol parsearRol(String texto) {
        try {
            return Rol.valueOf(texto.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BusinessException("Rol inválido. Valores permitidos: ADMIN, VENDEDOR, CLIENTE.");
        }
    }

    private Usuario buscarPorCorreo(String correo) {
        return usuarioRepository.findByCorreoIgnoreCase(correo)
                .orElseThrow(() -> new ResourceNotFoundException("No existe el usuario con correo " + correo));
    }

    private Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe el usuario " + id));
    }
}
