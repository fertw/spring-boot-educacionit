package com.educacionit.alumnos_api.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.JoinColumn;


@Entity
public class Alumno {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String nombre;
	private String apellido;
	private String dni;
	private String legajo;
	
	@ManyToMany (fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
	@JoinTable(
		    name = "alumno_materia",
		    joinColumns = @JoinColumn(name = "alumno_id"),
		    inverseJoinColumns = @JoinColumn(name = "materia_id")
		)
	private List<Materia> materias = new ArrayList<>();

	public Alumno() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Alumno(Long id, String nombre, String apellido, String dni, String legajo) {
		super();
		this.id = id;
		this.nombre = nombre;
		this.apellido = apellido;
		this.dni = dni;
		this.legajo = legajo;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getApellido() {
		return apellido;
	}

	public void setApellido(String apellido) {
		this.apellido = apellido;
	}

	public String getDni() {
		return dni;
	}

	public void setDni(String dni) {
		this.dni = dni;
	}

	public String getLegajo() {
		return legajo;
	}

	public void setLegajo(String legajo) {
		this.legajo = legajo;
	}
	
	public List<Materia> getMaterias() {
		return materias;
	}
	
	public void setMaterias(List<Materia> materias) {
		this.materias = materias;
	}

}
