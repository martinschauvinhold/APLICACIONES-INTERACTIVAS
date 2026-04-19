package com.uade.tpo.demo.entity.dto;

public record RegisterRequest(
        String username,
        String email,
        String password,
        String firstName,
        String lastName,
        String phone
) {}
