package com.uade.tpo.demo.controllers;

import java.net.URI;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uade.tpo.demo.entity.Message;
import com.uade.tpo.demo.entity.SupportTicket;
import com.uade.tpo.demo.entity.TicketStatus;
import com.uade.tpo.demo.entity.dto.MessageRequest;
import com.uade.tpo.demo.entity.dto.MessageResponse;
import com.uade.tpo.demo.entity.dto.SupportTicketRequest;
import com.uade.tpo.demo.entity.dto.SupportTicketResponse;
import com.uade.tpo.demo.entity.dto.TicketStatusRequest;
import com.uade.tpo.demo.service.AuthorizationService;
import com.uade.tpo.demo.service.MessageService;
import com.uade.tpo.demo.service.SupportTicketService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("support/tickets")
@RequiredArgsConstructor
public class SupportTicketController {

    private final SupportTicketService ticketService;
    private final MessageService messageService;

    @Autowired
    private AuthorizationService authorizationService;

    @GetMapping
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<List<SupportTicketResponse>> getAll() {
        List<SupportTicketResponse> result = ticketService.getAll().stream()
                .map(SupportTicketResponse::from).toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{ticketId}")
    public ResponseEntity<SupportTicketResponse> getById(@PathVariable Integer ticketId) {
        SupportTicket ticket = ticketService.getById(ticketId);
        authorizationService.requireSelfOrAdmin(ticket.getUser().getId());
        return ResponseEntity.ok(SupportTicketResponse.from(ticket));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('buyer', 'seller')")
    public ResponseEntity<SupportTicketResponse> create(@Valid @RequestBody SupportTicketRequest request) {
        // El usuario y el estado inicial los decide el back, no el cliente:
        // el ticket siempre se crea a nombre de quien lo manda, en estado OPEN.
        SupportTicketRequest safeRequest = new SupportTicketRequest(
                authorizationService.currentUser().getId(), request.subject(), TicketStatus.OPEN);
        SupportTicket created = ticketService.create(safeRequest);
        return ResponseEntity.created(URI.create("/support/tickets/" + created.getId()))
                .body(SupportTicketResponse.from(created));
    }

    @PutMapping("/{ticketId}/status")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<SupportTicketResponse> updateStatus(@PathVariable Integer ticketId,
                                                       @Valid @RequestBody TicketStatusRequest request) {
        return ResponseEntity.ok(SupportTicketResponse.from(ticketService.updateStatus(ticketId, request.status())));
    }

    @GetMapping("/{ticketId}/messages")
    public ResponseEntity<List<MessageResponse>> getMessages(@PathVariable Integer ticketId) {
        SupportTicket ticket = ticketService.getById(ticketId);
        authorizationService.requireSelfOrAdmin(ticket.getUser().getId());
        List<MessageResponse> result = messageService.getByTicketId(ticketId).stream()
                .map(MessageResponse::from).toList();
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{ticketId}/messages")
    public ResponseEntity<MessageResponse> sendMessage(@PathVariable Integer ticketId,
                                                @Valid @RequestBody MessageRequest request) {
        SupportTicket ticket = ticketService.getById(ticketId);
        authorizationService.requireSelfOrAdmin(ticket.getUser().getId());
        // El remitente siempre es quien está autenticado, no lo que mande el body.
        MessageRequest safeRequest = new MessageRequest(authorizationService.currentUser().getId(), request.content());
        Message sent = messageService.send(ticketId, safeRequest);
        return ResponseEntity.created(URI.create("/support/tickets/" + ticketId + "/messages/" + sent.getId()))
                .body(MessageResponse.from(sent));
    }
}
