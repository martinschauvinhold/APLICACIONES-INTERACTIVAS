package com.uade.tpo.demo.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
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

    /**
     * Usuario de la sesión actual, o vacío si el request es anónimo. Pensado
     * para endpoints públicos que cambian su comportamiento si hay sesión (p.
     * ej. el listado de productos, donde un vendedor ve sus inactivos).
     */
    public Optional<User> currentUserOrEmpty() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }
        return userRepository.findByEmail(authentication.getName());
    }

    public boolean isAdmin() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_admin"));
    }

    public void requireSelfOrAdmin(int targetUserId) {
        if (isAdmin()) {
            return;
        }
        requireSelf(targetUserId);
    }

    /**
     * Exige que el usuario de la sesión sea exactamente el dueño del recurso.
     * A diferencia de {@link #requireSelfOrAdmin}, el admin NO está habilitado
     * (se usa para recursos cuya autoridad es del dueño, p. ej. los productos
     * de un vendedor).
     */
    public void requireSelf(int targetUserId) {
        if (currentUser().getId() != targetUserId) {
            throw new AccessDeniedException("No tenés permiso para operar sobre datos de otro usuario");
        }
    }
}
