package com.eti.qualaboa.evento.repository;

import com.eti.qualaboa.evento.model.Evento;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EventoRepository extends JpaRepository<Evento, Long> {
    List<Evento> findByEstabelecimentoIdEstabelecimento(Long idEstabelecimento);
}

