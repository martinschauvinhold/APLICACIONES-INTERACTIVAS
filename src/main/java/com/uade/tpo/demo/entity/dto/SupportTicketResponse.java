package com.uade.tpo.demo.entity.dto;

import java.util.Date;

import com.uade.tpo.demo.entity.SupportTicket;
import com.uade.tpo.demo.entity.TicketStatus;

public record SupportTicketResponse(
        Integer id,
        Integer userId,
        String username,
        String subject,
        TicketStatus status,
        Date createdAt) {

    public static SupportTicketResponse from(SupportTicket ticket) {
        var user = ticket.getUser();
        return new SupportTicketResponse(
                ticket.getId(),
                user != null ? user.getId() : null,
                user != null ? user.getUsername() : null,
                ticket.getSubject(),
                ticket.getStatus(),
                ticket.getCreatedAt());
    }
}
