package com.educacionit.alumnos_api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FeriadoResponse(
        String fecha,
        String tipo,
        String nombre
) {}