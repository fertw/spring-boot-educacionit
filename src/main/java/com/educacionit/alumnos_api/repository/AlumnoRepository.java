package com.educacionit.alumnos_api.repository;
import org.springframework.data.jpa.repository.JpaRepository;

import com.educacionit.alumnos_api.model.Alumno;

public interface AlumnoRepository extends JpaRepository<Alumno, Long> {


}
