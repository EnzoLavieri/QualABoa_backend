package com.eti.qualaboa.usuario.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tb_role")
@Getter
@Setter
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Long id;
    private String nome;

    public enum Values {
        USER(1L),
        ADMIN(2L),
        ESTABELECIMENTO(3L);

        long roleId;

        Values(long roleId) {
            this.roleId = roleId;
        }
    }
}
