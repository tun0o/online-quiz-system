package com.example.online_quiz_system.mapper;

import com.example.online_quiz_system.dto.UserAdminDTO;
import com.example.online_quiz_system.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    public UserAdminDTO toUserAdminDTO(User user);
}
