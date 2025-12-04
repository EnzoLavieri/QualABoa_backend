package com.eti.qualaboa.comunidadetest.controllertest;

import com.eti.qualaboa.comunidade.controller.ComunidadeController;
import com.eti.qualaboa.comunidade.domain.entity.Comunidade;
import com.eti.qualaboa.comunidade.domain.entity.Postagem;
import com.eti.qualaboa.comunidade.domain.enums.TipoPostagem;
import com.eti.qualaboa.comunidade.domain.enums.TipoReacao;
import com.eti.qualaboa.comunidade.dto.CriarPostagemDTO;
import com.eti.qualaboa.comunidade.dto.PostagemResponseDTO;
import com.eti.qualaboa.comunidade.repository.ComunidadeRepository;
import com.eti.qualaboa.comunidade.service.ComunidadeService;
import com.eti.qualaboa.estabelecimento.model.Estabelecimento;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ComunidadeController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ComunidadeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ComunidadeService comunidadeService;

    @MockBean
    private ComunidadeRepository comunidadeRepository;

    @Test
    @DisplayName("GET /api/comunidades/estabelecimento/{estabId} - Deve retornar comunidade quando encontrada")
    void buscarPorEstabelecimento_ComSucesso() throws Exception {
        Long estabId;
        estabId = 1L;

        // Configura o Estabelecimento dono
        Comunidade comunidade = getComunidade(estabId);

        // Define o comportamento do Mock
        when(comunidadeRepository.findByDonoIdEstabelecimento(eq(estabId)))
                .thenReturn(Optional.of(comunidade));

        // Executa a requisição
        mockMvc.perform(get("/api/comunidades/estabelecimento/{estabId}", estabId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Comunidade Teste"));
    }

    private static @NotNull Comunidade getComunidade(Long estabId) {
        Estabelecimento dono = new Estabelecimento();
        dono.setIdEstabelecimento(estabId);

        // Configura a Comunidade Mock
        Comunidade comunidade = new Comunidade();
        comunidade.setId(1L);
        comunidade.setNome("Comunidade Teste");
        comunidade.setDescricao("Descrição da comunidade");
        comunidade.setCapaUrl("http://foto.com/capa.jpg");
        comunidade.setDono(dono);
        comunidade.setMembros(new HashSet<>()); // Inicializa lista vazia para evitar NullPointer no DTO
        return comunidade;
    }

    @Test
    @DisplayName("GET /api/comunidades/estabelecimento/{estabId} - Deve retornar 404 quando não encontrada")
    void buscarPorEstabelecimento_NaoEncontrado() throws Exception {
        Long estabId = 999L;

        when(comunidadeRepository.findByDonoIdEstabelecimento(estabId))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/comunidades/estabelecimento/{estabId}", estabId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/comunidades/{id}/entrar - Deve entrar na comunidade com sucesso")
    void entrarNaComunidade_DeveRetornar200() throws Exception {
        Long comunidadeId = 1L;
        Long usuarioId = 10L;

        doNothing().when(comunidadeService).entrarComunidade(comunidadeId, usuarioId);

        mockMvc.perform(post("/api/comunidades/{id}/entrar", comunidadeId)
                        .param("usuarioId", String.valueOf(usuarioId)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/comunidades/entrar/estabelecimento/{estabId} - Deve entrar por estabelecimento")
    void entrarPorEstabelecimento_DeveRetornar200() throws Exception {
        Long estabId = 100L;
        Long usuarioId = 10L;

        doNothing().when(comunidadeService).entrarComunidadePorEstabelecimento(estabId, usuarioId);

        mockMvc.perform(post("/api/comunidades/entrar/estabelecimento/{estabId}", estabId)
                        .param("usuarioId", String.valueOf(usuarioId)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/comunidades/{id}/postagens - Deve criar postagem com sucesso")
    void criarPostagem_DeveRetornar200() throws Exception {
        Long comunidadeId = 1L;
        Long estabelecimentoId = 100L;

        CriarPostagemDTO requestDTO = new CriarPostagemDTO(
                "Conteúdo do post",
                "http://imagem.com/foto.jpg",
                TipoPostagem.TEXTO,
                null
        );

        Postagem postagemMock = new Postagem();
        postagemMock.setId(10L);
        PostagemResponseDTO responseDTO = new PostagemResponseDTO(postagemMock);

        when(comunidadeService.criarPostagem(eq(comunidadeId), eq(estabelecimentoId), any(CriarPostagemDTO.class)))
                .thenReturn(responseDTO);

        mockMvc.perform(post("/api/comunidades/{id}/postagens", comunidadeId)
                        .param("estabelecimentoId", String.valueOf(estabelecimentoId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/comunidades/{id}/postagens - Deve listar postagens paginadas")
    void listarPostagens_DeveRetornar200() throws Exception {
        Long comunidadeId = 1L;
        Page<PostagemResponseDTO> page = new PageImpl<>(Collections.emptyList());

        when(comunidadeService.listarPostagens(eq(comunidadeId), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/comunidades/{id}/postagens", comunidadeId)
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/comunidades/postagens/{postId}/reagir - Deve reagir a uma postagem")
    void reagirPostagem_DeveRetornar200() throws Exception {
        Long postId = 50L;
        Long usuarioId = 10L;
        TipoReacao tipo = TipoReacao.CURTIR;

        doNothing().when(comunidadeService).reagir(postId, usuarioId, tipo);

        mockMvc.perform(post("/api/comunidades/postagens/{postId}/reagir", postId)
                        .param("usuarioId", String.valueOf(usuarioId))
                        .param("tipo", tipo.name()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/comunidades/enquete/{opcaoId}/votar - Deve votar em enquete")
    void votarEnquete_DeveRetornar200() throws Exception {
        Long opcaoId = 5L;
        Long usuarioId = 10L;

        doNothing().when(comunidadeService).votarEnquete(opcaoId, usuarioId);

        mockMvc.perform(post("/api/comunidades/enquete/{opcaoId}/votar", opcaoId)
                        .param("usuarioId", String.valueOf(usuarioId)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/comunidades/usuario/{usuarioId} - Deve listar comunidades do usuário")
    void listarMinhasComunidades_DeveRetornar200() throws Exception {
        Long usuarioId = 10L;

        when(comunidadeService.listarMinhasComunidades(usuarioId))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/comunidades/usuario/{usuarioId}", usuarioId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}