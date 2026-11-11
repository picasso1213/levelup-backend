package com.levelup.backend.dto;

public record AuthResponse(String token, UsuarioDto usuario, boolean conReferido) {
}
