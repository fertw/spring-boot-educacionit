package com.educacionit.alumnos_api.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

import com.educacionit.alumnos_api.model.Alumno;
import com.educacionit.alumnos_api.repository.AlumnoRepository;

@Service
public class AlumnoService {
	
	private final AlumnoRepository alumnoRepository;
	
	public AlumnoService(AlumnoRepository alumnoRepository) {
		this.alumnoRepository = alumnoRepository;
	}
	
	public List<Alumno> listar() {
		return alumnoRepository.findAll();
	}
	
	public Optional<Alumno> buscarPorId(Long id) {
		return alumnoRepository.findById(id);
	}
	
	public Alumno crear(Alumno alumno) {
		return alumnoRepository.save(alumno);
	}
	
	public Optional<Alumno> actualizar(Long id, Alumno alumno) {
		return alumnoRepository.findById(id).map(existente -> {
			alumno.setId(id);
            return alumnoRepository.save(alumno);
        });
	}
	
	public boolean eliminar(Long id) {
		return alumnoRepository.findById(id).map(existente -> {
			alumnoRepository.deleteById(id);
			return true;
		}).orElse(false);
	}

	public Optional<Alumno> actualizarApellido(Long id, String nuevoApellido) {
		
		return alumnoRepository.findById(id).map(existente -> {
			existente.setApellido(nuevoApellido);
			return alumnoRepository.save(existente);
		});
	}

	public List<Alumno> buscar(String nombre, String apellido, String dni, String legajo) {
		return alumnoRepository.findAll().stream()
				.filter(alumno -> (nombre == null || alumno.getNombre().equalsIgnoreCase(nombre)) &&
						(apellido == null || alumno.getApellido().equalsIgnoreCase(apellido)) &&
						(dni == null || alumno.getDni().equalsIgnoreCase(dni)) &&
						(legajo == null || alumno.getLegajo().equalsIgnoreCase(legajo)))
				.toList();	
	}

	public Optional<Alumno> buscarPorDni(String dni) {
		return alumnoRepository.findAll().stream()
				.filter(alumno -> alumno.getDni().equalsIgnoreCase(dni))
				.findFirst();
	}

	public Optional<Alumno> buscarPorLegajo(String legajo) {
		return alumnoRepository.findAll().stream()
				.filter(alumno -> alumno.getLegajo().equalsIgnoreCase(legajo))
				.findFirst();
	}

}
