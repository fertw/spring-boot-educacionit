package com.educacionit.alumnos_api.exception;

public class AlumnoNoEncontradoException extends RuntimeException {
	public AlumnoNoEncontradoException(Long id) {
		super("No existe el alumno con id: " + id);
	}

}
