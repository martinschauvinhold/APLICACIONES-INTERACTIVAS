package com.uade.tpo.demo.entity.dto;

import lombok.Data;

@Data
public class UserRequest {
    private String username;
    private String email;
    private String passwordHash;
    private String firstName;
    private String lastName;
    private String role;
    private String phone;
}
