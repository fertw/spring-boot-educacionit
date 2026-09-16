# alumnos-api

API REST desarrollada con Spring Boot como parte del curso **Java Spring Boot** de EducaciónIT. El proyecto evoluciona clase a clase; cada clase queda marcada con un tag (`clase-1`, `clase-2`, …) para poder partir del estado exacto de cada encuentro.

Este commit corresponde a la **Clase 7 — Manejo de errores y APIs de terceros**: un `@RestControllerAdvice` (`GlobalExceptionHandler`) centraliza el mapeo de excepciones a `ProblemDetail` (RFC 9457), reemplazando el `RuntimeException` genérico de `AlumnoService`. Se suma además un recurso nuevo, `/externo/feriados/{anio}`, que consume la API pública de feriados de Argentina vía `RestClient`.

## Requisitos

- JDK 21
- Maven (o el wrapper `mvnw` incluido en el proyecto)
- Eclipse con Spring Tools 4 (o cualquier IDE con soporte para proyectos Maven/Spring Boot)
- Postman (o `curl`) para probar los endpoints que no son GET

## Cómo importar y correr el proyecto

1. Clonar el repositorio:
   ```bash
   git clone https://github.com/fertw/spring-boot-educacionit.git
   ```
2. Para ubicarse en el estado de una clase: `git checkout clase-5`.
3. Importar en Eclipse como **Existing Maven Project** (`File > Import > Maven > Existing Maven Projects`), seleccionando la carpeta `alumnos-api`.
4. Esperar a que Eclipse descargue las dependencias definidas en `pom.xml`.
5. Ejecutar la clase principal `AlumnosApiApplication` como **Spring Boot App** (o `Java Application`).
6. La aplicación levanta en el puerto **9080** (configurado en `src/main/resources/application.properties` con `server.port=9080`).

También se puede correr desde la terminal con el wrapper de Maven:

```bash
./mvnw spring-boot:run
```

### Datos del proyecto

| Propiedad   | Valor                        |
|-------------|------------------------------|
| Group       | `com.educacionit`            |
| Artifact    | `alumnos-api`                |
| Spring Boot | `4.0.x`                      |
| Java        | `21`                         |
| Build tool  | Maven                        |
| Dependencias | `spring-boot-starter-webmvc`, `spring-boot-starter-data-jpa`, `h2`, `spring-boot-h2console` |
| Puerto      | `9080`                       |

## Estructura

```
src/main/java/com/educacionit/alumnos_api/
├── AlumnosApiApplication.java          ← clase main (@SpringBootApplication)
├── controller/
│   └── AlumnoController.java           ← recurso /alumnos (+ inscripción a materias), delega en AlumnoService
├── service/
│   └── AlumnoService.java              ← reglas de negocio, orquesta AlumnoRepository y MateriaRepository
├── repository/
│   ├── AlumnoRepository.java           ← extends JpaRepository<Alumno, Long> — sin implementación propia
│   └── MateriaRepository.java          ← extends JpaRepository<Materia, Long>
├── dto/
│   ├── AlumnoRequest.java              ← record: lo que llega en el body de POST/PUT (sin id)
│   └── AlumnoResponse.java             ← record: lo que se devuelve al cliente
└── model/
    ├── Alumno.java                     ← @Entity: id, nombre, apellido, dni, legajo, materias (@ManyToMany)
    └── Materia.java                    ← @Entity: id, nombre, codigo

src/main/resources/
├── application.properties              ← datasource H2, ddl-auto, show-sql, consola web
└── data.sql                            ← alumnos de arranque (se ejecuta después de que Hibernate crea las tablas)
```

`AlumnoRepositoryEnMemoria` (la `List` + `AtomicLong` de la Clase 4) se eliminó: `JpaRepository` la reemplaza por completo. `AlumnoService` y `AlumnoController` no cambiaron para hacer ese reemplazo — solo suman lo nuevo de materias.

Hibernate crea las tablas `alumno`, `materia` y `alumno_materia` al arrancar (`ddl-auto=update`). Los datos viven en H2 en memoria: sobreviven mientras el proceso corre y se pierden al reiniciar, salvo lo que recargue `data.sql`.

### Consola web de H2

Con la app corriendo, entrar a `http://localhost:9080/h2-console` y conectarse con:

| Campo    | Valor                   |
|----------|-------------------------|
| JDBC URL | `jdbc:h2:mem:alumnosdb` |
| User     | `sa`                    |
| Password | (vacía)                 |

> El formulario trae por defecto `jdbc:h2:~/test`, que apunta a un archivo inexistente y da `Database not found`. Hay que reemplazarlo por la URL de arriba, la misma del `application.properties`.

## MySQL con Docker (perfil `mysql`)

El proyecto trae también `application-mysql.properties` para correr contra MySQL en vez de H2. El `docker-compose.yml` de esta carpeta levanta MySQL y phpMyAdmin ya configurados para ese perfil.

1. Levantar los contenedores:
   ```bash
   docker compose up -d
   ```
2. Confirmar que están arriba:
   ```bash
   docker compose ps
   ```
3. Arrancar la app con el perfil `mysql`:
   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=mysql
   ```
   (En Eclipse: **Run Configurations → Arguments → VM arguments** → `-Dspring.profiles.active=mysql`, o **Program arguments** → `--spring.profiles.active=mysql`.)

| Servicio    | URL / Conexión                              | Credenciales           |
|-------------|----------------------------------------------|------------------------|
| MySQL       | `localhost:3306`, base `alumnos`             | user `root`, sin password |
| phpMyAdmin  | `http://localhost:8082`                      | user `root`, sin password |

Para parar los contenedores (sin borrar los datos):
```bash
docker compose stop
```

Para bajarlos y borrar también el volumen de datos:
```bash
docker compose down -v
```

## Endpoints disponibles

### Clase 2 — recurso `/alumnos`

| Método | Ruta                                  | Qué hace                                  | Status |
|--------|---------------------------------------|-------------------------------------------|--------|
| GET    | `/alumnos`                            | Lista todos los alumnos                   | 200    |
| GET    | `/alumnos/{id}`                       | Un alumno por id                          | 200 · 404 si no existe |
| GET    | `/alumnos/legajo/{legajo}`            | Un alumno por legajo                      | 200 · 404 si no existe |
| GET    | `/alumnos/dni/{dni}`                  | Un alumno por dni                         | 200 · 404 si no existe |
| GET    | `/alumnos/buscar?nombre=X&apellido=Y&dni=Z&legajo=W` | Búsqueda por cualquier combinación de filtros (todos opcionales) | 200 |
| POST   | `/alumnos`                            | Crea un alumno (el id lo asigna el servidor) | 201 |

Ejemplo de alta:

```bash
curl -X POST http://localhost:9080/alumnos \
     -H "Content-Type: application/json" \
     -d '{"nombre":"Diego","apellido":"Perez","dni":"33444555","legajo":1004}'
```

Respuesta (`201 Created`):

```json
{ "id": 4, "nombre": "Diego", "apellido": "Perez", "dni": "33444555", "legajo": 1004 }
```

> Si el body no se envía como JSON (`Content-Type: application/json`) la API responde `415`. Si el JSON está mal formado, `400`.

### Clase 3 — CRUD completo con `ResponseEntity`

| Método | Ruta                        | Qué hace                              | Status |
|--------|-----------------------------|----------------------------------------|--------|
| PUT    | `/alumnos/{id}`             | Reemplaza el alumno completo           | 200 · 404 si no existe |
| PATCH  | `/alumnos/{id}`             | Cambia solo el apellido (`{"apellido": "..."}`) | 200 · 400 si falta el campo · 404 si no existe |
| DELETE | `/alumnos/{id}`             | Elimina el alumno                      | 204 · 404 si no existe |

Ejemplo de alta con `Location` (header, no en el body):

```bash
curl -i -X POST http://localhost:9080/alumnos \
     -H "Content-Type: application/json" \
     -d '{"nombre":"Diego","apellido":"Perez","dni":"33444555","legajo":"A004"}'
```

```
HTTP/1.1 201
Location: http://localhost:9080/alumnos/6
```

### Clase 5 — inscripción a materias

| Método | Ruta                                | Qué hace                                         | Status |
|--------|-------------------------------------|--------------------------------------------------|--------|
| POST   | `/alumnos/{id}/materias/{materiaId}` | Inscribe al alumno en la materia                 | 200 · 500 si alumno o materia no existen (ver deudas) |
| GET    | `/alumnos/{id}/materias`            | Lista las materias en las que está inscripto     | 200 · 500 si el alumno no existe |

```bash
curl -X POST http://localhost:9080/alumnos/1/materias/1
curl http://localhost:9080/alumnos/1/materias
```

### Clase 7 — manejo de errores y API externa de feriados

| Método | Ruta                          | Qué hace                                              | Status |
|--------|-------------------------------|--------------------------------------------------------|--------|
| GET    | `/externo/feriados/{anio}`   | Consulta los feriados de un año vía la API de ArgentinaDatos | 200 · 500 si el año no tiene feriados publicados |

`AlumnoService` (`buscarPorId`, `actualizar`, `eliminar`) ya no devuelve `null`/`Optional`: usa `orElseThrow(() -> new AlumnoNoEncontradoException(id))`, y `AlumnoController` dejó de chequear `if (alumno == null)` a mano. Ambas excepciones (`AlumnoNoEncontradoException` → 404, `LegajoDuplicadoException` → 409) y los errores de `@Valid` (400) se resuelven en `GlobalExceptionHandler` con `ProblemDetail`.

```bash
curl http://localhost:9080/alumnos/999
curl http://localhost:9080/externo/feriados/2026
```

## Temario de la Clase 2

- **API y REST**: recursos identificados por URIs, representaciones en JSON, verbos HTTP para las operaciones, servicios sin estado (*stateless*), interfaz uniforme.
- **Verbos HTTP ↔ CRUD**: `POST` crea, `GET` lee, `PUT` reemplaza, `DELETE` borra. `GET` es seguro; `PUT` y `DELETE` son idempotentes; `POST` no.
- **Status codes de una API**: `200`, `201`, `204`, `400`, `404`, `405`, `415`, `500`.
- **Buenas prácticas de rutas**: sustantivos en plural, jerarquía (`/alumnos/{id}/materias`), filtros como query params, códigos HTTP correctos, JSON planos.
- **JSON y Jackson**: serialización (objeto → JSON, vía getters) y deserialización (JSON → objeto, vía constructor vacío + setters). Por eso `Alumno` es un POJO con ambos.
- **Anotaciones de Spring MVC**:
  - `@RequestMapping("/alumnos")` a nivel de clase como prefijo de ruta.
  - `@GetMapping` / `@PostMapping`.
  - `@PathVariable("id")` para segmentos de la ruta (identifican el recurso).
  - `@RequestParam("x")` con `required` y `defaultValue` para filtros (modifican la consulta).
  - `@RequestBody` para deserializar el body del POST.
  - `@ResponseStatus(HttpStatus.CREATED)` para responder `201`.
- **Postman**: collection `alumnos-api` con un request guardado por endpoint; pestañas *Params*, *Headers* y *Body → raw → JSON*.

## Temario de la Clase 3

- **PUT vs PATCH vs POST**: `POST` no es idempotente (cada llamada crea otro alumno); `PUT` reemplaza el recurso completo y es idempotente; `PATCH` modifica parcialmente. Idempotencia real: ejecutar la operación una vez o N veces deja el mismo estado final observable — `DELETE` también es idempotente aunque el status de la segunda llamada cambie de `204` a `404`.
- **DELETE y `204 No Content`**: si se borró bien no hay nada que devolver → `204`; si el id no existe, `404`. `204` es distinto de `200` con body vacío: dice explícitamente "no hay contenido".
- **`ResponseEntity<T>`**: control explícito de status, headers y body en cada rama (`.ok()`, `.notFound().build()`, `.created(uri).body(...)`, `.noContent().build()`, `.badRequest().build()`), reemplazando el status fijo que decidía Spring en la Clase 2.
- **`GET /alumnos/{id}` inexistente ahora es 404**, no `200` con `null`: el cliente antes no podía distinguir "no hay datos" de "esto no existe".
- **Un POST que no crea nada no es REST**: un endpoint sin un recurso con identidad (ej. `/sumar`) es una función, no un recurso.
- **`ServletUriComponentsBuilder`**: construye el header `Location` del `201` a partir de la request actual (`fromCurrentRequest()` + `path("/{id}")` + `buildAndExpand(id)`), sin hardcodear host/puerto.
- **Actividad "Detective de Status Codes"**: diagnosticar a partir de la respuesta de Postman (status, headers, body) qué bug de `ResponseEntity` introdujo el compañero, sin ver el código.

### Deudas que quedan a propósito

| Problema                                                   | Se resuelve en |
|-------------------------------------------------------------|----------------|
| `POST /alumnos` con `{}` crea un alumno de puros `null`     | Clase 6 (Bean Validation) |
| Los datos se pierden al reiniciar                            | ✅ Clase 5 (Spring Data JPA + H2) |
| **Laboratorio 1 pendiente**: `Materia`, `Alumno.materias` y `CarreraController` (`GET /carreras`, `GET /carreras/{codigo}`) | `Materia` y `Alumno.materias` ✅ Clase 5 · `CarreraController` pendiente |

## Temario de la Clase 4

El objetivo de la clase fue puertas adentro: reordenar la misma API en capas sin agregar endpoints nuevos. Contra Postman, el proyecto responde igual que al final de la Clase 3 (mismos status codes); lo único visible es que el JSON de salida nunca trae campos fuera del DTO.

- **Por qué separar en capas**: hasta la Clase 3, `AlumnoController` mezclaba HTTP, reglas de negocio y datos (lista + `AtomicLong`). Eso complica testear la lógica sin levantar un servidor y acopla el proyecto a que los datos vivan en memoria.
- **Arquitectura en capas** (variante de MVC para una API, sin vista):

  | Capa | Responsabilidad |
  |---|---|
  | `Controller` | Recibe el HTTP, valida la forma del pedido, delega y arma la respuesta |
  | `Service` | Reglas de negocio, orquesta las operaciones — no sabe nada de HTTP |
  | `Repository` | Acceso a los datos — hoy en memoria, en la Clase 5 con JPA |

- **Inversión de Control (IoC) e inyección de dependencias**: sin IoC cada clase crea sus dependencias con `new` (acoplamiento fuerte); con IoC el contenedor de Spring (`ApplicationContext`) crea los objetos y los conecta. Un **bean** es cualquier objeto administrado por ese contenedor, con scope `singleton` por defecto (una sola instancia por contenedor). `@ComponentScan` es lo que hace que el paquete del `Controller` tenga que colgar del paquete del `main` — ahí empieza el escaneo de beans.
- **Estereotipos de Spring**: `@Component` (genérico) y sus especializaciones `@Repository` (acceso a datos), `@Service` (lógica de negocio) y `@Controller`/`@RestController` (expone HTTP). Para beans de clases que no son propias (de una librería externa) se usan `@Bean` + `@Configuration` en vez de anotar la clase.
- **Inyección por constructor**: `AlumnoController` recibe un `AlumnoService`, y `AlumnoService` recibe un `AlumnoRepository`, ambos como campos `final`. Con un solo constructor no hace falta `@Autowired` — Spring lo detecta solo. Se prefiere sobre `@Autowired` en campo porque la dependencia queda obligatoria desde que el objeto existe (sin `null`) y es más fácil de testear.
- **Programar contra una interfaz**: `AlumnoRepository` declara el contrato (el *qué*); `AlumnoRepositoryEnMemoria` es la única implementación por ahora y guarda el estado (el *cómo* — la lista y el `AtomicLong`), por eso `@Repository` va en la implementación y no en la interfaz. El `Service` depende de la interfaz, no de la implementación concreta — así en la Clase 5 se puede cambiar la lista en memoria por Spring Data JPA sin tocar una línea del `Service` ni del `Controller`.
- **DTOs con `record`**: `AlumnoRequest` (lo que entra en `POST`/`PUT`, sin `id` — lo genera el server) y `AlumnoResponse` (lo que sale) separan el contrato de la API del modelo interno `Alumno`. `toModel()` convierte el DTO de entrada al modelo; `AlumnoResponse.fromModel(...)` hace el camino inverso. Jackson deserializa `record` sin configuración extra.
- **Ver la inyección de dependencias fallar en vivo**: comentar `@Repository` en `AlumnoRepositoryEnMemoria` y arrancar la app tira `UnsatisfiedDependencyException: No qualifying bean of type 'AlumnoRepository' available` — Spring dice exactamente qué bean faltó y dónde lo necesitaba.
- **Limpieza de código muerto**: se eliminó `HolaController` (ya cumplió su función como primer contacto con Spring MVC) y un método `@GetMapping("/buscar")` duplicado que había quedado en el `Controller` de la Clase 3 (dos handlers para la misma ruta — Spring ni siquiera arrancaba con eso).
- **`GET /alumnos/dni/{dni}`** pasa a usar `ResponseEntity<AlumnoResponse>` con `404` si no existe, en vez de devolver el objeto (o `null`) directo como en clases anteriores.

### Errores frecuentes de este refactor

- Poner la lista de alumnos en la interfaz en vez de la implementación (Java ni permite un campo de instancia mutable en una interfaz).
- Refactor a DTO a medias: algún método del controller sigue devolviendo `Alumno` en vez de `AlumnoResponse`.
- Perder un endpoint al reescribir el controller a mano.
- `@GetMapping` duplicado en dos métodos → la app no arranca por mapping ambiguo.
- Volver al "200 con `null`": un método que devuelve el tipo directo (no `ResponseEntity<T>`) y no encuentra el recurso responde `200` con body vacío en lugar de `404`.

## Temario de la Clase 5

Objetivo: que los alumnos sobrevivan al reinicio de la aplicación. Se reemplaza la lista en memoria por una base H2 real, con `JpaRepository` generando el repositorio automáticamente. Como adelanto, se arma también la relación `@ManyToMany` entre `Alumno` y `Materia` (tema oficialmente de la Clase 6, pero salió en la práctica de esta clase).

- **El problema del mapeo objeto-relacional**: un objeto tiene referencias a otros objetos (un `Alumno` con una lista de `Materia`); una tabla relacional resuelve eso con claves foráneas y tablas intermedias. Un **ORM** es la capa que traduce objetos a filas y filas a objetos automáticamente.
- **JPA, Hibernate y Spring Data JPA**: JPA es la especificación (interfaces y anotaciones); Hibernate es la implementación que arma y ejecuta el SQL; Spring Data JPA es la capa que genera repositorios sin escribir SQL ni una implementación concreta.
- **La entidad**: `@Entity` marca la clase como tabla persistente; `@Id` + `@GeneratedValue(strategy = GenerationType.IDENTITY)` definen la clave primaria autogenerada. `@Column`/`@Table` solo hacen falta si el nombre no coincide con la convención (`Alumno` → `alumno`, `nombreCompleto` → `nombre_completo`).
- **`application.properties`**: datasource (URL, usuario, contraseña), `ddl-auto` (`create-drop`, `update` o `validate` — **nunca `update` en producción**, ahí van migraciones controladas) y `show-sql` para ver cada sentencia que ejecuta Hibernate.
- **H2 en memoria**: no requiere instalación. Con `spring.h2.console.enabled=true` se habilita la consola web en `/h2-console`. Los datos persisten mientras el proceso corre y desaparecen al reiniciar; esa limitación se resuelve en la Clase 6 con MySQL.
- **`JpaRepository<T, ID>`**: trae gratis `findAll`, `findById`, `save`, `deleteById`, `count`, `existsById`. Spring genera un proxy en tiempo de ejecución: solo se declara la interfaz, sin cuerpo y sin `@Repository`. Como `AlumnoService` dependía de la interfaz y no de la implementación (Clase 4), cambiar de persistencia no tocó ni el controller ni el service.
- **Entidad ≠ DTO**: exponer la entidad acopla la API al modelo de base y, con relaciones, puede filtrar objetos completos o entrar en recursión infinita al serializar. `AlumnoRequest`/`AlumnoResponse` siguen siendo la fachada pública.
- **`@ManyToMany` (adelanto de la Clase 6)**: un alumno cursa varias materias y una materia tiene varios alumnos; en el modelo relacional se resuelve con la tabla intermedia `alumno_materia` (`alumno_id`, `materia_id`). En JPA se modela con `@ManyToMany` + `@JoinTable` en el **lado dueño** de la relación — acá, `Alumno`.
- **`data.sql`**: inserts de arranque. Requiere `spring.jpa.defer-datasource-initialization=true`; sin eso Spring corre el script antes de que Hibernate cree las tablas y la app falla al arrancar. Si se cargan materias e inscripciones, el orden importa: `alumno` → `materia` → `alumno_materia`.

### Paso a paso en Eclipse

**Parte 1: persistencia con H2**

1. Agregar `spring-boot-starter-data-jpa` y `com.h2database:h2` (scope `runtime`) al `pom.xml`. En Spring Boot 4 la consola web va aparte: `spring-boot-h2console`. Clic derecho sobre el proyecto → Maven → Update Project (Alt+F5).
2. Convertir `Alumno` en entidad: `@Entity` sobre la clase, `@Id` + `@GeneratedValue(strategy = GenerationType.IDENTITY)` sobre `id`.
3. `AlumnoRepository extends JpaRepository<Alumno, Long>`. Borrar `AlumnoRepositoryEnMemoria`.
4. Compilar y confirmar que `AlumnoService` no cambió.
5. Configurar `application.properties`: datasource, `ddl-auto=update`, `show-sql=true`, `spring.h2.console.enabled=true`.
6. Arrancar y leer en consola el `Hibernate: create table alumno (...)`; abrir `/h2-console` y ver la tabla.
7. Cargar datos iniciales con `data.sql` + `spring.jpa.defer-datasource-initialization=true`.
8. Correr la collection completa de Postman: todo responde igual que en la Clase 4, pero los datos sobreviven mientras la app corre.

**Parte 2: relación Alumno–Materia**

1. Crear la entidad `Materia` (`@Entity`, `@Id`, `@GeneratedValue(IDENTITY)`, campos `codigo` y `nombre`).
2. Agregar en `Alumno` el campo `List<Materia> materias` con `@ManyToMany` + `@JoinTable(name = "alumno_materia", joinColumns = @JoinColumn(name = "alumno_id"), inverseJoinColumns = @JoinColumn(name = "materia_id"))`.
3. Crear `MateriaRepository extends JpaRepository<Materia, Long>`.
4. Inyectar `MateriaRepository` en `AlumnoService` sumando el parámetro al constructor existente (sin `@Autowired`).
5. Agregar `inscribirEnMateria(alumnoId, materiaId)` y `obtenerMateriasDeAlumno(alumnoId)` en el service.
6. Agregar `POST /alumnos/{id}/materias/{materiaId}` y `GET /alumnos/{id}/materias` en el controller.
7. Reiniciar y confirmar en consola el `Hibernate: create table alumno_materia (...)`.
8. Probar en Postman: crear materias, inscribir un alumno, verificar con el GET de materias del alumno.

### Errores frecuentes

| Error | Causa | Solución |
|---|---|---|
| Whitelabel Error Page (404) en `/error` | URL sin mapping, o puerto distinto de 9080 | Confirmar la URL exacta y `http://localhost:9080/...` |
| `data.sql` falla al arrancar (tabla no existe) | Falta `spring.jpa.defer-datasource-initialization=true`, o el insert de `alumno_materia` va antes que los de `alumno`/`materia` | Agregar la property y respetar el orden `alumno` → `materia` → `alumno_materia` |
| `/h2-console` responde 404 | En Spring Boot 4 la consola no viene con `spring-boot-autoconfigure` | Agregar la dependencia `spring-boot-h2console` y reiniciar |
| `/h2-console` da `Database "~/test" not found` | Se envió el formulario con la JDBC URL por defecto | Usar exactamente `jdbc:h2:mem:alumnosdb` |
| El `id` llega duplicado o en 0 al crear un alumno | El mapeo de `AlumnoRequest` a `Alumno` seguía seteando el id a mano | Sacar esa línea: lo asigna Hibernate |
| JSON con recursión infinita o alumnos/materias repetidos sin fin | Se expuso la entidad y se agregó el lado inverso `@ManyToMany(mappedBy=...)` en `Materia` | Pasar a DTOs también para `Materia` (`MateriaResponse`) |
| `UnsatisfiedDependencyException` al arrancar | Falta el bean del nuevo repository o el constructor de `AlumnoService` no quedó bien al agregar `MateriaRepository` | Revisar que el constructor reciba ambos repositorios y que ambas interfaces existan |

### Secuencia de prueba en Postman

1. `GET /alumnos` → aparecen los 3 alumnos de `data.sql`.
2. Cargar materias (por `/h2-console`, ya que no hay endpoint de alta de `Materia` todavía).
3. `POST /alumnos/1/materias/3` → inscribe al alumno 1 en la materia 3.
4. `GET /alumnos/1/materias` → devuelve las materias del alumno 1.
5. Reiniciar la app (Stop + Run As Spring Boot App) y repetir el paso 4: vuelven solo los datos que recarga `data.sql`. Es la demostración en vivo de que H2 en memoria no sobrevive al reinicio del proceso.

### Deudas que quedan a propósito

| Problema | Se resuelve en |
|---|---|
| `inscribirEnMateria` tira `RuntimeException` genérico → `500` en vez de `404` | ✅ Clase 7 (`@RestControllerAdvice`) |
| `GET /alumnos/{id}/materias` devuelve la entidad `Materia` directo, sin DTO | Tarea: `MateriaResponse` + repository/service/DTOs para `Materia` |
| `data.sql` solo carga alumnos; materias e inscripciones se cargan a mano | Próxima clase |
| `POST /alumnos` con `{}` crea un alumno de puros `null` | Clase 6 (Bean Validation) |

### Glosario

- **ORM (Object-Relational Mapping)**: capa que traduce objetos Java a filas de tablas relacionales y viceversa.
- **JPA (Jakarta Persistence API)**: especificación de Java para persistencia; define interfaces y anotaciones.
- **Hibernate**: implementación de JPA que ejecuta el SQL real.
- **Spring Data JPA**: abstracción sobre JPA que genera repositorios sin implementación manual.
- **`@Entity`**: marca una clase como tabla persistente.
- **`@Id` / `@GeneratedValue`**: clave primaria y su estrategia de generación (acá, `IDENTITY`).
- **`ddl-auto`**: qué hace Hibernate con el esquema al arrancar (`create-drop`, `update`, `validate`).
- **H2**: base de datos en memoria usada para desarrollo y aprendizaje.
- **`JpaRepository<T, ID>`**: interfaz de Spring Data que provee CRUD básico sin implementación.
- **`@ManyToMany`**: relación muchos a muchos entre dos entidades, resuelta con una tabla intermedia.
- **`@JoinTable`**: define el nombre de la tabla intermedia y sus columnas de clave foránea, en el lado dueño.
- **Lado dueño de la relación**: la entidad que declara el `@JoinTable` (acá, `Alumno`); es la que Hibernate usa para sincronizar los cambios.

## Temario de la Clase 7

Objetivo: que la API deje de responder `500` genéricos y de exponer detalles internos, y que sepa consumir un servicio externo con manejo de errores propio.

- **Por qué centralizar el manejo de errores**: hasta la Clase 6, un `id` inexistente o un legajo duplicado terminaban en `RuntimeException` sin capturar → `500` con stacktrace filtrado al cliente. `@RestControllerAdvice` intercepta las excepciones de **todos** los controllers en un solo lugar, sin `try/catch` repetido en cada método.
- **Excepciones de dominio**: `AlumnoNoEncontradoException` y `LegajoDuplicadoException` son `RuntimeException` propias — no dependen de Spring, solo llevan el mensaje. El service las lanza (`orElseThrow`), el controller no sabe que existen.
- **`ProblemDetail` (RFC 9457)**: reemplaza el body de error ad-hoc por un formato estándar (`type`, `title`, `status`, `detail`), con `setProperty(...)` para agregar campos propios (`timestamp`, `errors`).
- **`GlobalExceptionHandler`**: un `@ExceptionHandler` por tipo de excepción → `AlumnoNoEncontradoException` (404), `LegajoDuplicadoException` (409), `MethodArgumentNotValidException` (400, con la lista de `{campo, mensaje}` de `@Valid`) y `Exception` genérica (500, sin exponer el mensaje real).
- **Consumir una API externa con `RestClient`**: `RestClientConfig` define el bean con `baseUrl` y timeouts (`connectTimeout`/`readTimeout`); `FeriadoService` lo inyecta y arma la request (`.get().uri(...).retrieve().body(...)`). `.onStatus(...)` traduce un `404` de la API externa en una excepción propia, en vez de propagar el error del proveedor tal cual.
- **DTO con campos que no controlás**: `FeriadoResponse` sólo modela `fecha`, `tipo` y `nombre`; `@JsonIgnoreProperties(ignoreUnknown = true)` evita que la app rompa si la API externa agrega un campo nuevo.
- **Cuando la API externa desaparece**: la pensada originalmente (`nolaborables.com.ar`) está dada de baja desde fines de 2023 (dominio no renovado — [issue #48](https://github.com/pjnovas/nolaborables/issues/48)); se migró a [ArgentinaDatos](https://argentinadatos.com/docs/operations/get-feriados.html), mismo tipo de dato con otro esquema de campos.

### Deudas que quedan a propósito (Clase 7)

| Problema | Se resuelve en |
|---|---|
| `AlumnoService.crear` no chequea legajo duplicado (`LegajoDuplicadoException` está lista pero no se lanza) | Tarea de esta clase: sumar `existsByLegajo` a `AlumnoRepository` y validar antes de guardar |
| `GET /externo/feriados` (año actual, sin path variable) mencionado en el changelog, no implementado | Pendiente |

## Temario de la Clase 8

**Java Spring Boot · EducaciónIT · Miércoles 16/9 (1 h)**

> Clase corta y sin arrastre: lo que no entre hoy en vivo, queda documentado acá para que cada uno lo siga por su cuenta. A diferencia de las siete clases anteriores, hoy **no se agrega funcionalidad de negocio nueva** — todo lo que sigue es sobre el mismo proyecto `alumnos-api` que ya funciona, para dejarlo documentado, empaquetado, y armar el mapa de qué seguir estudiando.
>
> ⚠️ El bloque de **Seguridad básica** (Basic Auth) es una excepción a lo anterior: es contenido nuevo, no visto en ninguna clase previa. Si el tiempo no llega, es el primer bloque para saltear entero — queda igual documentado acá.

### Temas de la clase

1. Documentación con Swagger / OpenAPI — qué es, beneficios, configuración completa
2. JAR vs WAR y las ventajas de Spring Boot
3. Empaquetado y ejecución sin IDE (`mvn clean package`, `java -jar`)
4. Bonus: Spring Boot Actuator (`/actuator/health`)
5. Seguridad básica con Basic Auth (`spring-boot-starter-security`)
6. Repaso del proyecto capa por capa
7. Glosario final y próximos pasos (testing, JWT, Docker, despliegue, frontend)

### Teoría

#### 1. Documentación con Swagger / OpenAPI

- **OpenAPI** es la especificación: un archivo JSON/YAML que describe cada endpoint, sus parámetros y sus respuestas.
- **Swagger** es el conjunto de herramientas que leen esa especificación — Swagger UI (la que se usa hoy) y Swagger Editor.
- No se escribe a mano: `springdoc-openapi` la genera automáticamente inspeccionando los controllers que ya existen.
- Es un estándar de la industria: lo entienden Postman, los IDEs y hasta generadores automáticos de clientes.
- **Beneficios concretos:** siempre queda actualizada (se regenera con cada build), se prueba desde el navegador sin instalar nada, es el contrato que le dice a un frontend qué esperar sin preguntar, y acelera el onboarding de alguien nuevo al equipo.
- **Ejemplos públicos para mostrar en vivo:** [petstore3.swagger.io](https://petstore3.swagger.io/) (OpenAPI 3.0, el formato moderno) y [petstore.swagger.io](https://petstore.swagger.io/) (OpenAPI 2.0, el clásico). Sirven como aperitivo antes de mostrar la propia `alumnos-api` documentada.

#### 2. JAR vs WAR y por qué Spring Boot

- **WAR** (Web Application Archive): pensado para desplegarse dentro de un servidor externo ya instalado (Tomcat, JBoss, etc.). El servidor es responsabilidad de otra instalación, otra versión, otro mantenimiento.
- **JAR**: el ejecutable trae su propio servidor embebido adentro. No hay nada externo que instalar.
- `spring-boot-starter-webmvc` arma por default un jar autocontenido — es la razón por la que alcanza con `java -jar` para correr la API en cualquier lado.
- **Por qué Spring Boot conviene:** arranca sin XML de configuración manual; autoconfiguración (mira el `pom.xml` y configura razonablemente lo que encuentra); los *starters* traen de una todo lo necesario para una funcionalidad; y el resultado final es "un jar, un comando", igual en la laptop de cada uno que en un servidor real.

#### 3. Actuator (bonus)

- `/actuator/health` es una foto de si la instancia está viva. Respuesta típica: `{"groups":["liveness","readiness"],"status":"UP"}`.
- **`status: UP`**: la app responde. Si algo falla (ej. la base caída), diría `DOWN`.
- **`liveness`** (`/actuator/health/liveness`): "¿el proceso sigue vivo o hay que reiniciarlo?"
- **`readiness`** (`/actuator/health/readiness`): "¿ya terminó de arrancar y puede recibir tráfico?" — puede estar `DOWN` mientras la app todavía está inicializando, aunque el proceso ya esté `UP`.
- En un proyecto real esto no lo mira una persona: lo consulta un balanceador de carga o Kubernetes cada tantos segundos, y decide con esa distinción si hay que reiniciar la instancia o simplemente esperar antes de mandarle tráfico.

#### 4. Seguridad básica en Spring (contenido nuevo)

- Spring Security agrega un **filtro** que se ejecuta antes de que el request llegue al controller — no es una anotación que se agregue método por método.
- Por defecto, apenas se agrega la dependencia, la aplicación empieza a devolver **401 Unauthorized** en todos los endpoints: arranca en modo "todo cerrado", y las rutas que quedan libres se declaran explícitamente.
- Para esta clase se usa **Basic Auth** con un usuario hardcodeado en memoria (`InMemoryUserDetailsManager`), sin armar un login completo.
- El bean `SecurityFilterChain` es donde se decide qué rutas requieren autenticación y cuáles no.
- **Límites de Basic Auth** (por qué no alcanza para producción): las credenciales viajan codificadas en Base64 en cada request, no cifradas; no hay noción de sesión ni expiración; y un único usuario no distingue quién hizo cada operación. Estos tres límites son justo lo que resuelve **JWT**, el próximo paso natural.

### Paso a paso

#### Bloque 0 — Dependencias (`pom.xml`)

```xml
<!-- Swagger / OpenAPI: OJO con la versión -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>3.0.0</version> <!-- la serie 3.x es la compatible con Boot 4 (Jackson 3) -->
</dependency>

<!-- Actuator: starter oficial, sin version propia, sigue al spring-boot-starter-parent -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>

<!-- Seguridad: starter oficial, sin version propia -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

Después de pegar esto: **Alt+F5** (Update Maven Project) en Eclipse.

#### Bloque 1 — Swagger / OpenAPI

1. Agregar la dependencia (ver arriba) con la **versión 3.0.0** — la 2.x apunta a Boot 3 y tira 500 en `/v3/api-docs` sobre Boot 4.
2. Update Maven Project.
3. Arrancar y abrir `http://localhost:9080/swagger-ui.html`.
4. Recorrer los endpoints ya generados desde `AlumnoController` y `MateriaController`, sin haber tocado nada.
5. Agregar `@Tag` y `@Operation` en el controller:

```java
package com.educacionit.alumnos_api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@Tag(name = "Alumnos", description = "Operaciones CRUD sobre alumnos")
@SecurityRequirement(name = "basicAuth") // solo si ya está el bloque de seguridad
@RestController
@RequestMapping("/alumnos")
public class AlumnoController {

    private final AlumnoService alumnoService;

    public AlumnoController(AlumnoService alumnoService) {
        this.alumnoService = alumnoService;
    }

    @Operation(summary = "Lista los alumnos, con filtro opcional por apellido")
    @GetMapping
    public ResponseEntity<List<AlumnoResponse>> listar(@RequestParam(required = false) String apellido) {
        // sin cambios en el cuerpo
    }

    @Operation(summary = "Crea un nuevo alumno")
    @PostMapping
    public ResponseEntity<AlumnoResponse> crear(@Valid @RequestBody AlumnoRequest request) {
        // sin cambios en el cuerpo
    }
    // el resto de los endpoints queda sin anotar, no hace falta documentar todo
}
```

6. Configurar título, descripción y el botón "Authorize" (necesario si se dio seguridad):

```java
package com.educacionit.alumnos_api.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@SecurityScheme(
    name = "basicAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "basic"
)
public class OpenApiConfig {

    @Bean
    public OpenAPI alumnosApiOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Alumnos API")
                .description("API REST para la gestión de alumnos y materias — EducaciónIT")
                .version("1.0"));
    }
}
```

#### Bloque 2 — Empaquetado y ejecución sin IDE

1. **Empaquetar**: clic derecho > Run As > Maven build..., goals `clean package`.
2. **Ubicar el jar**: `target/alumnos-api-0.0.1-SNAPSHOT.jar`.
3. **Ejecutar desde una terminal** (fuera de Eclipse): `java -jar target/alumnos-api-0.0.1-SNAPSHOT.jar`.
4. **Pasar el perfil por línea de comandos**: `java -jar target/alumnos-api-0.0.1-SNAPSHOT.jar --spring.profiles.active=mysql` (el flag va **después** del nombre del jar).
5. **Probar** la collection de Postman o Swagger UI contra el jar corriendo, mismo puerto 9080.

#### Bloque 3 — Actuator (bonus, primero que se cae si falta tiempo)

1. Agregar la dependencia (ver Bloque 0).
2. Update Maven Project.
3. Abrir `http://localhost:9080/actuator/health` y leer `{"groups":["liveness","readiness"],"status":"UP"}`.

#### Bloque 4 — Seguridad básica con Basic Auth (contenido nuevo)

1. Agregar la dependencia (ver Bloque 0). Apenas se agrega, sin configurar nada más, todos los endpoints empiezan a devolver 401 — mostrar esto primero, en vivo.
2. Update Maven Project.
3. Crear la clase de configuración completa:

```java
package com.educacionit.alumnos_api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
public class SecurityConfig {

    @Bean
    public InMemoryUserDetailsManager userDetailsService() {
        UserDetails admin = User.withUsername("admin")
                .password("{noop}admin") // {noop} = contraseña en texto plano, solo para esta clase
                .roles("ADMIN")
                .build();
        return new InMemoryUserDetailsManager(admin);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable) // no aplica a una API REST sin sesión
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll() // Swagger queda libre
                .anyRequest().authenticated() // todo lo demás pide login
            )
            .httpBasic(withDefaults());
        return http.build();
    }
}
```

**Usuario:** `admin` / **Contraseña:** `admin` (los que quedaron hardcodeados arriba).

### Estado final del código

Al cierre de la Clase 8, `alumnos-api` tiene:

- Toda la arquitectura en capas de las Clases 4 a 7 (Controller → Service → Repository, DTOs, JPA, validación, manejo de errores centralizado).
- Documentación interactiva en `/swagger-ui.html`, con título propio y botón "Authorize" para Basic Auth.
- Un jar ejecutable en `target/`, corrible con `java -jar` y con soporte de perfiles por línea de comandos.
- `/actuator/health` expuesto, con los sub-chequeos `liveness` y `readiness`.
- `spring-boot-starter-security` con Basic Auth protegiendo `/alumnos/**` y `/materias/**` (si se llegó a dar el bloque).

### Errores frecuentes (Clase 8)

| Error | Causa | Solución |
|---|---|---|
| **500 en `/v3/api-docs`** | `springdoc-openapi` en versión 2.x, incompatible con Boot 4.0.x (usa Jackson 3) | Subir la dependencia a la versión **3.0.0** o superior |
| `/swagger-ui.html` da 404 | Dependencia mal agregada o falta Update Maven Project | Verificar el `pom.xml` y repetir Alt+F5 |
| Prompt nativo del navegador pidiendo usuario/contraseña al abrir Swagger | Falta el `permitAll()` de `/swagger-ui/**` y `/v3/api-docs/**` en el `SecurityFilterChain`, o el orden de las reglas está invertido | Los `permitAll()` van **antes** del `anyRequest().authenticated()` |
| El jar no arranca fuera de Eclipse | Se corrió `mvn package` en vez de `clean package`, o quedó un jar viejo en `target/` | `mvn clean package` de nuevo antes de ejecutar |
| El perfil no toma con `--spring.profiles.active=mysql` | El flag se puso antes de `-jar` en vez de después del nombre del jar | El flag va al final: `java -jar archivo.jar --spring.profiles.active=mysql` |
| Postman sigue devolviendo 401 con Basic Auth cargado | Usuario o contraseña no coinciden con el `InMemoryUserDetailsManager`, o falta el prefijo `{noop}` en el password | Revisar que las credenciales de Postman coincidan exactamente con las del bean |

### Secuencia de prueba (Postman)

1. `GET /alumnos` **sin** Authorization cargado → **401 Unauthorized**.
2. Cargar Authorization > Basic Auth > `admin` / `admin`. Repetir `GET /alumnos` → **200 OK** con el listado de siempre.
3. Cambiar la contraseña a cualquier otra cosa y repetir → vuelve a dar **401** (para mostrar que valida contra el usuario real).
4. `GET /v3/api-docs` **sin** ninguna autenticación → **200 OK** (tiene que seguir libre).
5. Guardar en la collection dos variantes de `GET /alumnos`: una "sin auth" y otra "con Basic Auth" ya cargada, para no repetir el paso 2 en cada prueba en vivo.
6. Si se dio Actuator: `GET /actuator/health` → `{"groups":["liveness","readiness"],"status":"UP"}`.

### Glosario final

`@Autowired` · `@Bean` · `@Entity` · `@Repository` · `@Service` · `cascade` · `ProblemDetail` · `RestClient` · `ddl-auto` · `@Tag` · `@Operation` · `@SecurityScheme` · `@SecurityRequirement` · `SecurityFilterChain` · `InMemoryUserDetailsManager` · `httpBasic()` · `liveness` / `readiness`

### Hacia dónde seguir

- **Testing**: JUnit 5, MockMvc, `@SpringBootTest`, Testcontainers.
- **Seguridad**: de Basic Auth a JWT (resuelve lo que Basic Auth no cubre: expiración, sesión, distinguir usuarios).
- **Docker**, despliegue, y un frontend que consuma la API.

*Tag de cierre: `clase-8`. Repo: `github.com/fertw/spring-boot-educacionit`.*
