package com.eti.qualaboa.usuario.repository;

import com.eti.qualaboa.usuario.domain.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario,Long> {

}
