package com.uade.tpo.demo.entity.dto;

import lombok.Data;

@Data
public class AuthRequest {
    private String email;
    private String password;
}
