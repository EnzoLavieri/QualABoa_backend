package com.eti.qualaboa.metricas.repository;

import com.eti.qualaboa.metricas.model.LogClique;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MetricasRepository extends JpaRepository<LogClique, Long> {
    Long id(Long id);
}
