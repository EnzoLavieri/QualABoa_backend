package com.eti.qualaboa.estabelecimento.service;

import com.eti.qualaboa.endereco.Endereco;
import com.eti.qualaboa.estabelecimento.dto.EstabelecimentoDTO;
import com.eti.qualaboa.estabelecimento.dto.EstabelecimentoRegisterDTO;
import com.eti.qualaboa.estabelecimento.dto.EstabelecimentoResponseDTO;
import com.eti.qualaboa.estabelecimento.model.Estabelecimento;
import com.eti.qualaboa.estabelecimento.repository.EstabelecimentoRepository;
import com.eti.qualaboa.usuario.domain.entity.Role;
import com.eti.qualaboa.usuario.repository.RoleRepository;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class EstabelecimentoService {

    private final EstabelecimentoRepository repositoryEstabelecimento;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public EstabelecimentoService(EstabelecimentoRepository repositoryEstabelecimento, RoleRepository roleRepository, BCryptPasswordEncoder passwordEncoder) {
        this.repositoryEstabelecimento = repositoryEstabelecimento;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public EstabelecimentoResponseDTO criar(EstabelecimentoRegisterDTO estabelecimentoRequest) {
        if (repositoryEstabelecimento.existsByEmail(estabelecimentoRequest.getEmail())) {
            throw new RuntimeException("E-mail já cadastrado");
        }

        Estabelecimento estabelecimento = new Estabelecimento();

        estabelecimento.setNome(estabelecimentoRequest.getNome());
        estabelecimento.setEmail(estabelecimentoRequest.getEmail());
        estabelecimento.setSenha(passwordEncoder.encode(estabelecimentoRequest.getSenha()));
        estabelecimento.setCategoria(estabelecimentoRequest.getCategoria());
        estabelecimento.setDescricao(estabelecimentoRequest.getDescricao());
        estabelecimento.setTelefone(estabelecimentoRequest.getTelefone());
        estabelecimento.setEndereco(estabelecimentoRequest.getEndereco());
        estabelecimento.setConveniencias(estabelecimentoRequest.getConveniencias());

        if (estabelecimentoRequest.getIdRole() == 3){
            Role role = roleRepository.findByNome("ESTABELECIMENTO");
            estabelecimento.setRoles(Set.of(role));
        }

        Estabelecimento salvo = repositoryEstabelecimento.save(estabelecimento);
        return new EstabelecimentoResponseDTO(
                salvo.getIdEstabelecimento(),
                salvo.getNome(),
                salvo.getEmail(),
                salvo.getCategoria(),
                salvo.getDescricao(),
                salvo.getTelefone(),
                salvo.getEndereco(),
                salvo.getClassificacao(),
                salvo.getConveniencias()
        );
    }

    public List<EstabelecimentoDTO> listarTodos() {
        return repositoryEstabelecimento.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public EstabelecimentoDTO buscarPorId(Long id) {
        Estabelecimento est = repositoryEstabelecimento.findById(id)
                .orElseThrow(() -> new RuntimeException("Estabelecimento não encontrado"));
        return toDTO(est);
    }

    public EstabelecimentoDTO atualizar(Long id, Estabelecimento dto) {
        Estabelecimento existente = repositoryEstabelecimento.findById(id)
                .orElseThrow(() -> new RuntimeException("Estabelecimento não encontrado"));

        existente.setNome(dto.getNome());
        existente.setCategoria(dto.getCategoria());
        existente.setDescricao(dto.getDescricao());
        existente.setTelefone(dto.getTelefone());
        existente.setConveniencias(dto.getConveniencias());
        existente.setClassificacao(dto.getClassificacao());

        if (dto.getEndereco() != null) {
            Endereco end = existente.getEndereco();
            if (end == null) {
                existente.setEndereco(dto.getEndereco());//endereço novo
            } else {
                //atualiza campos de endereço existente
                end.setRua(dto.getEndereco().getRua());
                end.setNumero(dto.getEndereco().getNumero());
                end.setBairro(dto.getEndereco().getBairro());
                end.setCidade(dto.getEndereco().getCidade());
                end.setEstado(dto.getEndereco().getEstado());
                end.setCep(dto.getEndereco().getCep());
            }
        }

        Estabelecimento atualizado = repositoryEstabelecimento.save(existente);
        return toDTO(atualizado);
    }

    public void deletar(Long id) {
        repositoryEstabelecimento.deleteById(id);
    }

    private EstabelecimentoDTO toDTO(Estabelecimento e) {
        return EstabelecimentoDTO.builder()
                .idEstabelecimento(e.getIdEstabelecimento())
                .nome(e.getNome())
                .email(e.getEmail())
                .categoria(e.getCategoria())
                .descricao(e.getDescricao())
                .telefone(e.getTelefone())
                .endereco(e.getEndereco())
                .classificacao(e.getClassificacao())
                .conveniencias(e.getConveniencias())
                .build();
    }
}

