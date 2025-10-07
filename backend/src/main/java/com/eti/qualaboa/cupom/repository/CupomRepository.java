package com.eti.qualaboa.cupom.repository;

import com.eti.qualaboa.cupom.model.Cupom;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CupomRepository extends JpaRepository<Cupom, Long> {
    List<Cupom> findByEstabelecimentoIdEstabelecimento(Long idEstabelecimento);
}

