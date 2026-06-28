package com.uade.tpo.demo.entity.dto;

import java.util.Date;

import com.uade.tpo.demo.entity.Role;
import com.uade.tpo.demo.entity.User;

public record UserResponse(
        Integer id,
        String username,
        String email,
        String firstName,
        String lastName,
        Role role,
        String phone,
        Date createdAt) {

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole(),
                user.getPhone(),
                user.getCreatedAt());
    }
}
