package com.uade.tpo.demo.entity.dto;

import lombok.Data;

@Data
public class AddressRequest {
    private int userId;
    private String street;
    private String city;
    private String state;
    private String zipCode;
    private String referenceNote;
}
