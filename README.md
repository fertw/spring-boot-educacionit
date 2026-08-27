# alumnos-api

API REST desarrollada con Spring Boot como parte del curso **Java Spring Boot** de EducaciónIT. El proyecto evoluciona clase a clase; cada clase queda marcada con un tag (`clase-1`, `clase-2`, …) para poder partir del estado exacto de cada encuentro.

Este commit corresponde a la **Clase 2 — API REST (parte 1)**: el proyecto pasa de devolver textos sueltos a exponer un recurso real (`Alumno`) con operaciones GET y POST sobre una lista en memoria.

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
2. Para ubicarse en el estado de una clase: `git checkout clase-2`.
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
├── AlumnosApiApplication.java      ← clase main (@SpringBootApplication)
├── controller/
│   ├── HolaController.java         ← endpoints de la Clase 1
│   └── AlumnoController.java       ← recurso /alumnos (Clase 2)
└── model/
    └── Alumno.java                 ← POJO: id, nombre, apellido, dni, legajo
```

Los alumnos viven en una `List<Alumno>` dentro del controlador, con tres registros de prueba cargados al iniciar (Ana Gomez, Bruno Diaz, Carla Gomez). Se pierden al reiniciar: la persistencia llega en la Clase 5.

## Endpoints disponibles

### Clase 1

| Método | Ruta        | Devuelve                                              |
|--------|-------------|-------------------------------------------------------|
| GET    | `/hola`     | Texto plano: `"Hola Mundo"`                           |
| GET    | `/curso`    | JSON (`Map<String,String>`) con información del curso |
| GET    | `/modulos`  | Información sobre los módulos del curso               |
| GET    | `/duracion` | Información sobre la duración del curso               |

### Clase 2 — recurso `/alumnos`

| Método | Ruta                                  | Qué hace                                  | Status |
|--------|---------------------------------------|-------------------------------------------|--------|
| GET    | `/alumnos`                            | Lista todos los alumnos                   | 200    |
| GET    | `/alumnos?apellido=Gomez`             | Filtra por apellido (opcional)            | 200    |
| GET    | `/alumnos/{id}`                       | Un alumno por id                          | 200 (body vacío si no existe — se corrige en la Clase 3) |
| GET    | `/alumnos/legajo/{legajo}`            | Un alumno por legajo                      | 200    |
| GET    | `/alumnos/buscar?nombre=X&apellido=Y` | Búsqueda por ambos (los dos obligatorios) | 200 · 400 si falta alguno |
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

### Deudas que quedan a propósito

| Problema                                           | Se resuelve en |
|----------------------------------------------------|----------------|
| `GET /alumnos/99` devuelve 200 con body vacío en vez de 404 | Clase 3 (`ResponseEntity`) |
| `POST /alumnos` con `{}` crea un alumno de puros `null`     | Clase 6 (Bean Validation) |
| Los datos se pierden al reiniciar                            | Clase 5 (Spring Data JPA + H2) |
| La lista vive dentro del controlador                         | Clase 4 (capas: Service / Repository) |

## Próxima clase

- Completar el CRUD: `PUT /alumnos/{id}` y `DELETE /alumnos/{id}`.
- `ResponseEntity` para controlar status, headers y body; `404` cuando el recurso no existe y `Location` en el `201`.
- Resolución del **Laboratorio 1** (clases `Alumno` y `Materia`, endpoints de consulta, puerto 9080).
