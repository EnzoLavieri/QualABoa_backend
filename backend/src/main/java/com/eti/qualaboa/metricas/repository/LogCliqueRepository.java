package com.eti.qualaboa.metricas.repository;

import com.eti.qualaboa.metricas.dto.CliquesPorDiaDTO;
import com.eti.qualaboa.metricas.model.LogClique;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LogCliqueRepository extends JpaRepository<LogClique, Long> {
    @Query("SELECT new com.eti.qualaboa.metricas.dto.CliquesPorDiaDTO(CAST(l.dataHoraClique AS date), COUNT(l)) " +
            "FROM LogClique l " +
            "WHERE l.estabelecimento.idEstabelecimento = :idEstabelecimento " +
            "GROUP BY CAST(l.dataHoraClique AS date) " +
            "ORDER BY CAST(l.dataHoraClique AS date) DESC")
        List<CliquesPorDiaDTO> findCliquesAgrupadosPorDia(@Param("idEstabelecimento") Long idEstabelecimento);
}
