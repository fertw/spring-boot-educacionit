package com.educacionit.alumnos_api.dto;

import com.educacionit.alumnos_api.model.Alumno;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AlumnoRequest (
		
		@NotBlank(message = "El nombre no puede estar vacío")
		String nombre, 
		@NotBlank(message = "El apellido no puede estar vacío")
		String apellido, 
		@NotBlank(message = "El DNI no puede estar vacío")
		@Pattern(regexp = "\\d{7,8}", message = "El DNI debe tener 7 u 8 dígitos")
		String dni, 
		
		@NotBlank(message = "El legajo no puede estar vacío")
		String legajo) {
	
	public Alumno toModel() {
		Alumno alumno = new Alumno();
		alumno.setNombre(nombre);
		alumno.setApellido(apellido);
		alumno.setDni(dni);
		alumno.setLegajo(legajo);
		return alumno;
		
	}

}
