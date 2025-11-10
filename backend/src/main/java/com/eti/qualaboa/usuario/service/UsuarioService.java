package com.eti.qualaboa.usuario.service;

import com.eti.qualaboa.estabelecimento.dto.EstabelecimentoDTO;
import com.eti.qualaboa.estabelecimento.model.Estabelecimento;
import com.eti.qualaboa.estabelecimento.service.EstabelecimentoService;
import com.eti.qualaboa.metricas.service.MetricasService;
import com.eti.qualaboa.usuario.domain.entity.Role;
import com.eti.qualaboa.usuario.domain.entity.Usuario;
import com.eti.qualaboa.usuario.dto.UsuarioRequestDTO;
import com.eti.qualaboa.usuario.dto.UsuarioResponseDTO;
import com.eti.qualaboa.usuario.dto.UsuarioUpdateRequestDTO;
import com.eti.qualaboa.usuario.dto.UsuarioUpdateResponseDTO;
import com.eti.qualaboa.usuario.repository.RoleRepository;
import com.eti.qualaboa.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
@ToString
public class UsuarioService {

    private final UsuarioRepository repository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final EstabelecimentoService estabelecimentoService;
    private final MetricasService metricasService;

    @Transactional
    public UsuarioResponseDTO criarUsuario(UsuarioRequestDTO requestDTO){

       if (repository.findByEmail(requestDTO.getEmail()).isPresent()) {
           throw new RuntimeException("Email já cadastrado");
       }

        Usuario user = new Usuario();
        user.setNome(requestDTO.getNome());
        user.setEmail(requestDTO.getEmail());
        user.setSenha(passwordEncoder.encode(requestDTO.getSenha()));
        user.setPreferenciasUsuario(requestDTO.getPreferenciasUsuario());

        if (requestDTO.getIdRole() == 2) {
            Role roleUser = roleRepository.findByNome("ADMIN").orElseThrow(() -> new RuntimeException("Role ADMIN não encontrada"));
            user.setRoles(Set.of(roleUser));
        } else if (requestDTO.getIdRole() == 1){
            Role roleUser = roleRepository.findByNome("USER").orElseThrow(() -> new RuntimeException("Role USER não encontrada"));
            user.setRoles(Set.of(roleUser));
        }

        Usuario usuarioSalvo = repository.save(user);

        return new UsuarioResponseDTO(
                usuarioSalvo.getId(),
                usuarioSalvo.getNome(),
                usuarioSalvo.getEmail(),
                usuarioSalvo.getSexo(),
                usuarioSalvo.getPreferenciasUsuario()
        );
    }

    public Usuario findUserById(Long userID){
        return  repository.findById(userID).orElseThrow(() -> new RuntimeException("Usuario não encontrado"));
    }

    @Transactional
    public UsuarioUpdateResponseDTO atualizarUsuario(Long userID, UsuarioUpdateRequestDTO requestDTO){
        Usuario user = findUserById(userID);

        if (requestDTO.getNome() != null && !requestDTO.getNome().isBlank()){
            user.setNome(requestDTO.getNome());
        } else {
            throw new RuntimeException("Preencha o campo nome");
        }

        if (requestDTO.getPreferenciasUsuario() != null) {
            user.setPreferenciasUsuario(requestDTO.getPreferenciasUsuario());
        }

        Usuario userAtualizado = repository.save(user);

        return new UsuarioUpdateResponseDTO(userAtualizado);

    }

    @Transactional
    public HttpStatus favoritarEstabelecimento( Long userID, Long estabelecimentoID){
        Usuario user = findUserById(userID);
        Estabelecimento estabelecimento = estabelecimentoService.buscarPorId(estabelecimentoID);
        boolean addFavorito = user.getFavoritos().add(estabelecimento);

        if (addFavorito) {
            metricasService.registrarFavorito(userID);
        }

        repository.save(user);
        return HttpStatus.NO_CONTENT;
    }

    @Transactional(readOnly = true)
    public Set<Estabelecimento> buscarFavoritos(Long usuarioId) {
        Optional<Usuario> usuarioComFavoritos = repository.findUserFaviritosById(usuarioId);

        if (usuarioComFavoritos.isEmpty()) {
            throw new RuntimeException("Usuário não encontrado!");
        }

        return usuarioComFavoritos.get().getFavoritos();
    }

    @Transactional
    public HttpStatus excluirFavoritos(Long userID, Long estabelecimentoID){
        Usuario user = findUserById(userID);
        Estabelecimento estabelecimento = estabelecimentoService.buscarPorId(estabelecimentoID);
        user.getFavoritos().remove(estabelecimento);
        repository.save(user);
        return HttpStatus.NO_CONTENT;
    }

    @Transactional
    public HttpStatus deletarUsuario(Long userID){
        Usuario user = findUserById(userID);
        repository.delete(user);
        return HttpStatus.NO_CONTENT;
    }
}
