package com.educacionit.alumnos_api.repository;
import java.util.List;
import java.util.Optional;
import com.educacionit.alumnos_api.model.Alumno;

public interface AlumnoRepository {
	
	List<Alumno> findAll();
	Optional<Alumno> findById(Long id);
	Alumno save(Alumno alumno);
	void deleteById(Long id);
	

}
