package com.levelup.backend.config;

import com.levelup.backend.model.Categoria;
import com.levelup.backend.model.Producto;
import com.levelup.backend.model.Rol;
import com.levelup.backend.model.Usuario;
import com.levelup.backend.repository.CategoriaRepository;
import com.levelup.backend.repository.ProductoRepository;
import com.levelup.backend.repository.UsuarioRepository;
import com.levelup.backend.util.CodigoReferidoUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Carga datos iniciales (categorías, productos y usuarios demo) si las tablas están vacías. */
@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class DataLoader implements CommandLineRunner {

    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final String passwordSemilla;

    public DataLoader(CategoriaRepository categoriaRepository,
                      ProductoRepository productoRepository,
                      UsuarioRepository usuarioRepository,
                      PasswordEncoder passwordEncoder,
                      @Value("${app.seed.password}") String passwordSemilla) {
        this.categoriaRepository = categoriaRepository;
        this.productoRepository = productoRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.passwordSemilla = passwordSemilla;
    }

    @Override
    public void run(String... args) {
        if (categoriaRepository.count() == 0) {
            for (String nombre : new String[]{"Consolas", "Computadores Gamers", "Sillas Gamers",
                    "Accesorios", "Mouse", "Poleras Personalizadas"}) {
                categoriaRepository.save(new Categoria(nombre));
            }
        }
        if (productoRepository.count() == 0) {
            crearProducto("PlayStation 5", "Consolas", 549990, 8, "/images/ps5.jpg",
                    "Gráficos de última generación y tiempos de carga ultrarrápidos.");
            crearProducto("PC Gamer ASUS ROG Strix", "Computadores Gamers", 1299990, 4, "/images/pc.jpg",
                    "Rendimiento excepcional para los juegos más exigentes.");
            crearProducto("Silla Gamer Secretlab Titan", "Sillas Gamers", 349990, 6, "/images/silla-gamer.jpg",
                    "Soporte ergonómico y ajustable para sesiones largas de juego.");
            crearProducto("Control Xbox Series X", "Accesorios", 59990, 15, "/images/control-xbox.jpg",
                    "Botones mapeables y respuesta táctil mejorada.");
            crearProducto("Mouse Logitech G502 HERO", "Mouse", 49990, 20, "/images/mouse.jpg",
                    "Sensor de alta precisión y botones personalizables.");
            crearProducto("Polera Personalizada Level-Up", "Poleras Personalizadas", 14990, 30,
                    "/images/polera.png", "Cómoda y estilizada, personalízala con tu gamer tag.");
        }
        if (usuarioRepository.count() == 0) {
            crearUsuario("Admin", "Level-Up", "111111111", "admin@levelup.cl", Rol.ADMIN);
            crearUsuario("Vendedor", "Level-Up", "222222222", "vendedor@levelup.cl", Rol.VENDEDOR);
            crearUsuario("Cliente", "Demo", "123456785", "cliente@gmail.com", Rol.CLIENTE);
        }
    }

    private void crearProducto(String nombre, String nombreCategoria, int precio, int stock,
                               String imagen, String descripcion) {
        Categoria categoria = categoriaRepository.findByNombreIgnoreCase(nombreCategoria)
                .orElseGet(() -> categoriaRepository.save(new Categoria(nombreCategoria)));
        Producto p = new Producto();
        p.setNombre(nombre);
        p.setCategoria(categoria);
        p.setPrecio(precio);
        p.setStock(stock);
        p.setImagen(imagen);
        p.setDescripcion(descripcion);
        productoRepository.save(p);
    }

    private void crearUsuario(String nombre, String apellidos, String run, String correo, Rol rol) {
        Usuario u = new Usuario();
        u.setNombre(nombre);
        u.setApellidos(apellidos);
        u.setRun(run);
        u.setCorreo(correo);
        u.setPassword(passwordEncoder.encode(passwordSemilla));
        u.setFechaNacimiento(LocalDate.of(1995, 1, 1));
        u.setRegion("Región Metropolitana de Santiago");
        u.setComuna("Santiago");
        u.setDireccion("Av. Siempre Viva 742");
        u.setRol(rol);
        u.setEsDuoc(false);
        u.setDescuento(0);
        u.setPuntosLevelUp(0);
        u.setFechaRegistro(LocalDateTime.now());
        String codigo;
        do {
            codigo = CodigoReferidoUtil.generar(nombre, apellidos);
        } while (usuarioRepository.existsByCodigoReferido(codigo));
        u.setCodigoReferido(codigo);
        usuarioRepository.save(u);
    }
}
