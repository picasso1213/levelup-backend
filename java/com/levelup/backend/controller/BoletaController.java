package com.levelup.backend.controller;

import com.levelup.backend.dto.BoletaDto;
import com.levelup.backend.dto.BoletaRequest;
import com.levelup.backend.service.BoletaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/boletas")
@Tag(name = "Boletas", description = "Compras y boletas")
public class BoletaController {

    private final BoletaService boletaService;

    public BoletaController(BoletaService boletaService) {
        this.boletaService = boletaService;
    }

    @PostMapping
    @Operation(summary = "Crear una boleta (compra) del usuario autenticado")
    public ResponseEntity<BoletaDto> crear(Authentication authentication,
                                           @Valid @RequestBody BoletaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(boletaService.crear(authentication.getName(), request));
    }

    @GetMapping("/mis-compras")
    @Operation(summary = "Listar las boletas del usuario autenticado")
    public List<BoletaDto> misCompras(Authentication authentication) {
        return boletaService.misCompras(authentication.getName());
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','VENDEDOR')")
    @Operation(summary = "Listar todas las boletas (ADMIN y VENDEDOR)")
    public List<BoletaDto> listar() {
        return boletaService.listarTodas();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una boleta (ADMIN, VENDEDOR o dueño)")
    public BoletaDto obtener(@PathVariable Long id, Authentication authentication) {
        return boletaService.obtener(id, authentication.getName());
    }
}
