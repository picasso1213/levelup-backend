# Level-Up Gamer — Backend

API REST en Spring Boot para la tienda **Level-Up Gamer** (DSY1104, Evaluación 3). Entrega el catálogo de
productos, categorías, usuarios con roles, boletas de compra y el programa de puntos Level-Up. La
autenticación es stateless con JWT y los datos se guardan en MySQL. El frontend React consume esta API
bajo el prefijo `/api/v1`.


## Variables de entorno

Todas son opcionales; si no las defines se usan los valores por defecto de `application.properties`
(ver también `.env.example`).

| Variable | Por defecto | Descripción |
|---|---|---|
| `DB_URL` | `jdbc:mysql://localhost:3306/levelup?...` | URL JDBC de MySQL |
| `DB_USER` | `root` | Usuario de la base de datos |
| `DB_PASSWORD` | (vacío) | Contraseña de la base de datos |
| `JWT_SECRET` | secreto de desarrollo | Secreto para firmar el JWT (mínimo 32 caracteres) |
| `JWT_EXPIRATION_MS` | `7200000` | Duración del token (2 horas) |
| `CORS_ORIGINS` | `http://localhost:5173,http://localhost:4173` | Orígenes permitidos, separados por coma |
| `SEED_PASSWORD` | `Level2026!` | Contraseña de los usuarios demo |

Cambia `JWT_SECRET` en cualquier entorno que no sea tu computador.

## Cómo ejecutar

Este proyecto **no incluye `mvnw`**. Opciones:

1. Abrir la carpeta en tu IDE (IntelliJ, Eclipse, VS Code) y ejecutar `LevelupBackendApplication`.
2. Con Maven instalado: `mvn spring-boot:run`
3. Si quieres generar el wrapper: `mvn wrapper:wrapper` (después podrás usar `./mvnw spring-boot:run`).

Con `MySQL` encendido, la API queda en <http://localhost:8080>. Al primer arranque se crean las tablas y los
datos iniciales (6 productos, sus categorías y 3 usuarios demo; cada tabla se llena solo si está vacía).
Para desactivar los datos iniciales usa `app.seed.enabled=false`.

## Swagger

Documentación interactiva en <http://localhost:8080/swagger-ui.html>.

Para probar endpoints protegidos: haz `POST /api/v1/auth/login`, copia el valor de `token`, pulsa el botón
**Authorize** y pega el JWT **sin** escribir la palabra `Bearer`.

## Endpoints (`/api/v1`)

| Método | Ruta | Acceso |
|---|---|---|
| POST | `/auth/register` | Público |
| POST | `/auth/login` | Público |
| GET | `/productos`, `/productos/{id}` | Público |
| POST, PUT, DELETE | `/productos`, `/productos/{id}` | ADMIN |
| GET | `/categorias`, `/categorias/{id}` | Público |
| POST, PUT, DELETE | `/categorias`, `/categorias/{id}` | ADMIN |
| GET | `/usuarios/me` | Autenticado |
| PUT | `/usuarios/me` | Autenticado |
| POST | `/usuarios/me/canjes` | Autenticado |
| GET | `/usuarios` | ADMIN |
| PUT | `/usuarios/{id}/rol` | ADMIN |
| DELETE | `/usuarios/{id}` | ADMIN |
| POST | `/boletas` | Autenticado |
| GET | `/boletas/mis-compras` | Autenticado |
| GET | `/boletas` | ADMIN, VENDEDOR |
| GET | `/boletas/{id}` | ADMIN, VENDEDOR o dueño de la boleta |

Reglas destacadas: el registro siempre crea usuarios `CLIENTE`; un ADMIN no puede cambiar su propio rol ni
borrarse; los correos `@duoc.cl` y `@profesor.duoc.cl` reciben 20 % de descuento; cada boleta suma
`total / 1000` puntos Level-Up (división entera); el código de referido da 20 puntos al nuevo usuario y 50 al
referente.

## Usuarios demo (solo desarrollo)

Contraseña de todos: `Level2026!` (o el valor de `SEED_PASSWORD`).

| Correo | Rol |
|---|---|
| `admin@levelup.cl` | ADMIN |
| `vendedor@levelup.cl` | VENDEDOR |
| `cliente@gmail.com` | CLIENTE |

No uses estos usuarios ni esta contraseña en producción.

## Formato de errores

Todos los errores responden con el mismo JSON (`campos` solo aparece cuando hay errores por campo):

```json
{
  "status": 400,
  "error": "Hay datos inválidos en la petición.",
  "campos": { "correo": "Usa un correo @duoc.cl, @profesor.duoc.cl o @gmail.com." }
}
```

Códigos usados: 400 (datos inválidos o regla de negocio), 401 (sin sesión o credenciales incorrectas),
403 (sin permisos), 404 (no existe), 409 (duplicado, stock insuficiente o registro en uso).

## Pruebas

```
mvn test
```

Las pruebas unitarias usan JUnit 5 y Mockito. `SeguridadIntegracionTest` levanta el contexto completo con una
base H2 en memoria (`src/test/resources/application.properties`), por lo que no necesita MySQL.

## Modelo de datos

```
USUARIO 1 ──── N BOLETA 1 ──── N DETALLE_BOLETA N ──── 1 PRODUCTO N ──── 1 CATEGORIA
```

- `categoria`: id, nombre (único, máx. 80).
- `producto`: id, nombre, descripcion, precio (CLP), stock, imagen, categoria_id.
- `usuario`: id, run (único), nombre, apellidos, correo (único), password (BCrypt), fecha_nacimiento,
  telefono, region, comuna, direccion, rol (ADMIN/VENDEDOR/CLIENTE), es_duoc, descuento (%),
  preferencias (CSV), codigo_referido (único), referido_por (correo del referente),
  puntos_level_up, fecha_registro.
- `boleta`: id, fecha, usuario_id, subtotal, descuento, total, puntos_ganados.
- `detalle_boleta`: id, boleta_id, producto_id, cantidad, precio_unitario, subtotal.
