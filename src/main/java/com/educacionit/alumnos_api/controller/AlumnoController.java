package com.educacionit.alumnos_api.controller;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

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

import com.educacionit.alumnos_api.model.Alumno;

@RestController
@RequestMapping("/alumnos")
public class AlumnoController {

	private final List<Alumno> alumnos = new ArrayList<>();
	private final AtomicLong counter = new AtomicLong();

	public AlumnoController() {
		// Agregar algunos alumnos de ejemplo
		alumnos.add(new Alumno(counter.incrementAndGet(), "Juan", "Pérez", "12345678", "A001"));
		alumnos.add(new Alumno(counter.incrementAndGet(), "María", "Gómez", "87654321", "A002"));
	}

	@PostMapping
	public ResponseEntity<Alumno> crear(@RequestBody Alumno alumno) {
		alumno.setId(counter.incrementAndGet());
		alumnos.add(alumno);		
		
		URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
				.buildAndExpand(alumno.getId()).toUri();
		return ResponseEntity.created(ubicacion).body(alumno);
	}

	@PutMapping("/{id}")
	public ResponseEntity<Alumno> actualizar(@PathVariable("id") Long id, @RequestBody Alumno alumno) {
	    Alumno alumnoExistente = alumnos.stream()
	        .filter(a -> a.getId().equals(id))
	        .findFirst()
	        .orElse(null);

	    if (alumnoExistente == null) {
	        return ResponseEntity.notFound().build();
	    }

	    alumnoExistente.setNombre(alumno.getNombre());
	    alumnoExistente.setApellido(alumno.getApellido());
	    alumnoExistente.setDni(alumno.getDni());
	    alumnoExistente.setLegajo(alumno.getLegajo());
	    return ResponseEntity.ok(alumnoExistente);
	}
	
	@PatchMapping("/{id}")
	public ResponseEntity<Alumno> actualizarApellido(@PathVariable("id") Long id, @RequestBody Map<String, String> cambios) {
	    Alumno alumnoExistente = alumnos.stream()
	        .filter(a -> a.getId().equals(id))
	        .findFirst()
	        .orElse(null);

	    if (alumnoExistente == null) {
	        return ResponseEntity.notFound().build();
	    }

	    String nuevoApellido = cambios.get("apellido");
	    if (nuevoApellido == null) {
	        return ResponseEntity.badRequest().build();
	    }

	    alumnoExistente.setApellido(nuevoApellido);
	    return ResponseEntity.ok(alumnoExistente);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> eliminar(@PathVariable("id") Long id) {
		boolean borrado = alumnos.removeIf(a -> a.getId().equals(id));
		if (!borrado) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.noContent().build();
	}

	@GetMapping
	public ResponseEntity<List<Alumno>> listar() {
		return ResponseEntity.ok(alumnos);
	}

	@GetMapping("/{id}")
	public ResponseEntity<Alumno> buscarPorId(@PathVariable("id") Long id) {
		boolean alumnoExistente = alumnos.stream().anyMatch(alumno -> alumno.getId().equals(id));
		if (!alumnoExistente) {
			return ResponseEntity.notFound().build();
		}
		Alumno alumno = alumnos.stream().filter(a -> a.getId().equals(id)).findFirst().orElse(null);
		return ResponseEntity.ok(alumno);
	}
		

	@GetMapping("/legajo/{legajo}")
	public ResponseEntity<Alumno> buscarPorLegajo(@PathVariable("legajo") String legajo) {
		Alumno alu = alumnos.stream().filter(alumno -> alumno.getLegajo().equals(legajo)).findFirst().orElse(null);
		if (alu == null) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(alu);
	}

	@GetMapping("/dni/{dni}")
	public Alumno buscarPorDni(@PathVariable("dni") String dni) {
		return alumnos.stream().filter(alumno -> alumno.getDni().equals(dni)).findFirst().orElse(null);
	}

	@GetMapping("/buscar")
	public List<Alumno> buscar(@RequestParam("nombre") String nombre, @RequestParam("apellido") String apellido) {
		return alumnos.stream().filter(alumno -> alumno.getNombre().equalsIgnoreCase(nombre)
				&& alumno.getApellido().equalsIgnoreCase(apellido)).toList();
	}

}
