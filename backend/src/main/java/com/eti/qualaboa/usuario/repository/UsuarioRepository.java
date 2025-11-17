package com.eti.qualaboa.usuario.repository;

import com.eti.qualaboa.usuario.domain.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario,Long> {
    Optional<Usuario> findByEmail(String email);

    @Query("SELECT u FROM Usuario u LEFT JOIN FETCH u.favoritos WHERE u.id = :id")
    Optional<Usuario> findUserFaviritosById(@Param("id") Long id);
}
