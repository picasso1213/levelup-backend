package com.levelup.backend.service;

import com.levelup.backend.dto.UsuarioDto;
import com.levelup.backend.model.Usuario;
import com.levelup.backend.repository.UsuarioRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class UsuarioMapper {

    private final UsuarioRepository usuarioRepository;

    public UsuarioMapper(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public UsuarioDto aDto(Usuario u) {
        int cantidadReferidos = (int) usuarioRepository.countByReferidoPor(u.getCorreo());
        return new UsuarioDto(
                u.getId(),
                u.getNombre(),
                u.getApellidos(),
                u.getRun(),
                u.getCorreo(),
                u.getFechaNacimiento() == null ? null : u.getFechaNacimiento().toString(),
                u.getTelefono(),
                u.getRegion(),
                u.getComuna(),
                u.getDireccion(),
                u.getRol() == null ? null : u.getRol().name(),
                u.isEsDuoc(),
                u.getDescuento(),
                separarPreferencias(u.getPreferencias()),
                u.getCodigoReferido(),
                u.getReferidoPor(),
                cantidadReferidos,
                u.getPuntosLevelUp(),
                u.getFechaRegistro() == null ? null : u.getFechaRegistro().toString()
        );
    }

    public static List<String> separarPreferencias(String csv) {
        List<String> resultado = new ArrayList<>();
        if (csv == null || csv.isBlank()) {
            return resultado;
        }
        for (String parte : csv.split(",")) {
            String limpio = parte.trim();
            if (!limpio.isEmpty()) {
                resultado.add(limpio);
            }
        }
        return resultado;
    }

    public static String unirPreferencias(List<String> preferencias) {
        if (preferencias == null) {
            return null;
        }
        List<String> limpias = new ArrayList<>();
        for (String p : preferencias) {
            if (p != null && !p.isBlank()) {
                limpias.add(p.trim().replace(",", " "));
            }
        }
        return String.join(",", limpias);
    }
}
