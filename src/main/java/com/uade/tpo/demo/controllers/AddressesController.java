package com.uade.tpo.demo.controllers;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uade.tpo.demo.entity.Address;
import com.uade.tpo.demo.entity.User;
import com.uade.tpo.demo.entity.dto.AddressRequest;
import com.uade.tpo.demo.repository.UserRepository;
import com.uade.tpo.demo.service.AddressService;

@RestController
@RequestMapping("addresses")
public class AddressesController {

    @Autowired
    private AddressService addressService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<ArrayList<Address>> getAddresses() {
        return ResponseEntity.ok(addressService.getAddresses());
    }

    @GetMapping("/{addressId}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<Address> getAddressById(@PathVariable int addressId) {
        Optional<Address> result = addressService.getAddressById(addressId);
        return result.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<List<Address>> getAddressesByUser(@PathVariable int userId) {
        return ResponseEntity.ok(addressService.getAddressesByUser(userId));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Object> createAddress(@Validated @RequestBody AddressRequest addressRequest,
                                                Authentication auth) {
        requireSelfOrAdmin(addressRequest.getUserId(), auth);
        Address result = addressService.createAddress(addressRequest);
        return ResponseEntity.created(URI.create("/addresses/" + result.getId())).body(result);
    }

    @PutMapping("/{addressId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Object> updateAddress(@PathVariable int addressId,
                                                @RequestBody AddressRequest addressRequest,
                                                Authentication auth) {
        Optional<Address> existing = addressService.getAddressById(addressId);
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        requireSelfOrAdmin(existing.get().getUser().getId(), auth);
        Address updated = addressService.updateAddress(addressId, addressRequest);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{addressId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Object> deleteAddress(@PathVariable int addressId, Authentication auth) {
        Optional<Address> existing = addressService.getAddressById(addressId);
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        requireSelfOrAdmin(existing.get().getUser().getId(), auth);
        addressService.deleteAddress(addressId);
        return ResponseEntity.noContent().build();
    }

    private void requireSelfOrAdmin(int targetUserId, Authentication auth) {
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_admin"));
        if (isAdmin) {
            return;
        }
        User current = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new AccessDeniedException("Usuario autenticado no encontrado"));
        if (current.getId() != targetUserId) {
            throw new AccessDeniedException("No tenés permiso para operar sobre direcciones de otro usuario");
        }
    }
}
