package com.uade.tpo.demo.entity.dto;

import lombok.Data;
import lombok.ToString;

@Data
public class AuthRequest {
    private String email;
    @ToString.Exclude
    private String password;
}
