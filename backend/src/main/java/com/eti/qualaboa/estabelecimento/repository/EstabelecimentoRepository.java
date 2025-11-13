package com.eti.qualaboa.estabelecimento.repository;

import com.eti.qualaboa.estabelecimento.model.Estabelecimento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

import java.util.Optional;

public interface EstabelecimentoRepository extends JpaRepository<Estabelecimento, Long> {

    boolean existsByEmail(String email);

    // Exemplo de busca espacial com PostGIS
    @Query(value = """
            SELECT * FROM estabelecimentos e
            WHERE e.latitude IS NOT NULL
            AND e.longitude IS NOT NULL
            AND ST_DWithin(
                ST_SetSRID(ST_MakePoint(e.longitude, e.latitude), 4326)::geography,
                ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography,
                :radius
            )
            """, nativeQuery = true)
    List<Estabelecimento> findAllWithinRadiusPostGis(
            @Param("lat") double lat,
            @Param("lng") double lng,
            @Param("radius") double radiusMeters);

    Optional<Estabelecimento> findByEmail(String email);

    Optional<Estabelecimento> findByNomeNormalizado(String nomeNormalizado);
}
