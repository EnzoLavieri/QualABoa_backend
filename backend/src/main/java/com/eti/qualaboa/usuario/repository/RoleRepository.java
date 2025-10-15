package com.eti.qualaboa.usuario.repository;

import com.eti.qualaboa.usuario.domain.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role,Long> {
    Role findByNome(String role);
}
