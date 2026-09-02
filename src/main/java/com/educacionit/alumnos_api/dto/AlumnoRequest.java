package com.educacionit.alumnos_api.dto;

import com.educacionit.alumnos_api.model.Alumno;

public record AlumnoRequest (String nombre, String apellido, String dni, String legajo) {
	
	public Alumno toModel() {
		Alumno alumno = new Alumno();
		alumno.setNombre(nombre);
		alumno.setApellido(apellido);
		alumno.setDni(dni);
		alumno.setLegajo(legajo);
		return alumno;
		
	}

}
