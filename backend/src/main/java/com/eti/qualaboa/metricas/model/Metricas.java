package com.eti.qualaboa.metricas.model;

import com.eti.qualaboa.estabelecimento.model.Estabelecimento;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@Table(name = "metricas")
@ToString
public class Metricas {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long cliques = 0L;
    private Long totalFavoritos = 0L;

    @OneToOne(mappedBy = "metricas")
    private Estabelecimento estabelecimento;

}
