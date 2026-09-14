package com.educacionit.alumnos_api.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.educacionit.alumnos_api.dto.FeriadoResponse;
import com.educacionit.alumnos_api.service.FeriadoService;


@RestController
@RequestMapping("/externo/feriados")
public class FeriadoController {
	
	private final FeriadoService feriadoService;

	public FeriadoController(FeriadoService feriadoService) {
		this.feriadoService = feriadoService;
	}
	
	@GetMapping("/{anio}")
	public ResponseEntity<List<FeriadoResponse>> obtenerFeriados(@PathVariable int anio) {
		List<FeriadoResponse> feriados = feriadoService.obtenerFeriados(anio);
		return ResponseEntity.ok(feriados);
	}
	


}
