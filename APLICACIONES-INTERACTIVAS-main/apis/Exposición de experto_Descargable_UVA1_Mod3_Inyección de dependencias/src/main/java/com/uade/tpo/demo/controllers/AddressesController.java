package com.uade.tpo.demo.controllers;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uade.tpo.demo.entity.Address;
import com.uade.tpo.demo.entity.dto.AddressRequest;
import com.uade.tpo.demo.service.AddressService;

@RestController
@RequestMapping("addresses")
public class AddressesController {

    @Autowired
    private AddressService addressService;

    @GetMapping
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<ArrayList<Address>> getAddresses() {
        return ResponseEntity.ok(addressService.getAddresses());
    }

    @GetMapping("/{addressId}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<Address> getAddressById(@PathVariable int addressId) {
        Optional<Address> result = addressService.getAddressById(addressId);
        return result.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<List<Address>> getAddressesByUser(@PathVariable int userId) {
        return ResponseEntity.ok(addressService.getAddressesByUser(userId));
    }

    @PostMapping
    public ResponseEntity<Object> createAddress(@RequestBody AddressRequest addressRequest) {
        Address result = addressService.createAddress(addressRequest);
        return ResponseEntity.created(URI.create("/addresses/" + result.getId())).body(result);
    }

    @PutMapping("/{addressId}")
    public ResponseEntity<Object> updateAddress(@PathVariable int addressId, @RequestBody AddressRequest addressRequest) {
        Optional<Address> result = addressService.getAddressById(addressId);
        if (result.isPresent()) {
            Address updated = addressService.updateAddress(addressId, addressRequest);
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<Object> deleteAddress(@PathVariable int addressId) {
        Optional<Address> result = addressService.getAddressById(addressId);
        if (result.isPresent()) {
            addressService.deleteAddress(addressId);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
