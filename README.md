# alumnos-api

API REST desarrollada con Spring Boot como parte del curso **Java Spring Boot** de EducaciónIT. Este repositorio corresponde a la **Clase 1**, donde se sientan las bases de HTTP, Maven y el ecosistema Spring.

## Requisitos

- JDK 21
- Maven (o el wrapper `mvnw` incluido en el proyecto)
- Eclipse (o cualquier IDE con soporte para proyectos Maven/Spring Boot)

## Cómo importar y correr el proyecto

1. Clonar el repositorio:
   ```bash
   git clone https://github.com/fertw/spring-boot-educacionit.git
   ```
2. Importar en Eclipse como **Existing Maven Project** (`File > Import > Maven > Existing Maven Projects`), seleccionando la carpeta `alumnos-api`.
3. Esperar a que Eclipse descargue las dependencias definidas en `pom.xml`.
4. Ejecutar la clase principal `AlumnosApiApplication` como **Spring Boot App** (o `Java Application`).
5. Por defecto la aplicación levanta en el puerto `8080`. Para cambiarlo, agregar en `src/main/resources/application.properties`:
   ```properties
   server.port=8081
   ```

También se puede correr desde la terminal con el wrapper de Maven:

```bash
./mvnw spring-boot:run
```

### Datos del proyecto

| Propiedad   | Valor              |
|-------------|--------------------|
| Group       | `com.educacionit`  |
| Artifact    | `alumnos-api`      |
| Spring Boot | `4.0.x`            |
| Java        | `21`               |
| Build tool  | Maven              |
| Dependencia | `spring-boot-starter-web` |

## Endpoints disponibles

| Método | Ruta         | Devuelve                                                                 |
|--------|--------------|---------------------------------------------------------------------------|
| GET    | `/hola`      | Texto plano: `"Hola Mundo"`                                              |
| GET    | `/curso`     | JSON (`Map<String,String>`) con información del curso                    |
| GET    | `/modulos`   | Información sobre los módulos del curso                                  |
| GET    | `/duracion`  | Información sobre la duración del curso                                  |

> El endpoint `/curso` originalmente devolvía texto plano y fue migrado para devolver un `Map<String,String>`, dejando que Spring (vía Jackson) lo serialice automáticamente a JSON. Esto sirvió para mostrar la conversión automática de objetos Java a JSON sin necesidad de configuración adicional.

## Temario de la Clase 1

- **Arquitectura cliente-servidor** y protocolo HTTP/HTTPS (rol de SSL en el cifrado del transporte).
- **Anatomía de un request**: method, URL, headers y body.
- **Anatomía de un response**: headers, body y status code.
- **Status codes** agrupados por familia:
  - `1xx` Informacional
  - `2xx` Éxito
  - `3xx` Redirección
  - `4xx` Error del cliente
  - `5xx` Error del servidor
- **Librería vs. framework**: quién tiene el control del flujo de ejecución.
  - Librerías: Lombok, Jackson, Guava (las invoca el código propio).
  - Frameworks: Hibernate, Spring (invierten el control y llaman al código propio — *IoC*).
- **Maven**:
  - Problema que resuelve: gestión de dependencias y estandarización del ciclo de build.
  - Coordenadas: `Group / Artifact / Version` (GAV).
  - Ciclo de vida: `clean`, `validate`, `compile`, `test`, `package`, `install`.
- **Spring Framework vs. Spring Boot**:
  - Autoconfiguración basada en las dependencias presentes en el classpath.
  - Servidor Tomcat embebido (no hace falta desplegar un `.war` en un servidor externo).
  - Starters: dependencias empaquetadas que simplifican la configuración (ej. `spring-boot-starter-web`).

## Próxima clase

- Profundización en **API REST**: recursos, representaciones y buenas prácticas de diseño.
- **Verbos HTTP**: `GET`, `POST`, `PUT`, `PATCH`, `DELETE` y su semántica.
- Introducción a **Postman** para probar los endpoints de forma manual.
