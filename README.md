# alumnos-api

API REST desarrollada con Spring Boot como parte del curso **Java Spring Boot** de EducaciónIT. El proyecto evoluciona clase a clase; cada clase queda marcada con un tag (`clase-1`, `clase-2`, …) para poder partir del estado exacto de cada encuentro.

Este commit corresponde a la **Clase 4 — Inyección de dependencias y arquitectura en capas**: el proyecto se reorganiza en `Controller → Service → Repository`, con Spring creando y conectando los objetos por inyección de dependencias, y DTOs con `records` de Java separando lo que se expone de lo que se guarda.

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
2. Para ubicarse en el estado de una clase: `git checkout clase-4`.
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
| Dependencia | `spring-boot-starter-webmvc` |
| Puerto      | `9080`                       |

## Estructura

```
src/main/java/com/educacionit/alumnos_api/
├── AlumnosApiApplication.java          ← clase main (@SpringBootApplication)
├── controller/
│   └── AlumnoController.java           ← recurso /alumnos, delega toda la lógica en AlumnoService
├── service/
│   └── AlumnoService.java              ← reglas de negocio, orquesta el repository
├── repository/
│   ├── AlumnoRepository.java           ← interfaz: contrato de persistencia
│   └── AlumnoRepositoryEnMemoria.java  ← @Repository, implementación con List en memoria
├── dto/
│   ├── AlumnoRequest.java              ← record: lo que llega en el body de POST/PUT (sin id)
│   └── AlumnoResponse.java             ← record: lo que se devuelve al cliente
└── model/
    └── Alumno.java                     ← POJO interno: id, nombre, apellido, dni, legajo
```

`HolaController` (los endpoints sueltos de la Clase 1) se eliminó: ya cumplió su propósito como primer contacto con Spring MVC.

Los alumnos viven en una `List<Alumno>` dentro de `AlumnoRepositoryEnMemoria`. Se pierden al reiniciar: la persistencia real llega en la Clase 5.

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
| Los datos se pierden al reiniciar                            | Clase 5 (Spring Data JPA + H2) |
| **Laboratorio 1 pendiente**: `Materia`, `Alumno.materias` y `CarreraController` (`GET /carreras`, `GET /carreras/{codigo}`) | Tarea / próxima clase |

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

## Próxima clase

**Clase 5 — Persistencia con Spring Data JPA y H2.** La interfaz `AlumnoRepository` se reemplaza por `JpaRepository<Alumno, Long>` contra una base H2 en memoria (con consola web en `/h2-console`); `Alumno` pasa a ser `@Entity`. Gracias a que el `Service` depende de la interfaz y no de la implementación, el `Service` y el `Controller` no cambian. Tarea: aplicar el mismo refactor de capas (repository + service + DTOs) a `Materia`.
