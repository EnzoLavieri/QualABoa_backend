package com.eti.qualaboa.config.dto;

public record LoginResponse(Long userID,String accessToken, Long expiresIn, String fotoUrl) {
}
