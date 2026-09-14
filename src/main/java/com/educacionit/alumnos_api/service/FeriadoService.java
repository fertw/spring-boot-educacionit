package com.educacionit.alumnos_api.service;

import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.educacionit.alumnos_api.dto.FeriadoResponse;

@Service
public class FeriadoService {

    private final RestClient restClient;

    public FeriadoService(RestClient restClient) {
        this.restClient = restClient;
    }

    public List<FeriadoResponse> obtenerFeriados(int anio) {
        return restClient.get()
                .uri("/feriados/{anio}", anio)
                .retrieve()
                .onStatus(status -> status.value() == 404,
                        (request, response) -> {
                            throw new IllegalArgumentException("No hay feriados publicados para el año " + anio);
                        })
                .body(new ParameterizedTypeReference<List<FeriadoResponse>>() {});
    }
}