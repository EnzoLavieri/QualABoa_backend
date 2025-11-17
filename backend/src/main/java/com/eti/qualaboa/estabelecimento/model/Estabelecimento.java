package com.eti.qualaboa.estabelecimento.model;

import com.eti.qualaboa.config.dto.LoginRequest;
import com.eti.qualaboa.endereco.Endereco;
import com.eti.qualaboa.evento.model.Evento;
import com.eti.qualaboa.cupom.model.Cupom;
import com.eti.qualaboa.metricas.model.Metricas;
import com.eti.qualaboa.usuario.domain.entity.Role;
import com.eti.qualaboa.usuario.domain.entity.Usuario;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Set;

@Entity
@Table(name = "estabelecimentos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "idEstabelecimento")
@ToString(exclude = {"endereco", "eventos", "cupons", "roles", "favoritadoPorUsuarios"})
public class Estabelecimento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idEstabelecimento;

    private String nome;

    @Column(name = "nome_normalizado")
    private String nomeNormalizado;

    private String senha;
    private String email;
    private String categoria;
    private String descricao;
    private String telefone;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "idEndereco")
    private Endereco endereco;

    // Adiciona coluna de foto do estabelecimento
    @ManyToOne
    @JoinColumn(name = "id_role")
    private Role idRole;

    @Column(name = "foto_url", length = 1000)
    private String fotoUrl;

//    @Lob
//    @JsonIgnore
//    private byte[] imagemPerfil;

    private Double classificacao;

    @ElementCollection
    @CollectionTable(
            name = "estabelecimento_conveniencias",
            joinColumns = @JoinColumn(name = "idEstabelecimento")
    )
    @Column(name = "conveniencia")
    private List<String> conveniencias;

    // -------- integração com Google Places / Maps ----------
    private Boolean parceiro = false;       // se é parceiro do app
    private String placeId;                 // ID do Google Places
    private Double latitude;
    private Double longitude;

    @Column(length = 1000)
    private String enderecoFormatado;       // endereço obtido pelo Places

    //relacionamentos
    @OneToMany(mappedBy = "estabelecimento", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Evento> eventos;

    @OneToMany(mappedBy = "estabelecimento", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Cupom> cupons;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "estabelecimento_roles",
            joinColumns = @JoinColumn(name = "id_estabelecimento"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles;

    @ManyToMany(mappedBy = "favoritos", fetch = FetchType.LAZY)
    private Set<Usuario> favoritadoPorUsuarios;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "metricas_id")
    private Metricas metricas;

    public boolean isLoginCorrect(LoginRequest loginRequest, PasswordEncoder passwordEncoder) {
        return passwordEncoder.matches(loginRequest.password(), this.senha);
    }

    public Object getId() {
        return  this.getIdEstabelecimento();
    }

    public Metricas getMetricas() {
        if (this.metricas == null) {
            this.setMetricas(new Metricas());
        }
        return this.metricas;
    }
}
