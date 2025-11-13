package com.eti.qualaboa.metricas.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Date;

@Getter
@Setter
public class CliquesPorDiaDTO {

    private Date data;
    private Long total;

    public CliquesPorDiaDTO(Date data, Long total) {
        this.data = data;
        this.total = total;
    }
}