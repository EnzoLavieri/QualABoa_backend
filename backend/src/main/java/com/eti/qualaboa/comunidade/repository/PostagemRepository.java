package com.eti.qualaboa.comunidade.repository;

import com.eti.qualaboa.comunidade.domain.entity.Postagem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostagemRepository extends JpaRepository<Postagem, Long> {
    Page<Postagem> findByComunidadeIdOrderByDataHoraDesc(Long comunidadeId, Pageable pageable);
}