package com.levelup.backend.security;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SeguridadIntegracionTest {

    private static final String CLAVE = "Level2026!";

    @Autowired
    private MockMvc mockMvc;

    private String loginJson(String correo, String clave) {
        return "{\"correo\":\"" + correo + "\",\"password\":\"" + clave + "\"}";
    }

    private String obtenerToken(String correo) throws Exception {
        String cuerpo = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(correo, CLAVE)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(cuerpo, "$.token");
    }

    @Test
    void productosSonPublicosYHaySeis() throws Exception {
        mockMvc.perform(get("/api/v1/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(6)));
    }

    @Test
    void usuariosSinTokenDevuelve401() throws Exception {
        mockMvc.perform(get("/api/v1/usuarios"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void usuariosComoClienteDevuelve403() throws Exception {
        String token = obtenerToken("cliente@gmail.com");
        mockMvc.perform(get("/api/v1/usuarios").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void usuariosComoAdminDevuelve200() throws Exception {
        String token = obtenerToken("admin@levelup.cl");
        mockMvc.perform(get("/api/v1/usuarios").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void vendedorNoPuedeCrearProductosPeroSiVerBoletas() throws Exception {
        String token = obtenerToken("vendedor@levelup.cl");
        String producto = "{\"nombre\":\"Teclado\",\"descripcion\":\"Mecánico\",\"precio\":30000,"
                + "\"stock\":5,\"imagen\":\"/images/teclado.jpg\",\"categoriaId\":1}";

        mockMvc.perform(post("/api/v1/productos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(producto))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/boletas").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void loginConCredencialesMalasDevuelve401() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("cliente@gmail.com", "incorrecta")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Correo o contraseña incorrectos."));
    }

    @Test
    void tokenInvalidoNoAutenticaPeroNoRompe() throws Exception {
        mockMvc.perform(get("/api/v1/usuarios/me").header("Authorization", "Bearer token.invalido"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/productos").header("Authorization", "Bearer token.invalido"))
                .andExpect(status().isOk());
    }

    @Test
    void registroValidoDevuelve201ConToken() throws Exception {
        String cuerpo = "{\"nombre\":\"Nuevo\",\"apellidos\":\"Usuario Prueba\",\"run\":\"7.654.321-6\","
                + "\"correo\":\"nuevo.test@gmail.com\",\"password\":\"1234\",\"fechaNacimiento\":\"2000-05-10\","
                + "\"region\":\"Región Metropolitana de Santiago\",\"comuna\":\"Santiago\","
                + "\"direccion\":\"Calle Falsa 123\"}";

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.usuario.rol").value("CLIENTE"))
                .andExpect(jsonPath("$.conReferido").value(false));
    }
}
