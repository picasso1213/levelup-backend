package com.levelup.backend.service;

import com.levelup.backend.dto.AuthResponse;
import com.levelup.backend.dto.LoginRequest;
import com.levelup.backend.dto.RegistroRequest;
import com.levelup.backend.exception.BusinessException;
import com.levelup.backend.exception.ConflictException;
import com.levelup.backend.model.Usuario;
import com.levelup.backend.repository.UsuarioRepository;
import com.levelup.backend.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;

    private AuthService servicio;

    @BeforeEach
    void preparar() {
        servicio = new AuthService(usuarioRepository, passwordEncoder, jwtService,
                new UsuarioMapper(usuarioRepository));
        when(passwordEncoder.encode(any(CharSequence.class))).thenReturn("hash");
        when(jwtService.generarToken(any(Usuario.class))).thenReturn("token-falso");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private RegistroRequest registro(String correo, String codigoReferido) {
        return new RegistroRequest("Ana", "Pérez Soto", "123456785", correo, "1234",
                LocalDate.of(2000, 1, 1), null, "Región Metropolitana de Santiago", "Santiago",
                "Calle 123", codigoReferido);
    }

    @Test
    void correoDuplicadoLanzaConflicto() {
        when(usuarioRepository.existsByCorreoIgnoreCase("ana@gmail.com")).thenReturn(true);

        ConflictException ex = assertThrows(ConflictException.class,
                () -> servicio.registrar(registro("ana@gmail.com", null)));

        assertEquals("Ya existe una cuenta registrada con ese correo.", ex.getCampos().get("correo"));
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void referidoInexistenteLanzaErrorDeNegocioConCampo() {
        when(usuarioRepository.findByCodigoReferido("ZZZZ99")).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> servicio.registrar(registro("ana@gmail.com", "zzzz99")));

        assertEquals("Ese código de referido no existe.", ex.getCampos().get("codigoReferido"));
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void correoDuocRecibeDescuentoDel20() {
        AuthResponse resp = servicio.registrar(registro("ana@duoc.cl", null));

        assertTrue(resp.usuario().esDuoc());
        assertEquals(20, resp.usuario().descuento());
        assertEquals("CLIENTE", resp.usuario().rol());
        assertFalse(resp.conReferido());
        assertEquals("token-falso", resp.token());
        assertEquals("123456785", resp.usuario().run());
    }

    @Test
    void correoGmailNoRecibeDescuento() {
        AuthResponse resp = servicio.registrar(registro("ana@gmail.com", null));

        assertFalse(resp.usuario().esDuoc());
        assertEquals(0, resp.usuario().descuento());
        assertEquals(0, resp.usuario().puntosLevelUp());
    }

    @Test
    void referenteGana50YNuevoUsuarioParteCon20() {
        Usuario referente = new Usuario();
        referente.setId(9L);
        referente.setCorreo("ref@gmail.com");
        referente.setPuntosLevelUp(0);
        when(usuarioRepository.findByCodigoReferido("REF123")).thenReturn(Optional.of(referente));

        AuthResponse resp = servicio.registrar(registro("ana@gmail.com", "ref123"));

        assertTrue(resp.conReferido());
        assertEquals(20, resp.usuario().puntosLevelUp());
        assertEquals("ref@gmail.com", resp.usuario().referidoPor());
        assertEquals(50, referente.getPuntosLevelUp());
    }

    @Test
    void loginConClaveIncorrectaLanzaCredencialesInvalidas() {
        Usuario u = new Usuario();
        u.setCorreo("ana@gmail.com");
        u.setPassword("hash");
        when(usuarioRepository.findByCorreoIgnoreCase("ana@gmail.com")).thenReturn(Optional.of(u));
        when(passwordEncoder.matches("mala", "hash")).thenReturn(false);

        BadCredentialsException ex = assertThrows(BadCredentialsException.class,
                () -> servicio.login(new LoginRequest("ana@gmail.com", "mala")));
        assertEquals("Correo o contraseña incorrectos.", ex.getMessage());
    }
}
