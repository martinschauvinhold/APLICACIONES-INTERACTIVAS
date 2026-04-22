package com.uade.tpo.demo.entity.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank String username,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8) String password,
        String firstName,
        String lastName,
        String phone
) {
    @Override
    public String toString() {
        return "RegisterRequest[username=" + username + ", email=" + email +
               ", firstName=" + firstName + ", lastName=" + lastName + ", phone=" + phone + "]";
    }
}
