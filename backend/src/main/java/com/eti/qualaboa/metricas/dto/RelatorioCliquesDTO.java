package com.eti.qualaboa.metricas.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class RelatorioCliquesDTO {
    private List<CliquesPorDiaDTO> detalhamentoPorDia;
}
