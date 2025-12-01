package com.eti.qualaboa.comunidade.repository;

import com.eti.qualaboa.comunidade.domain.entity.OpcaoEnquete;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OpcaoEnqueteRepository extends JpaRepository<OpcaoEnquete, Long> {
}