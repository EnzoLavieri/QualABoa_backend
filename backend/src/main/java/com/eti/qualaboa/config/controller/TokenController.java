package com.eti.qualaboa.config.controller;

import com.eti.qualaboa.config.dto.LoginRequest;
import com.eti.qualaboa.config.dto.LoginResponse;
import com.eti.qualaboa.estabelecimento.repository.EstabelecimentoRepository;
import com.eti.qualaboa.usuario.domain.entity.Role;
import com.eti.qualaboa.usuario.repository.UsuarioRepository;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class TokenController {

    private final JwtEncoder jwtEncoder;
    private final UsuarioRepository usuarioRepository;
    private final EstabelecimentoRepository estabelecimentoRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {

        var user = usuarioRepository.findByEmail(loginRequest.email());

        if (user.isEmpty() || !user.get().isLoginCorrect(loginRequest, bCryptPasswordEncoder)) {
            throw new BadCredentialsException("email ou senha inválidos");
        }

        var expiresIn = 84000L;
        var scope = user.get().getRoles().stream().map(Role::getNome).collect(Collectors.joining(" "));

        var claims = JwtClaimsSet.builder()
                .issuer("qualaboa-backend")
                .subject(user.get().getId().toString())
                .expiresAt(Instant.now().plusSeconds(expiresIn)) // 5 minutos
                .issuedAt(Instant.now())
                .claim("scope", scope)
                .build();

        var jwtValue = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
        return  ResponseEntity.ok(new LoginResponse(user.get().getId(),
                jwtValue,
                expiresIn,
                user.get().getFotoUrl()
        ));
    }

    @PostMapping("/login/estabelecimento")
    public ResponseEntity<LoginResponse> loginEstabelecimento(@RequestBody LoginRequest loginRequest) {

        var estabelecimento = estabelecimentoRepository.findByEmail(loginRequest.email());

        if (estabelecimento.isEmpty() || !estabelecimento.get().isLoginCorrect(loginRequest, bCryptPasswordEncoder)) {
            throw new BadCredentialsException("email ou senha inválidos");
        }

        var expiresIn = 86400L;
        var scope = estabelecimento.get().getRoles().stream().map(Role::getNome).collect(Collectors.joining(" "));

        var claims = JwtClaimsSet.builder()
                .issuer("qualaboa-backend")
                .subject(estabelecimento.get().getId().toString())
                .expiresAt(Instant.now().plusSeconds(expiresIn)) // 5 minutos
                .issuedAt(Instant.now())
                .claim("scope", scope)
                .build();

        var jwtValue = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
        return  ResponseEntity.ok(new LoginResponse(estabelecimento.get().getIdEstabelecimento(),
                jwtValue,
                expiresIn,
                estabelecimento.get().getFotoUrl()
        ));
    }
}
