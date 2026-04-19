package com.uade.tpo.demo.controllers;

import java.net.URI;
import java.util.ArrayList;
import java.util.Optional;

import com.uade.tpo.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
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
    public ResponseEntity<ArrayList<User>> getUsers() {
        return ResponseEntity.ok(userService.getUsers());
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<User> getUserById(@PathVariable int userId) {
        Optional<User> result = userService.getUserById(userId);
        return result.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/me")
    public ResponseEntity<User> getMe() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<Object> createUser(@RequestBody UserRequest userRequest) {
        User result = userService.createUser(userRequest);
        return ResponseEntity.created(URI.create("/users/" + result.getId())).body(result);
    }

    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<Object> updateUser(@PathVariable int userId, @RequestBody UserRequest userRequest) {
        Optional<User> result = userService.getUserById(userId);
        if (result.isPresent()) {
            User updated = userService.updateUser(userId, userRequest);
            return ResponseEntity.ok(updated);
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
