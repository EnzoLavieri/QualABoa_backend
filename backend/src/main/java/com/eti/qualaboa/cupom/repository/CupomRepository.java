package com.eti.qualaboa.cupom.repository;

import com.eti.qualaboa.cupom.model.Cupom;
import com.eti.qualaboa.estabelecimento.model.Estabelecimento;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CupomRepository extends JpaRepository<Cupom, Long> {
    List<Cupom> findByEstabelecimentoIdEstabelecimento(Long idEstabelecimento);

    Optional<Cupom> findByCodigo(String codigo);
}

