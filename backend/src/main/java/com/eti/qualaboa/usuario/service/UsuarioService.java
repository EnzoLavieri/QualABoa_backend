package com.eti.qualaboa.usuario.service;

import com.eti.qualaboa.usuario.domain.entity.Usuario;
import com.eti.qualaboa.usuario.dto.UsuarioRequestDTO;
import com.eti.qualaboa.usuario.dto.UsuarioResponseDTO;
import com.eti.qualaboa.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@ToString
public class UsuarioService {

    private final UsuarioRepository repository;

    @Transactional
    public UsuarioResponseDTO criarUsuario(UsuarioRequestDTO requestDTO){
        Usuario user = new Usuario();
        user.setNome(requestDTO.getNome());
        user.setEmail(requestDTO.getEmail());
        user.setSenha(requestDTO.getSenha());
        user.setSexo(requestDTO.getSexo());
        user.setPreferenciasUsuario(requestDTO.getPreferenciasUsuario());
        log.info("Iniciando processo de criação de usuário para o DTO: {}", user);


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


}
