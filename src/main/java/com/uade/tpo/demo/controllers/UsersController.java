package com.uade.tpo.demo.controllers;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import com.uade.tpo.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uade.tpo.demo.entity.User;
import com.uade.tpo.demo.entity.dto.UserRequest;
import com.uade.tpo.demo.entity.dto.UserResponse;
import com.uade.tpo.demo.service.UserService;

@RestController
@RequestMapping("users")
public class UsersController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<List<UserResponse>> getUsers() {
        List<UserResponse> result = userService.getUsers().stream().map(UserResponse::from).toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<UserResponse> getUserById(@PathVariable int userId) {
        Optional<User> result = userService.getUserById(userId);
        return result.map(u -> ResponseEntity.ok(UserResponse.from(u))).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMe() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .map(u -> ResponseEntity.ok(UserResponse.from(u)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<Object> createUser(@Valid @RequestBody UserRequest userRequest) {
        User result = userService.createUser(userRequest);
        return ResponseEntity.created(URI.create("/users/" + result.getId())).body(UserResponse.from(result));
    }

    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<Object> updateUser(@PathVariable int userId, @Valid @RequestBody UserRequest userRequest) {
        Optional<User> result = userService.getUserById(userId);
        if (result.isPresent()) {
            User updated = userService.updateUser(userId, userRequest);
            return ResponseEntity.ok(UserResponse.from(updated));
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<Object> deleteUser(@PathVariable int userId) {
        Optional<User> result = userService.getUserById(userId);
        if (result.isPresent()) {
            userService.deleteUser(userId);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
