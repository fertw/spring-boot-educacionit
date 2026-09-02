package com.educacionit.alumnos_api.dto;

import com.educacionit.alumnos_api.model.Alumno;

public record AlumnoResponse (Long id, String nombre, String apellido, String dni, String legajo) {
	
	public static AlumnoResponse fromModel(Alumno alumno) {
		return new AlumnoResponse(alumno.getId(), alumno.getNombre(), alumno.getApellido(), alumno.getDni(), alumno.getLegajo());
	}

}
