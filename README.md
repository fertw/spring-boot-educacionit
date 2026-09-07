# alumnos-api

API REST desarrollada con Spring Boot como parte del curso **Java Spring Boot** de EducaciónIT. El proyecto evoluciona clase a clase; cada clase queda marcada con un tag (`clase-1`, `clase-2`, …) para poder partir del estado exacto de cada encuentro.

Este commit corresponde a la **Clase 5 — Spring Data JPA (parte 1): persistencia**: la lista en memoria se reemplaza por una base H2 real, con `AlumnoRepository` extendiendo `JpaRepository` (Spring genera la implementación) y `Alumno` convertido en `@Entity`. Como adelanto de la Clase 6 se agrega la relación `@ManyToMany` entre `Alumno` y `Materia`, con dos endpoints de inscripción.

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

> El `RuntimeException` genérico del service es provisorio: las excepciones propias con `404`/`409` llegan en la Clase 7 con `@RestControllerAdvice`.

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
| `inscribirEnMateria` tira `RuntimeException` genérico → `500` en vez de `404` | Clase 7 (`@RestControllerAdvice`) |
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

## Próxima clase

**Clase 6 — Spring Data JPA (parte 2): relaciones, queries, MySQL y validación.**

- Ya se adelantó `@ManyToMany`; queda `LAZY` vs `EAGER` y el problema N+1 en profundidad.
- Query methods derivados del nombre (`findByApellido`, `findByApellidoContainingIgnoreCase`) y `@Query` con JPQL.
- Paginación y ordenamiento: `Pageable`, `Page<T>`, `Sort`.
- Bean Validation: `@Valid`, `@NotBlank`, `@Size`, `@Pattern`, `@Positive`.
- Migración a MySQL (puede quedar como tarea si el tiempo no alcanza).

**Tarea de esta clase:** dejar `Materia` con su propio `JpaRepository` (ya hecho) y probar la inscripción de al menos 2 alumnos en 2 materias distintas desde Postman.
