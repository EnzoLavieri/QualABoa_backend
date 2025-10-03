package com.eti.qualaboa.estabelecimento.service;

import com.eti.qualaboa.endereco.Endereco;
import com.eti.qualaboa.estabelecimento.dto.EstabelecimentoDTO;
import com.eti.qualaboa.estabelecimento.model.Estabelecimento;
import com.eti.qualaboa.estabelecimento.repository.EstabelecimentoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EstabelecimentoService {

    private final EstabelecimentoRepository repositoryEstabelecimento;

    public EstabelecimentoService(EstabelecimentoRepository repositoryEstabelecimento) {
        this.repositoryEstabelecimento = repositoryEstabelecimento;
    }

    public EstabelecimentoDTO criar(Estabelecimento estabelecimento) {
        if (repositoryEstabelecimento.existsByEmail(estabelecimento.getEmail())) {
            throw new RuntimeException("Email ja cadastrado");
        }
        Estabelecimento salvo = repositoryEstabelecimento.save(estabelecimento);
        return toDTO(salvo);
    }

    public List<EstabelecimentoDTO> listarTodos() {
        return repositoryEstabelecimento.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public EstabelecimentoDTO buscarPorId(Long id) {
        Estabelecimento est = repositoryEstabelecimento.findById(id)
                .orElseThrow(() -> new RuntimeException("Estabelecimento nao encontrado"));
        return toDTO(est);
    }

    public EstabelecimentoDTO atualizar(Long id, Estabelecimento dto) {
        Estabelecimento existente = repositoryEstabelecimento.findById(id)
                .orElseThrow(() -> new RuntimeException("Estabelecimento nao encontrado"));

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

