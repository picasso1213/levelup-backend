package com.levelup.backend.controller;

import com.levelup.backend.dto.ActualizarPerfilRequest;
import com.levelup.backend.dto.CambiarRolRequest;
import com.levelup.backend.dto.CanjeRequest;
import com.levelup.backend.dto.CanjeResponse;
import com.levelup.backend.dto.UsuarioDto;
import com.levelup.backend.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/usuarios")
@Tag(name = "Usuarios", description = "Perfil, puntos Level-Up y administración de usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/me")
    @Operation(summary = "Obtener el perfil del usuario autenticado")
    public UsuarioDto miPerfil(Authentication authentication) {
        return usuarioService.obtenerPerfil(authentication.getName());
    }

    @PutMapping("/me")
    @Operation(summary = "Actualizar el perfil del usuario autenticado")
    public UsuarioDto actualizarPerfil(Authentication authentication,
                                       @Valid @RequestBody ActualizarPerfilRequest request) {
        return usuarioService.actualizarPerfil(authentication.getName(), request);
    }

    @PostMapping("/me/canjes")
    @Operation(summary = "Canjear puntos Level-Up por un beneficio")
    public CanjeResponse canjear(Authentication authentication, @Valid @RequestBody CanjeRequest request) {
        return usuarioService.canjear(authentication.getName(), request.idCanje());
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar usuarios (ADMIN)")
    public List<UsuarioDto> listar() {
        return usuarioService.listar();
    }

    @PutMapping("/{id}/rol")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cambiar el rol de un usuario (ADMIN)")
    public UsuarioDto cambiarRol(@PathVariable Long id,
                                 @Valid @RequestBody CambiarRolRequest request,
                                 Authentication authentication) {
        return usuarioService.cambiarRol(id, request.rol(), authentication.getName());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar un usuario (ADMIN)")
    public ResponseEntity<Void> eliminar(@PathVariable Long id, Authentication authentication) {
        usuarioService.eliminar(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
