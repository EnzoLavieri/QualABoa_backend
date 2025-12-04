package com.eti.qualaboa.comunidade.service;

import com.eti.qualaboa.comunidade.domain.entity.*;
import com.eti.qualaboa.comunidade.domain.enums.TipoPostagem;
import com.eti.qualaboa.comunidade.domain.enums.TipoReacao;
import com.eti.qualaboa.comunidade.dto.ComunidadeResponseDTO;
import com.eti.qualaboa.comunidade.dto.CriarPostagemDTO;
import com.eti.qualaboa.comunidade.dto.PostagemResponseDTO;
import com.eti.qualaboa.comunidade.repository.*;
import com.eti.qualaboa.estabelecimento.model.Estabelecimento;
import com.eti.qualaboa.estabelecimento.repository.EstabelecimentoRepository;
import com.eti.qualaboa.usuario.domain.entity.Usuario;
import com.eti.qualaboa.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ComunidadeService {

    private final ComunidadeRepository comunidadeRepository;
    private final PostagemRepository postagemRepository;
    private final UsuarioRepository usuarioRepository;
    private final EstabelecimentoRepository estabelecimentoRepository;
    private final OpcaoEnqueteRepository opcaoEnqueteRepository;
    private final ReacaoRepository reacaoRepository;

    @Transactional
    public Comunidade criarComunidade(Estabelecimento estabelecimento) {
        Optional<Comunidade> existente = comunidadeRepository.findByDonoIdEstabelecimento(estabelecimento.getIdEstabelecimento());
        if (existente.isPresent()) {
            return existente.get();
        }

        Comunidade com = new Comunidade();
        com.setNome("Comunidade " + estabelecimento.getNome());
        com.setDescricao("Fique por dentro das novidades de " + estabelecimento.getNome());
        com.setDono(estabelecimento);
        com.setCapaUrl(estabelecimento.getFotoUrl());
        return comunidadeRepository.save(com);
    }

    @Transactional
    public void entrarComunidadePorEstabelecimento(Long estabelecimentoId, Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Comunidade com = comunidadeRepository.findByDonoIdEstabelecimento(estabelecimentoId)
                .orElseGet(() -> {
                    Estabelecimento est = estabelecimentoRepository.findById(estabelecimentoId)
                            .orElseThrow(() -> new RuntimeException("Estabelecimento não encontrado"));
                    return criarComunidade(est);
                });

        if (!com.getMembros().contains(usuario)) {
            com.getMembros().add(usuario);
            comunidadeRepository.save(com);
        }
    }

    @Transactional(readOnly = true)
    public java.util.List<ComunidadeResponseDTO> listarMinhasComunidades(Long usuarioId) {
        if (!usuarioRepository.existsById(usuarioId)) {
            return java.util.Collections.emptyList();
        }

        return comunidadeRepository.findByMembrosId(usuarioId)
                .stream()
                .map(com.eti.qualaboa.comunidade.dto.ComunidadeResponseDTO::new)
                .toList();
    }

    @Transactional
    public PostagemResponseDTO criarPostagem(Long comunidadeId, Long estabelecimentoId, CriarPostagemDTO dto) {
        Comunidade com = comunidadeRepository.findById(comunidadeId)
                .orElseThrow(() -> new RuntimeException("Comunidade não encontrada"));

        Postagem post = new Postagem();
        post.setComunidade(com);
        post.setConteudoTexto(dto.conteudoTexto());
        post.setMediaUrl(dto.mediaUrl());
        post.setTipo(dto.tipo());
        post.setDataHora(LocalDateTime.now());

        post = postagemRepository.save(post);

        if (dto.tipo() == TipoPostagem.ENQUETE && dto.opcoesEnquete() != null) {
            for (String textoOpcao : dto.opcoesEnquete()) {
                OpcaoEnquete opcao = new OpcaoEnquete();
                opcao.setTextoOpcao(textoOpcao);
                opcao.setPostagem(post);
                opcaoEnqueteRepository.save(opcao);
            }
        }

        return new PostagemResponseDTO(post);
    }

    @Transactional(readOnly = true)
    public Page<PostagemResponseDTO> listarPostagens(Long comunidadeId, Pageable pageable) {
        return postagemRepository.findByComunidadeIdOrderByDataHoraDesc(comunidadeId, pageable)
                .map(PostagemResponseDTO::new);
    }

    @Transactional
    public void reagir(Long postagemId, Long usuarioId, TipoReacao tipo) {
        Postagem post = postagemRepository.findById(postagemId)
                .orElseThrow(() -> new RuntimeException("Postagem não encontrada"));
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Reacao reacao = new Reacao();
        reacao.setPostagem(post);
        reacao.setUsuario(usuario);
        reacao.setTipo(tipo);
        reacaoRepository.save(reacao);
    }

    @Transactional
    public void votarEnquete(Long opcaoId, Long usuarioId) {
        OpcaoEnquete opcao = opcaoEnqueteRepository.findById(opcaoId)
                .orElseThrow(() -> new RuntimeException("Opção não encontrada"));
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        opcao.getVotos().add(usuario);
        opcaoEnqueteRepository.save(opcao);
    }

    @Transactional
    public void entrarComunidade(Long comunidadeId, Long usuarioId) {
        Comunidade com = comunidadeRepository.findById(comunidadeId)
                .orElseThrow(() -> new RuntimeException("Comunidade não encontrada"));
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        com.getMembros().add(usuario);
        comunidadeRepository.save(com);
    }
}