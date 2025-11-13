package com.eti.qualaboa.metricas.repository;

import com.eti.qualaboa.metricas.dto.CliquesPorDiaDTO;
import com.eti.qualaboa.metricas.model.LogBuscaPeloNome;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LogBuscaPeloNomeRepository extends JpaRepository<LogBuscaPeloNome, Long> {

    @Query("SELECT new com.eti.qualaboa.metricas.dto.CliquesPorDiaDTO(CAST(l.dataHoraBusca AS date), COUNT(l))" +
            " FROM LogBuscaPeloNome l " +
            " WHERE l.estabelecimento.idEstabelecimento = :idEstabelecimento " +
            " GROUP BY CAST(l.dataHoraBusca AS date) " +
            " ORDER BY CAST(l.dataHoraBusca AS date) DESC")
    List<CliquesPorDiaDTO> findBuscasAgrupadasPorDia(@Param("idEstabelecimento") Long idEstabelecimento);
}
