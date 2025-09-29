package com.example.online_quiz_system.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class JwtResponseDTO {
    private final String accessToken;
    private final String refreshToken;
    private final String type = "Bearer";
    private final Long id;
    private final String email;
    private final List<String> roles;
    private final boolean verified;

    public JwtResponseDTO(String accessToken, String refreshToken, Long id,
                          String email, List<String> roles, boolean verified) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.id = id;
        this.email = email;
        this.roles = roles;
        this.verified = verified;
    }
}
