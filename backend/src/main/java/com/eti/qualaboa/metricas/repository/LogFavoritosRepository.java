package com.eti.qualaboa.metricas.repository;

import com.eti.qualaboa.metricas.dto.CliquesPorDiaDTO;
import com.eti.qualaboa.metricas.model.LogFavoritos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LogFavoritosRepository extends JpaRepository<LogFavoritos, Long> {

    @Query("SELECT new com.eti.qualaboa.metricas.dto.CliquesPorDiaDTO(CAST(l.dataHoraFavorito AS date), COUNT(l)) " +
            "FROM LogFavoritos l " +
            "WHERE l.estabelecimento.idEstabelecimento = :idEstabelecimento " +
            "GROUP BY CAST(l.dataHoraFavorito AS date) " +
            "ORDER BY CAST(l.dataHoraFavorito AS date) DESC")
    List<CliquesPorDiaDTO> findFavoritosAgrupadosPorDia(@Param("idEstabelecimento") Long idEstabelecimento);

}
