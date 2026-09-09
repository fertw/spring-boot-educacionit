package com.educacionit.alumnos_api.service;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.educacionit.alumnos_api.model.Materia;
import com.educacionit.alumnos_api.repository.MateriaRepository;

@Service
public class MateriaService {
	
	private final MateriaRepository materiaRepository;
	
	public MateriaService(MateriaRepository materiaRepository) {
		this.materiaRepository = materiaRepository;
	}
	
	public Optional<Materia> findByCodigo(String codigo) {
		return materiaRepository.findByCodigo(codigo);
	}

}
