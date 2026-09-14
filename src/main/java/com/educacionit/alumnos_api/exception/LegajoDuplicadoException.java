package com.educacionit.alumnos_api.exception;

public class LegajoDuplicadoException extends RuntimeException {
	public LegajoDuplicadoException(String legajo) {
		super("Ya existe un alumno con el legajo: " + legajo);
	}

}
