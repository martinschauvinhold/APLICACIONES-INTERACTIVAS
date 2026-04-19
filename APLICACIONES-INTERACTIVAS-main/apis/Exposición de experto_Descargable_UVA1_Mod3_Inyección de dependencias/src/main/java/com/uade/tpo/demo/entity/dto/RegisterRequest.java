package com.uade.tpo.demo.entity.dto;

public record RegisterRequest(
        String username,
        String email,
        String password,
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
