package com.eti.qualaboa.estabelecimento.repository;

import com.eti.qualaboa.estabelecimento.model.Estabelecimento;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EstabelecimentoRepository extends JpaRepository<Estabelecimento, Long> {
    boolean existsByEmail(String email);
}
