package com.eti.qualaboa.estabelecimento.service;

import com.eti.qualaboa.endereco.Endereco;
import com.eti.qualaboa.estabelecimento.dto.EstabelecimentoDTO;
import com.eti.qualaboa.estabelecimento.dto.EstabelecimentoRegisterDTO;
import com.eti.qualaboa.estabelecimento.dto.EstabelecimentoResponseDTO;
import com.eti.qualaboa.estabelecimento.model.Estabelecimento;
import com.eti.qualaboa.estabelecimento.repository.EstabelecimentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import com.eti.qualaboa.usuario.domain.entity.Role;
import com.eti.qualaboa.usuario.repository.RoleRepository;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.eti.qualaboa.map.places.PlacesClient;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class EstabelecimentoService {

    private final EstabelecimentoRepository repositoryEstabelecimento;
    private final PlacesClient placesClient;
    private final JdbcTemplate jdbcTemplate;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public EstabelecimentoService(EstabelecimentoRepository repositoryEstabelecimento, RoleRepository roleRepository,
            BCryptPasswordEncoder passwordEncoder) {
        this.repositoryEstabelecimento = repositoryEstabelecimento;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Cria um novo estabelecimento parceiro.
     * Garante que o endereço não seja duplicado (idEndereco é removido antes do save).
     */
    public EstabelecimentoDTO criar(Estabelecimento estabelecimento) {
        if (repositoryEstabelecimento.existsByEmail(estabelecimento.getEmail())) {
            throw new RuntimeException("E-mail já cadastrado");
        }

    // Evita conflito de chave duplicada no endereço
    // if (estabelecimento.getEndereco() != null) {
    // estabelecimento.getEndereco().setIdEndereco(null);
    // }

    // Estabelecimento salvo = repositoryEstabelecimento.save(estabelecimento);
    // jdbcTemplate.execute("""
    // ALTER TABLE estabelecimentos ADD COLUMN IF NOT EXISTS geom
    // geography(Point,4326);
    // """);

    // if (salvo.getLatitude() != null && salvo.getLongitude() != null) {
    // jdbcTemplate.update("""
    // UPDATE estabelecimentos
    // SET geom = ST_SetSRID(ST_MakePoint(?, ?), 4326)
    // WHERE id_estabelecimento = ?
    // """, salvo.getLongitude(), salvo.getLatitude(),
    // salvo.getIdEstabelecimento());
    // }
    // return toDTO(salvo);

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

        if (estabelecimentoRequest.getIdRole() == 3) {
            Role role = roleRepository.findByNome("ESTABELECIMENTO")
                    .orElseThrow(() -> new RuntimeException("Role ESTABELECIMENTO não encontrada"));
            estabelecimento.setRoles(Set.of(role));
        }

        Estabelecimento salvo = repositoryEstabelecimento.save(estabelecimento);

        jdbcTemplate.execute("""
                    ALTER TABLE estabelecimentos ADD COLUMN IF NOT EXISTS geom geography(Point,4326);
                """);

        if (salvo.getLatitude() != null && salvo.getLongitude() != null) {
            jdbcTemplate.update("""
                        UPDATE estabelecimentos
                        SET geom = ST_SetSRID(ST_MakePoint(?, ?), 4326)
                        WHERE id_estabelecimento = ?
                    """, salvo.getLongitude(), salvo.getLatitude(), salvo.getIdEstabelecimento());
        }

        return new EstabelecimentoResponseDTO(
                salvo.getIdEstabelecimento(),
                salvo.getNome(),
                salvo.getEmail(),
                salvo.getCategoria(),
                salvo.getDescricao(),
                salvo.getTelefone(),
                salvo.getParceiro(),
                salvo.getPlaceId(),
                salvo.getLatitude(),
                salvo.getLongitude(),
                salvo.getEnderecoFormatado(),
                salvo.getEndereco(),
                salvo.getClassificacao(),
                salvo.getConveniencias());
    }

    /**
     * Lista todos os estabelecimentos cadastrados (parceiros).
     */
    public List<EstabelecimentoDTO> listarTodos() {
        return repositoryEstabelecimento.findAll()
                .stream().map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Busca um estabelecimento pelo ID.
     */
    public Estabelecimento buscarPorId(Long id) {
        return repositoryEstabelecimento.findById(id)
                .orElseThrow(() -> new RuntimeException("Estabelecimento não encontrado"));

    }

    /**
     * Atualiza as informações de um estabelecimento existente.
     */
    public EstabelecimentoDTO atualizar(Long id, Estabelecimento dto) {
        Estabelecimento existente = repositoryEstabelecimento.findById(id)
                .orElseThrow(() -> new RuntimeException("Estabelecimento não encontrado"));

        existente.setNome(dto.getNome());
        existente.setCategoria(dto.getCategoria());
        existente.setDescricao(dto.getDescricao());
        existente.setTelefone(dto.getTelefone());
        existente.setConveniencias(dto.getConveniencias());
        existente.setClassificacao(dto.getClassificacao());
        existente.setLatitude(dto.getLatitude());
        existente.setLongitude(dto.getLongitude());
        existente.setParceiro(dto.getParceiro());
        existente.setPlaceId(dto.getPlaceId());
        existente.setEnderecoFormatado(dto.getEnderecoFormatado());

        // Atualiza ou cria o endereço
        if (dto.getEndereco() != null) {
            Endereco end = existente.getEndereco();
            if (end == null) {
                existente.setEndereco(dto.getEndereco());
            } else {
                end.setRua(dto.getEndereco().getRua());
                end.setNumero(dto.getEndereco().getNumero());
                end.setBairro(dto.getEndereco().getBairro());
                end.setCidade(dto.getEndereco().getCidade());
                end.setEstado(dto.getEndereco().getEstado());
                end.setCep(dto.getEndereco().getCep());
                end.setLatitude(dto.getEndereco().getLatitude());
                end.setLongitude(dto.getEndereco().getLongitude());
            }
        }

        Estabelecimento atualizado = repositoryEstabelecimento.save(existente);
        return toDTO(atualizado);
    }

    /**
     * Exclui um estabelecimento pelo ID.
     */
    public void deletar(Long id) {
        repositoryEstabelecimento.deleteById(id);
    }

    public EstabelecimentoDTO vincularComPlace(Long id, String placeId) {
        Estabelecimento est = repositoryEstabelecimento.findById(id)
                .orElseThrow(() -> new RuntimeException("Estabelecimento não encontrado"));

        Map<String, Object> details = placesClient.placeDetails(placeId);
        Map<String, Object> result = (Map<String, Object>) details.get("result");

        est.setPlaceId(placeId);
        est.setNome((String) result.get("name"));
        est.setEnderecoFormatado((String) result.get("formatted_address"));

        if (result.get("geometry") != null) {
            Map<String, Object> geometry = (Map<String, Object>) result.get("geometry");
            Map<String, Object> location = (Map<String, Object>) geometry.get("location");
            est.setLatitude(((Number) location.get("lat")).doubleValue());
            est.setLongitude(((Number) location.get("lng")).doubleValue());
        }

        est.setParceiro(true);
        repositoryEstabelecimento.save(est);
        return toDTO(est);
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
                .parceiro(e.getParceiro())
                .placeId(e.getPlaceId())
                .latitude(e.getLatitude())
                .longitude(e.getLongitude())
                .enderecoFormatado(e.getEnderecoFormatado())
                .build();
    }
}
