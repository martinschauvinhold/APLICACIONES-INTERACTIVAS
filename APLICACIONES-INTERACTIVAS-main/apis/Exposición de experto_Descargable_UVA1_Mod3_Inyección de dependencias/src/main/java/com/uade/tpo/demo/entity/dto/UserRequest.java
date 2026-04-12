package com.uade.tpo.demo.entity.dto;

import com.uade.tpo.demo.entity.Role;
import lombok.Data;

@Data
public class UserRequest {
    private String username;
    private String email;
    private String passwordHash;
    private String firstName;
    private String lastName;
    private Role role;
    private String phone;
}
