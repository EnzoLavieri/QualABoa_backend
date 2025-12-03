package com.eti.qualaboa.comunidade.repository;

import com.eti.qualaboa.comunidade.domain.entity.Comunidade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ComunidadeRepository extends JpaRepository<Comunidade, Long> {

    Optional<Comunidade> findByDonoIdEstabelecimento(Long idEstabelecimento);

    @Query("SELECT c FROM Comunidade c JOIN c.membros m WHERE m.id = :usuarioId")
    List<Comunidade> findByMembrosId(@Param("usuarioId") Long usuarioId);
}