package com.levelup.backend.service;

import com.levelup.backend.dto.AuthResponse;
import com.levelup.backend.dto.LoginRequest;
import com.levelup.backend.dto.RegistroRequest;
import com.levelup.backend.exception.BusinessException;
import com.levelup.backend.exception.ConflictException;
import com.levelup.backend.model.Rol;
import com.levelup.backend.model.Usuario;
import com.levelup.backend.repository.UsuarioRepository;
import com.levelup.backend.security.JwtService;
import com.levelup.backend.util.CodigoReferidoUtil;
import com.levelup.backend.util.RunUtil;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class AuthService {

    static final int PUNTOS_NUEVO_CON_REFERIDO = 20;
    static final int PUNTOS_REFERENTE = 50;
    static final int DESCUENTO_DUOC = 20;

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UsuarioMapper usuarioMapper;

    public AuthService(UsuarioRepository usuarioRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       UsuarioMapper usuarioMapper) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.usuarioMapper = usuarioMapper;
    }

    @Transactional
    public AuthResponse registrar(RegistroRequest req) {
        String correo = req.correo().trim().toLowerCase();
        String run = RunUtil.normalizar(req.run());

        if (usuarioRepository.existsByCorreoIgnoreCase(correo)) {
            String msg = "Ya existe una cuenta registrada con ese correo.";
            throw new ConflictException(msg, Map.of("correo", msg));
        }
        if (usuarioRepository.existsByRun(run)) {
            String msg = "Ya existe una cuenta registrada con ese RUN.";
            throw new ConflictException(msg, Map.of("run", msg));
        }

        Usuario referente = null;
        String codigoIngresado = req.codigoReferido();
        if (codigoIngresado != null && !codigoIngresado.isBlank()) {
            String codigo = codigoIngresado.trim().toUpperCase();
            referente = usuarioRepository.findByCodigoReferido(codigo).orElse(null);
            if (referente == null) {
                String msg = "Ese código de referido no existe.";
                throw new BusinessException(msg, Map.of("codigoReferido", msg));
            }
        }

        boolean esDuoc = correo.endsWith("@duoc.cl") || correo.endsWith("@profesor.duoc.cl");

        Usuario usuario = new Usuario();
        usuario.setRun(run);
        usuario.setNombre(req.nombre().trim());
        usuario.setApellidos(req.apellidos().trim());
        usuario.setCorreo(correo);
        usuario.setPassword(passwordEncoder.encode(req.password()));
        usuario.setFechaNacimiento(req.fechaNacimiento());
        usuario.setTelefono(req.telefono() == null || req.telefono().isBlank() ? null : req.telefono().trim());
        usuario.setRegion(req.region().trim());
        usuario.setComuna(req.comuna().trim());
        usuario.setDireccion(req.direccion().trim());
        usuario.setRol(Rol.CLIENTE);
        usuario.setEsDuoc(esDuoc);
        usuario.setDescuento(esDuoc ? DESCUENTO_DUOC : 0);
        usuario.setCodigoReferido(generarCodigoUnico(usuario.getNombre(), usuario.getApellidos()));
        usuario.setFechaRegistro(LocalDateTime.now());

        boolean conReferido = referente != null;
        if (conReferido) {
            usuario.setReferidoPor(referente.getCorreo());
            usuario.setPuntosLevelUp(PUNTOS_NUEVO_CON_REFERIDO);
            referente.setPuntosLevelUp(referente.getPuntosLevelUp() + PUNTOS_REFERENTE);
            usuarioRepository.save(referente);
        }

        Usuario guardado = usuarioRepository.save(usuario);
        String token = jwtService.generarToken(guardado);
        return new AuthResponse(token, usuarioMapper.aDto(guardado), conReferido);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req) {
        String mensaje = "Correo o contraseña incorrectos.";
        Usuario usuario = usuarioRepository.findByCorreoIgnoreCase(req.correo().trim())
                .orElseThrow(() -> new BadCredentialsException(mensaje));
        if (!passwordEncoder.matches(req.password(), usuario.getPassword())) {
            throw new BadCredentialsException(mensaje);
        }
        String token = jwtService.generarToken(usuario);
        return new AuthResponse(token, usuarioMapper.aDto(usuario), false);
    }

    private String generarCodigoUnico(String nombre, String apellidos) {
        String codigo;
        do {
            codigo = CodigoReferidoUtil.generar(nombre, apellidos);
        } while (usuarioRepository.existsByCodigoReferido(codigo));
        return codigo;
    }
}
