package com.uade.tpo.demo.entity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class AddressRequest {
    @Positive
    private int userId;
    @NotBlank
    private String street;
    @NotBlank
    private String city;
    private String state;
    @NotBlank
    private String zipCode;
    private String referenceNote;
}
