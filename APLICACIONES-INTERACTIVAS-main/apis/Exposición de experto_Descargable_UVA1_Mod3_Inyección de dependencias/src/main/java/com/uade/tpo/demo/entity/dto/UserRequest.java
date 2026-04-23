package com.uade.tpo.demo.entity.dto;

import com.uade.tpo.demo.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRequest {
    @NotBlank private String username;
    @NotBlank @Email private String email;
    @NotBlank @Size(min = 8) private String password;
    private String firstName;
    private String lastName;
    @NotNull private Role role;
    private String phone;
}
