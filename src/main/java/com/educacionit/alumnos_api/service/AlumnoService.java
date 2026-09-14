package com.educacionit.alumnos_api.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.educacionit.alumnos_api.dto.AlumnoResponse;
import com.educacionit.alumnos_api.exception.AlumnoNoEncontradoException;
import com.educacionit.alumnos_api.model.Alumno;
import com.educacionit.alumnos_api.model.Materia;
import com.educacionit.alumnos_api.repository.AlumnoRepository;
import com.educacionit.alumnos_api.repository.MateriaRepository;

@Service
public class AlumnoService {
	
	private final AlumnoRepository alumnoRepository;
	private final MateriaRepository materiaRepository;
	
	public AlumnoService(AlumnoRepository alumnoRepository, MateriaRepository materiaRepository) {
		this.alumnoRepository = alumnoRepository;
		this.materiaRepository = materiaRepository;
	}
	
	public Page<Alumno> listar(Pageable pageable) {
		return alumnoRepository.findAll(pageable);
	}
	
	public AlumnoResponse buscarPorId(Long id) {
		Alumno alumno = alumnoRepository.findById(id)
				.orElseThrow(() -> new AlumnoNoEncontradoException(id));
		return AlumnoResponse.fromModel(alumno);
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
	
	public Alumno inscribirEnMateria(Long alumnoId, Long materiaId) {
	    Alumno alumno = alumnoRepository.findById(alumnoId)
	        .orElseThrow(() -> new RuntimeException("Alumno no encontrado"));
	    Materia materia = materiaRepository.findById(materiaId)
	        .orElseThrow(() -> new RuntimeException("Materia no encontrada"));

	    alumno.getMaterias().add(materia);
	    return alumnoRepository.save(alumno);
	}

	public List<Materia> obtenerMateriasDeAlumno(Long alumnoId) {
	    Alumno alumno = alumnoRepository.findById(alumnoId)
	        .orElseThrow(() -> new RuntimeException("Alumno no encontrado"));
	    return alumno.getMaterias();
	}

}
