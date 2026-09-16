package com.educacionit.alumnos_api.controller;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import com.educacionit.alumnos_api.dto.AlumnoRequest;
import com.educacionit.alumnos_api.dto.AlumnoResponse;
import com.educacionit.alumnos_api.model.Alumno;
import com.educacionit.alumnos_api.model.Materia;
import com.educacionit.alumnos_api.service.AlumnoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Alumnos", description = "Operaciones CRUD con los alumnos")
@SecurityRequirement(name = "basicAuth")
@RestController
@RequestMapping("/alumnos")
public class AlumnoController {

	private final AlumnoService alumnoService;

	public AlumnoController(AlumnoService alumnoService) {
		this.alumnoService = alumnoService;
	}

	@Operation(summary = "Listar alumnos", description = "Obtiene una lista paginada de todos los alumnos registrados en el sistema.")
	@GetMapping
	public ResponseEntity<Page<AlumnoResponse>> listar(Pageable pageable) {
		Page<AlumnoResponse> pagina = alumnoService.listar(pageable)
				.map(AlumnoResponse::fromModel);			
		
		return ResponseEntity.ok(pagina);
	}

	@Operation(summary = "Buscar alumno por ID", description = "Obtiene los detalles de un alumno específico utilizando su ID único.")
	@GetMapping("/{id}")
	public ResponseEntity<AlumnoResponse> buscarPorId(@PathVariable("id") Long id) {
		return ResponseEntity.ok(alumnoService.buscarPorId(id));
	}

	@Operation(summary = "Buscar alumno por legajo", description = "Obtiene los detalles de un alumno específico utilizando su legajo único.")
	@GetMapping("/legajo/{legajo}")
	public ResponseEntity<AlumnoResponse> buscarPorLegajo(@PathVariable("legajo") String legajo) {
		return alumnoService.buscarPorLegajo(legajo)
				.map(AlumnoResponse::fromModel)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	@Operation(summary = "Buscar alumno por DNI", description = "Obtiene los detalles de un alumno específico utilizando su DNI único.")
	@GetMapping("/dni/{dni}")
	public ResponseEntity<AlumnoResponse> buscarPorDni(@PathVariable("dni") String dni) {
		return alumnoService.buscarPorDni(dni)
				.map(AlumnoResponse::fromModel)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	@Operation(summary = "Buscar alumnos por criterios", description = "Permite buscar alumnos utilizando uno o más criterios de búsqueda, como nombre, apellido, DNI o legajo.")
	@GetMapping("/buscar")
	public ResponseEntity<List<AlumnoResponse>> buscar(
			@RequestParam(value = "nombre", required = false) String nombre,
			@RequestParam(value = "apellido", required = false) String apellido,
			@RequestParam(value = "dni", required = false) String dni,
			@RequestParam(value = "legajo", required = false) String legajo) {
		List<AlumnoResponse> respuesta = alumnoService.buscar(nombre, apellido, dni, legajo).stream()
				.map(AlumnoResponse::fromModel)
				.toList();
		return ResponseEntity.ok(respuesta);
	}

	@Operation(summary = "Crear alumno", description = "Permite crear un nuevo alumno en el sistema proporcionando los datos necesarios.")
	@PostMapping
	public ResponseEntity<AlumnoResponse> crear(@Valid @RequestBody AlumnoRequest alumno) {
		Alumno alumnoCreado = alumnoService.crear(alumno.toModel());
		URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
				.buildAndExpand(alumnoCreado.getId()).toUri();
		return ResponseEntity.created(ubicacion).body(AlumnoResponse.fromModel(alumnoCreado));
	}

	@Operation(summary = "Actualizar alumno", description = "Permite actualizar los datos de un alumno existente utilizando su ID único.")
	@PutMapping("/{id}")
	public ResponseEntity<AlumnoResponse> actualizar(@PathVariable("id") Long id, @RequestBody AlumnoRequest alumno) {
		return alumnoService.actualizar(id, alumno.toModel())
				.map(AlumnoResponse::fromModel)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	@Operation(summary = "Actualizar apellido de alumno", description = "Permite actualizar únicamente el apellido de un alumno existente utilizando su ID único." )
	@PatchMapping("/{id}")
	public ResponseEntity<AlumnoResponse> actualizarApellido(@PathVariable("id") Long id, @RequestBody Map<String, String> cambios) {
		String nuevoApellido = cambios.get("apellido");
		if (nuevoApellido == null) {
			return ResponseEntity.badRequest().build();
		}
		return alumnoService.actualizarApellido(id, nuevoApellido)
				.map(AlumnoResponse::fromModel)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> eliminar(@PathVariable("id") Long id) {
		return alumnoService.eliminar(id)
				? ResponseEntity.noContent().build()
				: ResponseEntity.notFound().build();
	}
	
	@PostMapping("/{id}/materias/{materiaId}")
	public ResponseEntity<AlumnoResponse> asignarMateria(@PathVariable("id") Long id,@PathVariable("materiaId") Long materiaId) {
		Alumno alumnoActualizado = alumnoService.inscribirEnMateria(id, materiaId);
		return ResponseEntity.ok(AlumnoResponse.fromModel(alumnoActualizado));
	}
	
	@GetMapping("/{id}/materias")
	public ResponseEntity<List<Materia>> materiasDeAlumno(@PathVariable("id") Long id) {
	    return ResponseEntity.ok(alumnoService.obtenerMateriasDeAlumno(id));
	}
}