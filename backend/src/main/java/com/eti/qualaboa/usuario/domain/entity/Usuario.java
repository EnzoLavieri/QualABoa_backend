package com.eti.qualaboa.usuario.domain.entity;

import com.eti.qualaboa.config.dto.LoginRequest;
import com.eti.qualaboa.enums.Sexo;
import com.eti.qualaboa.estabelecimento.model.Estabelecimento;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Set;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@ToString
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "usuario_id")
    private Long id;

    private String nome;
    private String email;
    private String senha;
    private Sexo sexo;

    @Column(name = "foto_url", length = 1000)
    private String fotoUrl;

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(
            name = "tb_users_roles",
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name="usuario_preferencias", joinColumns=@JoinColumn(name="usuario_id"))
    private List<String> preferenciasUsuario;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "usuario_favoritos_estabelecimento",
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "estabelecimento_id")
    )
    private Set<Estabelecimento> favoritos;

    public boolean isLoginCorrect(LoginRequest loginRequest, PasswordEncoder passwordEncoder) {
        return passwordEncoder.matches(loginRequest.password(), this.senha);
    }

    // falta checkins e avaliações
    // falta imagens
}
