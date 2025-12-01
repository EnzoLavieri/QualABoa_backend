package com.eti.qualaboa.comunidade.repository;

import com.eti.qualaboa.comunidade.domain.entity.Reacao;
import com.eti.qualaboa.comunidade.domain.enums.TipoReacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReacaoRepository extends JpaRepository<Reacao, Long> {

    boolean existsByPostagemIdAndUsuarioId(Long postagemId, Long usuarioId);

    Optional<Reacao> findByPostagemIdAndUsuarioId(Long postagemId, Long usuarioId);

    long countByPostagemIdAndTipo(Long postagemId, TipoReacao tipo);
}