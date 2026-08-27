package com.educacionit.alumnos_api.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;

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
	@ResponseStatus(HttpStatus.CREATED)
	public Alumno crear (@RequestBody Alumno alumno) {
		alumno.setId(counter.incrementAndGet());
		alumnos.add(alumno);
		return alumno;
	}
	
	@GetMapping
	public List<Alumno> listar() {
		return alumnos;
	}
	
	@GetMapping("/{id}")
	public Alumno buscarPorId(@PathVariable("id") Long id) {
		return alumnos.stream()
				.filter(alumno -> alumno.getId().equals(id))
				.findFirst()
				.orElse(null);
	}
	
	@GetMapping("/legajo/{legajo}")
	public Alumno buscarPorLegajo(@PathVariable("legajo") String legajo) {
		return alumnos.stream()
				.filter(alumno -> alumno.getLegajo().equals(legajo))
				.findFirst()
				.orElse(null);
	}
	
	@GetMapping("/dni/{dni}")
	public Alumno buscarPorDni(@PathVariable("dni") String dni) {
		return alumnos.stream()
				.filter(alumno -> alumno.getDni().equals(dni))
				.findFirst()
				.orElse(null);
	}
	
	@GetMapping("/buscar")
	public List<Alumno> buscar(@RequestParam("nombre") String nombre, 
			                   @RequestParam("apellido") String apellido) {
		return alumnos.stream()
				.filter(alumno -> alumno.getNombre().equalsIgnoreCase(nombre) && 
						         alumno.getApellido().equalsIgnoreCase(apellido))
				.toList();
	}
	
}
