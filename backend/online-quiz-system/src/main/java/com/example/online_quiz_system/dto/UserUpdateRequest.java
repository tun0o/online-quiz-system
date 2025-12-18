package com.example.online_quiz_system.dto;

import com.example.online_quiz_system.enums.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserUpdateRequest {
    private String name;
    private String grade;
    private String goal;

    @NotNull(message = "Vai trò không được để trống")
    private Role role;

    @NotNull(message = "Trạng thái kích hoạt không được để trống")
    private Boolean enabled;

    @NotNull(message = "Trạng thái xác thực không được để trống")
    private Boolean verified;
}
