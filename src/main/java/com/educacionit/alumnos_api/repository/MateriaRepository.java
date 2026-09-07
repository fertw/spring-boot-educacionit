package com.educacionit.alumnos_api.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.educacionit.alumnos_api.model.Materia;

public interface MateriaRepository extends JpaRepository<Materia, Long> {

}
