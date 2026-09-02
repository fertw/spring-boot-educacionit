package com.educacionit.alumnos_api.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Repository;

import com.educacionit.alumnos_api.model.Alumno;

@Repository
public class AlumnoRepositoryEnMemoria implements AlumnoRepository {
	
	private final List<Alumno> alumnos = new ArrayList<>();
	private final AtomicLong counter = new AtomicLong();


	@Override
	public List<Alumno> findAll() {
		return alumnos;
	}

	@Override
	public Optional<Alumno> findById(Long id) {
		return alumnos.stream()
				.filter(alumno -> alumno.getId().equals(id))
				.findFirst();
	}

	@Override
	public Alumno save(Alumno alumno) {	
		if (alumno.getId() == null) {
			alumno.setId(counter.incrementAndGet());
			alumnos.add(alumno);
		} else {
			Optional<Alumno> alumnoExistente = findById(alumno.getId());
			if (alumnoExistente.isPresent()) {
				alumnos.remove(alumnoExistente.get());
				alumnos.add(alumno);
			} else {
				alumnos.add(alumno);
			}
		}
		return alumno;
	}

	@Override
	public void deleteById(Long id) {
		alumnos.removeIf(alumno -> alumno.getId().equals(id));
	}


}
