package com.eti.qualaboa.comunidadetest.servicetest;

import com.eti.qualaboa.comunidade.domain.entity.Comunidade;
import com.eti.qualaboa.comunidade.domain.entity.OpcaoEnquete;
import com.eti.qualaboa.comunidade.domain.entity.Postagem;
import com.eti.qualaboa.comunidade.domain.entity.Reacao;
import com.eti.qualaboa.comunidade.domain.enums.TipoPostagem;
import com.eti.qualaboa.comunidade.domain.enums.TipoReacao;
import com.eti.qualaboa.comunidade.dto.ComunidadeResponseDTO;
import com.eti.qualaboa.comunidade.dto.CriarPostagemDTO;
import com.eti.qualaboa.comunidade.dto.PostagemResponseDTO;
import com.eti.qualaboa.comunidade.repository.ComunidadeRepository;
import com.eti.qualaboa.comunidade.repository.OpcaoEnqueteRepository;
import com.eti.qualaboa.comunidade.repository.PostagemRepository;
import com.eti.qualaboa.comunidade.repository.ReacaoRepository;
import com.eti.qualaboa.comunidade.service.ComunidadeService;
import com.eti.qualaboa.estabelecimento.model.Estabelecimento;
import com.eti.qualaboa.estabelecimento.repository.EstabelecimentoRepository;
import com.eti.qualaboa.usuario.domain.entity.Usuario;
import com.eti.qualaboa.usuario.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ComunidadeServiceTest {

    @InjectMocks
    private ComunidadeService comunidadeService;

    @Mock
    private ComunidadeRepository comunidadeRepository;

    @Mock
    private PostagemRepository postagemRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private EstabelecimentoRepository estabelecimentoRepository;

    @Mock
    private OpcaoEnqueteRepository opcaoEnqueteRepository;

    @Mock
    private ReacaoRepository reacaoRepository;

    @Test
    @DisplayName("Deve criar nova comunidade quando não existir")
    void criarComunidade_Nova() {
        Estabelecimento estabelecimento = new Estabelecimento();
        estabelecimento.setIdEstabelecimento(1L);
        estabelecimento.setNome("Bar Teste");
        estabelecimento.setFotoUrl("url_foto");

        when(comunidadeRepository.findByDonoIdEstabelecimento(1L)).thenReturn(Optional.empty());
        when(comunidadeRepository.save(any(Comunidade.class))).thenAnswer(i -> i.getArguments()[0]);

        Comunidade result = comunidadeService.criarComunidade(estabelecimento);

        assertNotNull(result);
        assertEquals("Comunidade Bar Teste", result.getNome());
        assertEquals(estabelecimento, result.getDono());
        verify(comunidadeRepository).save(any(Comunidade.class));
    }

    @Test
    @DisplayName("Deve retornar comunidade existente se já houver uma para o estabelecimento")
    void criarComunidade_Existente() {
        Estabelecimento estabelecimento = new Estabelecimento();
        estabelecimento.setIdEstabelecimento(1L);

        Comunidade existente = new Comunidade();
        existente.setId(10L);
        existente.setDono(estabelecimento);

        when(comunidadeRepository.findByDonoIdEstabelecimento(1L)).thenReturn(Optional.of(existente));

        Comunidade result = comunidadeService.criarComunidade(estabelecimento);

        assertEquals(10L, result.getId());
        verify(comunidadeRepository, never()).save(any(Comunidade.class));
    }

    @Test
    @DisplayName("Deve entrar na comunidade com sucesso")
    void entrarComunidade() {
        Long comunidadeId = 1L;
        Long usuarioId = 100L;

        Comunidade comunidade = new Comunidade();
        comunidade.setMembros(new HashSet<>());

        Usuario usuario = new Usuario();
        usuario.setId(usuarioId);

        when(comunidadeRepository.findById(comunidadeId)).thenReturn(Optional.of(comunidade));
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));

        comunidadeService.entrarComunidade(comunidadeId, usuarioId);

        assertTrue(comunidade.getMembros().contains(usuario));
        verify(comunidadeRepository).save(comunidade);
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar criar postagem em comunidade inexistente")
    void criarPostagem_ComunidadeNaoEncontrada() {
        CriarPostagemDTO dto = new CriarPostagemDTO("Texto", null, TipoPostagem.TEXTO, null);

        when(comunidadeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                comunidadeService.criarPostagem(1L, 1L, dto)
        );
    }

    @Test
    @DisplayName("Deve lançar exceção quando estabelecimento não for o dono da comunidade")
    void criarPostagem_NaoDono() {
        Estabelecimento dono = new Estabelecimento();
        dono.setIdEstabelecimento(10L);

        Comunidade comunidade = new Comunidade();
        comunidade.setDono(dono);

        CriarPostagemDTO dto = new CriarPostagemDTO("Texto", null, TipoPostagem.TEXTO, null);

        when(comunidadeRepository.findById(1L)).thenReturn(Optional.of(comunidade));

        // Tenta postar com ID 99L, mas o dono é 10L
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                comunidadeService.criarPostagem(1L, 99L, dto)
        );
        assertEquals("Acesso negado: Apenas o dono pode postar.", ex.getMessage());
    }

    @Test
    @DisplayName("Deve criar postagem com sucesso")
    void criarPostagem_Sucesso() {
        Estabelecimento dono = new Estabelecimento();
        dono.setIdEstabelecimento(10L);

        Comunidade comunidade = new Comunidade();
        comunidade.setDono(dono);

        CriarPostagemDTO dto = new CriarPostagemDTO("Ola mundo", "img.jpg", TipoPostagem.TEXTO, null);

        when(comunidadeRepository.findById(1L)).thenReturn(Optional.of(comunidade));
        when(postagemRepository.save(any(Postagem.class))).thenAnswer(i -> {
            Postagem p = (Postagem) i.getArguments()[0];
            p.setId(55L);
            return p;
        });

        PostagemResponseDTO response = comunidadeService.criarPostagem(1L, 10L, dto);

        assertNotNull(response);
        assertEquals("Ola mundo", response.conteudoTexto());
        verify(postagemRepository).save(any(Postagem.class));
    }

    @Test
    @DisplayName("Deve listar postagens paginadas")
    void listarPostagens() {
        Long comunidadeId = 1L;
        Pageable pageable = Pageable.unpaged();

        Postagem post1 = new Postagem();
        post1.setId(10L);
        Postagem post2 = new Postagem();
        post2.setId(11L);

        Page<Postagem> page = new PageImpl<>(List.of(post1, post2));

        when(postagemRepository.findByComunidadeIdOrderByDataHoraDesc(comunidadeId, pageable))
                .thenReturn(page);

        Page<PostagemResponseDTO> result = comunidadeService.listarPostagens(comunidadeId, pageable);

        assertEquals(2, result.getTotalElements());
        assertEquals(10L, result.getContent().get(0).id());
    }

    @Test
    @DisplayName("Deve registrar reação em postagem")
    void reagir() {
        Long postId = 10L;
        Long userId = 5L;

        Postagem post = new Postagem();
        Usuario user = new Usuario();

        when(postagemRepository.findById(postId)).thenReturn(Optional.of(post));
        when(usuarioRepository.findById(userId)).thenReturn(Optional.of(user));

        comunidadeService.reagir(postId, userId, TipoReacao.CURTIR);

        verify(reacaoRepository).save(any(Reacao.class));
    }

    @Test
    @DisplayName("Deve votar em enquete")
    void votarEnquete() {
        Long opcaoId = 1L;
        Long userId = 2L;

        OpcaoEnquete opcao = new OpcaoEnquete();
        opcao.setVotos(new HashSet<>());

        Usuario user = new Usuario();

        when(opcaoEnqueteRepository.findById(opcaoId)).thenReturn(Optional.of(opcao));
        when(usuarioRepository.findById(userId)).thenReturn(Optional.of(user));

        comunidadeService.votarEnquete(opcaoId, userId);

        assertTrue(opcao.getVotos().contains(user));
        verify(opcaoEnqueteRepository).save(opcao);
    }

    @Test
    @DisplayName("Deve listar comunidades do usuário")
    void listarMinhasComunidades() {
        Long userId = 1L;
        Comunidade c1 = new Comunidade();
        c1.setId(10L);
        c1.setNome("C1");
        c1.setDono(new Estabelecimento()); // Evita NPE no DTO

        when(usuarioRepository.existsById(userId)).thenReturn(true);
        when(comunidadeRepository.findByMembrosId(userId)).thenReturn(List.of(c1));

        List<ComunidadeResponseDTO> result = comunidadeService.listarMinhasComunidades(userId);

        assertEquals(1, result.size());
        assertEquals("C1", result.get(0).nome());
    }
}