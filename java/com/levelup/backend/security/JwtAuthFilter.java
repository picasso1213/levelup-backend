package com.levelup.backend.security;

import com.levelup.backend.model.Usuario;
import com.levelup.backend.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Lee el JWT del encabezado Authorization. Si es válido, carga el usuario desde la BD en cada petición
 * (así los cambios de rol o el borrado de cuentas se aplican de inmediato). Si no, sigue sin autenticar.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    public JwtAuthFilter(JwtService jwtService, UsuarioRepository usuarioRepository) {
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String encabezado = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (encabezado != null && encabezado.startsWith("Bearer ")
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            String token = encabezado.substring(7).trim();
            Optional<String> correo = jwtService.obtenerCorreo(token);
            if (correo.isPresent()) {
                Optional<Usuario> usuario = usuarioRepository.findByCorreoIgnoreCase(correo.get());
                if (usuario.isPresent()) {
                    Usuario u = usuario.get();
                    UsernamePasswordAuthenticationToken autenticacion = new UsernamePasswordAuthenticationToken(
                            u.getCorreo(),
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + u.getRol().name())));
                    autenticacion.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(autenticacion);
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}
