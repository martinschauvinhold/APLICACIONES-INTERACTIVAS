package com.uade.tpo.demo.entity.dto;

import com.uade.tpo.demo.entity.Address;

public record AddressResponse(
        Integer id,
        Integer userId,
        String street,
        String city,
        String state,
        String zipCode,
        String referenceNote) {

    public static AddressResponse from(Address address) {
        var user = address.getUser();
        return new AddressResponse(
                address.getId(),
                user != null ? user.getId() : null,
                address.getStreet(),
                address.getCity(),
                address.getState(),
                address.getZipCode(),
                address.getReferenceNote());
    }
}
