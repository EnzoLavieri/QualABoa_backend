package com.eti.qualaboa.estabelecimento.repository;

import com.eti.qualaboa.estabelecimento.model.Estabelecimento;
import com.eti.qualaboa.usuario.domain.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EstabelecimentoRepository extends JpaRepository<Estabelecimento, Long> {
    boolean existsByEmail(String email);
    Optional<Estabelecimento> findByEmail(String email);
}
