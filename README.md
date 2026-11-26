# NutriVida - API REST (Spring Boot)

Backend del proyecto NutriVida: API REST de planes nutricionales con Spring Boot 3.2.5, Maven y Java 17.

## Semana 1: base del backend
| Requisito | Implementación |
| --- | --- |
| Proyecto Spring Boot | Maven, Spring Boot 3.2.5, Java 17 |
| Base de datos **MySQL** local | Base `nutrivida` en `localhost:3306`; credenciales en `application.properties` |
| 3 entidades JPA **sin relaciones** | `Plan` (≈ Producto), `Paciente` (≈ Cliente) y `Venta` (guarda `pacienteId` y `planId` como números, sin `@ManyToOne`) |
| Repositorios | `PlanRepository`, `PacienteRepository`, `VentaRepository` (heredan de `JpaRepository`) |
| Un servicio por entidad (`@Service`) con CRUD | `PlanService`, `PacienteService`, `VentaService`: obtener todos, buscar por id, crear, actualizar y eliminar. Repositorios inyectados por constructor o `@Autowired` |
| Controladores con 5 endpoints CRUD | `PlanController` (`/api/planes`), `PacienteController` (`/api/pacientes`), `VentaController` (`/api/ventas`) |

## Sprint 2: requisitos implementados
| Requisito | Implementación |
| --- | --- |
| **Swagger** | springdoc-openapi personalizado (`config/SwaggerConfig.java`, anotaciones en los controladores y en `Plan`). Botón **Authorize** para probar con JWT. |
| **Autenticación JWT** | Spring Security + JJWT (`security/JwtService`, `security/JwtFilter`, `config/SecurityConfig`). Login y registro en `/api/auth`. Contraseñas con BCrypt. |
| **Testing (JUnit + DataFaker)** | 73 pruebas: unitarias con Mockito (`PlanServiceTest`, `PacienteServiceTest`, `VentaServiceTest`, `AuthServiceTest`, `JwtServiceTest`, `RutUtilTest`) e integración con MockMvc (`ApiIntegracionTest`). Datos de prueba aleatorios con DataFaker (`DatosDePrueba`). |
| **Flyway** | 7 migraciones en `src/main/resources/db/migration` y `src/main/java/db/migration`. Hibernate con `ddl-auto=none`. |

## Base de datos MySQL
1. Inicia tu servidor MySQL local (MySQL Workbench, XAMPP, Laragon, etc.).
2. Crea la base de datos:
   ```sql
   CREATE DATABASE nutrivida;
   ```
   (Si no la creas, la URL tiene `createDatabaseIfNotExist=true` y se crea sola.)
3. Revisa las credenciales en `src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/nutrivida?...
   spring.datasource.username=${DB_USER:root}
   spring.datasource.password=${DB_PASSWORD:}
   ```
   Configurado para XAMPP (usuario `root` sin contraseña, MariaDB). Si tu MySQL tiene contraseña, escríbela después de los dos puntos: `${DB_PASSWORD:tuClave}`.
4. Al iniciar el backend, **Flyway crea las tablas y carga los datos** (planes, usuarios, pacientes y ventas).

Las **pruebas** (`mvnw test`) usan H2 en memoria en modo MySQL (`src/test/resources/application.properties`), así se pueden ejecutar sin tener MySQL encendido.

## Ejecutar
```bash
./mvnw spring-boot:run        # Windows: .\mvnw.cmd spring-boot:run
./mvnw test                   # ejecuta las 73 pruebas
```

- API: http://localhost:8080/api/planes
- Swagger: http://localhost:8080/swagger (o http://localhost:8080/swagger-ui/index.html)
- Documentación JSON (OpenAPI): http://localhost:8080/api-docs
- Base de datos: revisa las tablas `planes`, `pacientes`, `ventas`, `usuarios` y `flyway_schema_history` desde MySQL Workbench

## Usuarios (cargados por Flyway, contraseñas encriptadas con BCrypt)
| Rol | Correo | Clave |
| --- | --- | --- |
| Nutricionista | m.fernandez@nutrivida.cl, c.munoz@nutrivida.cl, a.riquelme@nutrivida.cl, s.toledo@nutrivida.cl | 123456 |
| Paciente | v.morales@gmail.com, g.arriagada@gmail.com | 123456 |
| Paciente | c.silva@outlook.com | 1234567 |

## Autenticación JWT
1. `POST /api/auth/login` con `{"correo": "...", "password": "..."}` devuelve `{ token, nombre, correo, rol }`.
2. Las peticiones protegidas llevan el header `Authorization: Bearer <token>`.
3. `JwtFilter` valida el token en cada petición y registra el rol (`ROLE_NUTRICIONISTA` o `ROLE_PACIENTE`).
4. El token dura 1 hora (`jwt.expiracion-ms` en `application.properties`).

| Endpoint | Acceso |
| --- | --- |
| `POST /api/auth/login`, `POST /api/auth/registro` | Público |
| `GET /api/planes`, `GET /api/planes/{id}`, `GET /api/planes/categoria/{categoria}` | Público |
| `POST`, `PUT`, `DELETE /api/planes/**` | Solo token de **nutricionista** (sin token: 401, token de paciente: 403) |
| `POST /api/ventas/contratar`, `GET /api/ventas/mis-ventas` | Solo token de **paciente** (contrata y ve sus propios planes) |
| `/api/pacientes/**` y el resto de `/api/ventas/**` | Solo token de **nutricionista** |
| Swagger | Público |

### Probar en Swagger
1. Abre `POST /api/auth/login` → **Try it out** → Execute (ya trae el usuario de ejemplo).
2. Copia el valor de `token` de la respuesta.
3. Presiona **Authorize** (arriba a la derecha), pega el token y confirma.
4. Ahora `POST`, `PUT` y `DELETE` de planes responden 200.

## Flyway
| Migración | Qué hace |
| --- | --- |
| `V1__crear_tabla_planes.sql` | Crea la tabla `planes` |
| `V2__insertar_planes_iniciales.sql` | Inserta los 4 planes del sitio |
| `V3__crear_tabla_usuarios.sql` | Crea la tabla `usuarios` |
| `V4__InsertarUsuariosIniciales.java` | Migración en Java: inserta los usuarios encriptando sus contraseñas con BCrypt |
| `V5__crear_tablas_pacientes_y_ventas.sql` | Crea las tablas `pacientes` y `ventas` (sin llaves foráneas) |
| `V6__insertar_pacientes_y_ventas.sql` | Inserta 3 pacientes y 3 ventas de ejemplo |
| `V7__corregir_rut_pacientes.sql` | Corrige el dígito verificador de los RUT de V6 y hace que el RUT sea único (ejemplo de por qué una migración aplicada no se modifica: se agrega otra) |

Al iniciar, Flyway ejecuta en orden las migraciones pendientes y las registra en la tabla `flyway_schema_history`. Para cambiar la base de datos se agrega una migración nueva (V5…); las ya aplicadas no se modifican.

## Pruebas (JUnit 5 + Mockito + DataFaker)
| Clase | Tipo | Qué prueba |
| --- | --- | --- |
| `PlanServiceTest` (9) | Unitaria | Listar, buscar, guardar, actualizar, descuento y eliminar planes (con planes generados por DataFaker) |
| `AuthServiceTest` (10) | Unitaria | Login correcto/incorrecto, registro completo, correo o RUT duplicado, RUT o teléfono inválido, fecha/objetivo faltante y contraseña corta |
| `JwtServiceTest` (5) | Unitaria | Token con datos correctos; rechaza token alterado, de otra clave, vencido o inválido |
| `PacienteServiceTest` (10) | Unitaria | CRUD de pacientes, correo o RUT repetido, RUT inválido e inexistentes (pacientes generados por DataFaker) |
| `RutUtilTest` (5) | Unitaria | Dígito verificador (módulo 11), RUT válidos e inválidos, formato y RUT aleatorios de DataFaker |
| `VentaServiceTest` (10) | Unitaria | Cálculo del total con descuento, fecha automática, plan/paciente inexistente, CRUD y contratación por el paciente |
| `ApiIntegracionTest` (23) | Integración (MockMvc) | Endpoints reales con Flyway + Security: planes, pacientes, ventas, login y registro (200, 400, 401, 403, 404, 409) |
| `NutrividaApplicationTests` (1) | Integración | La aplicación arranca |

`DatosDePrueba` usa **DataFaker** para generar nombres de planes, precios, descripciones, nombres de personas, correos y contraseñas aleatorios en cada ejecución.

## Endpoints
| Método | Ruta | Descripción |
| --- | --- | --- |
| POST | /api/auth/login | Inicia sesión y devuelve el token JWT |
| POST | /api/auth/registro | Registra un paciente y devuelve su token |
| GET | /api/planes | Lista todos los planes |
| GET | /api/planes/{id} | Obtiene un plan (404 si no existe) |
| GET | /api/planes/categoria/{categoria} | Planes de una categoría (deporte, peso, metabolico, vegetariano) |
| POST | /api/planes | Crea un plan 🔒 |
| PUT | /api/planes/{id} | Actualiza un plan 🔒 |
| PUT | /api/planes/{id}/descuento?porcentaje=10 | Aplica un descuento 🔒 |
| DELETE | /api/planes/{id} | Elimina un plan 🔒 |
| GET | /api/pacientes | Lista los pacientes 🔒 |
| GET | /api/pacientes/{id} | Busca un paciente por id 🔒 |
| POST | /api/pacientes | Crea un paciente 🔒 |
| PUT | /api/pacientes/{id} | Actualiza un paciente 🔒 |
| DELETE | /api/pacientes/{id} | Elimina un paciente 🔒 |
| GET | /api/ventas | Lista las ventas 🔒 |
| GET | /api/ventas/{id} | Busca una venta por id 🔒 |
| POST | /api/ventas | Registra una venta (calcula el total si no se envía) 🔒 |
| PUT | /api/ventas/{id} | Actualiza una venta 🔒 |
| DELETE | /api/ventas/{id} | Elimina una venta 🔒 |
| POST | /api/ventas/contratar | El paciente con sesión contrata un plan (`{"planId": 2, "sesiones": 4}`) 👤 |
| GET | /api/ventas/mis-ventas | Planes contratados por el paciente con sesión 👤 |

🔒 = requiere token JWT de nutricionista. 👤 = requiere token JWT de paciente.

Pacientes iniciales: 1 (Valentina Morales, 18.765.432-7), 2 (Gonzalo Arriagada, 15.432.987-0) y 3 (Camila Silva, 19.876.543-0). Al registrarse en `/api/auth/registro` (nombre, **RUT**, correo, contraseña, **teléfono**, **fecha de nacimiento** y **objetivo**, todos obligatorios), el usuario también queda como paciente con esos datos. El RUT se valida con el dígito verificador (`util/RutUtil.java`) y no se puede repetir.

## Estructura
```
src/main/java/com/example/nutrivida/
├── NutrividaApplication.java
├── config/       CorsConfig, SecurityConfig, SwaggerConfig
├── controller/   AuthController, PacienteController, PlanController, VentaController
├── dto/          LoginRequest, LoginResponse, RegistroRequest, ContratarPlanRequest
├── exception/    GlobalExceptionHandler y excepciones propias
├── model/        Plan, Paciente, Venta (entidades del negocio) y Usuario (login)
├── repository/   PlanRepository, PacienteRepository, VentaRepository, UsuarioRepository
├── security/     JwtService, JwtFilter
├── service/      AuthService, PacienteService, PlanService, VentaService
└── util/         RutUtil (RUT) y TelefonoUtil (celular chileno)
src/main/java/db/migration/   V4__InsertarUsuariosIniciales.java (migración Flyway en Java)
src/main/resources/db/migration/   V1, V2, V3, V5, V6, V7 (migraciones SQL)
src/test/java/com/example/nutrivida/   pruebas JUnit + DataFaker
```

## Configuración en Spring Initializr
Maven · Java · Spring Boot 3.2.5 · Group `com.example` · Artifact `nutrivida` · Jar · Java 17.
Dependencias: Spring Web, Spring Data JPA, MySQL Driver, Flyway Migration, Spring Security (y H2 Database solo para pruebas).
Se agregan a mano en el `pom.xml`: springdoc-openapi (Swagger), jjwt (JWT) y datafaker (pruebas).
