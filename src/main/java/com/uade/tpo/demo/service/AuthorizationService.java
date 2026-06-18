package com.uade.tpo.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.uade.tpo.demo.entity.User;
import com.uade.tpo.demo.repository.UserRepository;

/**
 * Chequeos de "dueño del recurso o admin" reusados por los controllers de
 * direcciones, órdenes, pagos e items de orden. Lee la sesión desde
 * SecurityContextHolder en vez de requerir que cada controller reciba
 * Authentication como parámetro.
 */
@Service
public class AuthorizationService {

    @Autowired
    private UserRepository userRepository;

    public User currentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AccessDeniedException("Usuario autenticado no encontrado"));
    }

    public boolean isAdmin() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_admin"));
    }

    public void requireSelfOrAdmin(int targetUserId) {
        if (isAdmin()) {
            return;
        }
        if (currentUser().getId() != targetUserId) {
            throw new AccessDeniedException("No tenés permiso para operar sobre datos de otro usuario");
        }
    }
}
