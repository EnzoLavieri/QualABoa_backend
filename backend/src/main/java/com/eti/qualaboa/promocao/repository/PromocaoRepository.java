package com.eti.qualaboa.promocao.repository;

import com.eti.qualaboa.promocao.model.Promocao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PromocaoRepository extends JpaRepository<Promocao, Long> {
}

