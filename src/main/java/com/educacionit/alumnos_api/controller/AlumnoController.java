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
import org.springframework.http.ResponseEntity;

import com.educacionit.alumnos_api.dto.AlumnoRequest;
import com.educacionit.alumnos_api.dto.AlumnoResponse;
import com.educacionit.alumnos_api.model.Alumno;
import com.educacionit.alumnos_api.service.AlumnoService;

@RestController
@RequestMapping("/alumnos")
public class AlumnoController {

	private final AlumnoService alumnoService;

	public AlumnoController(AlumnoService alumnoService) {
		this.alumnoService = alumnoService;
	}

	@GetMapping
	public ResponseEntity<List<AlumnoResponse>> listar() {
		List<AlumnoResponse> respuesta = alumnoService.listar().stream()
				.map(AlumnoResponse::fromModel)
				.toList();
		return ResponseEntity.ok(respuesta);
	}

	@GetMapping("/{id}")
	public ResponseEntity<AlumnoResponse> buscarPorId(@PathVariable("id") Long id) {
		return alumnoService.buscarPorId(id)
				.map(AlumnoResponse::fromModel)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	@GetMapping("/legajo/{legajo}")
	public ResponseEntity<AlumnoResponse> buscarPorLegajo(@PathVariable("legajo") String legajo) {
		return alumnoService.buscarPorLegajo(legajo)
				.map(AlumnoResponse::fromModel)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	@GetMapping("/dni/{dni}")
	public ResponseEntity<AlumnoResponse> buscarPorDni(@PathVariable("dni") String dni) {
		return alumnoService.buscarPorDni(dni)
				.map(AlumnoResponse::fromModel)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

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

	@PostMapping
	public ResponseEntity<AlumnoResponse> crear(@RequestBody AlumnoRequest alumno) {
		Alumno alumnoCreado = alumnoService.crear(alumno.toModel());
		URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
				.buildAndExpand(alumnoCreado.getId()).toUri();
		return ResponseEntity.created(ubicacion).body(AlumnoResponse.fromModel(alumnoCreado));
	}

	@PutMapping("/{id}")
	public ResponseEntity<AlumnoResponse> actualizar(@PathVariable("id") Long id, @RequestBody AlumnoRequest alumno) {
		return alumnoService.actualizar(id, alumno.toModel())
				.map(AlumnoResponse::fromModel)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

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
}