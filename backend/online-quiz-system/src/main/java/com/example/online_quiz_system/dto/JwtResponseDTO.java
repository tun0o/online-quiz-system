package com.example.online_quiz_system.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class JwtResponseDTO {
    private final String accessToken;
    private final String refreshToken;
    private final UserDTO user;

    public JwtResponseDTO(String accessToken, String refreshToken, UserDTO user) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.user = user;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class UserDTO {
        private final Long id;
        private final String email;
        private final String name;
        private final List<String> roles;
        private final boolean verified;
    }
}
