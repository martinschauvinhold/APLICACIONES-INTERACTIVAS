package com.uade.tpo.demo.controllers;

import java.net.URI;
import java.util.List;

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
import com.uade.tpo.demo.entity.dto.MessageRequest;
import com.uade.tpo.demo.entity.dto.SupportTicketRequest;
import com.uade.tpo.demo.entity.dto.TicketStatusRequest;
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

    @GetMapping
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<List<SupportTicket>> getAll() {
        return ResponseEntity.ok(ticketService.getAll());
    }

    @GetMapping("/{ticketId}")
    public ResponseEntity<SupportTicket> getById(@PathVariable Integer ticketId) {
        return ResponseEntity.ok(ticketService.getById(ticketId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('buyer', 'seller')")
    public ResponseEntity<SupportTicket> create(@Valid @RequestBody SupportTicketRequest request) {
        SupportTicket created = ticketService.create(request);
        return ResponseEntity.created(URI.create("/support/tickets/" + created.getId())).body(created);
    }

    @PutMapping("/{ticketId}/status")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<SupportTicket> updateStatus(@PathVariable Integer ticketId,
                                                       @Valid @RequestBody TicketStatusRequest request) {
        return ResponseEntity.ok(ticketService.updateStatus(ticketId, request.status()));
    }

    @GetMapping("/{ticketId}/messages")
    public ResponseEntity<List<Message>> getMessages(@PathVariable Integer ticketId) {
        return ResponseEntity.ok(messageService.getByTicketId(ticketId));
    }

    @PostMapping("/{ticketId}/messages")
    public ResponseEntity<Message> sendMessage(@PathVariable Integer ticketId,
                                                @Valid @RequestBody MessageRequest request) {
        Message sent = messageService.send(ticketId, request);
        return ResponseEntity.created(URI.create("/support/tickets/" + ticketId + "/messages/" + sent.getId()))
                .body(sent);
    }
}
