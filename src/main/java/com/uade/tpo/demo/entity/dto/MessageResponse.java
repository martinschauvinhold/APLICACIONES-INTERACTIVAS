package com.uade.tpo.demo.entity.dto;

import java.util.Date;

import com.uade.tpo.demo.entity.Message;

public record MessageResponse(
        Integer id,
        Integer ticketId,
        Integer senderId,
        String senderUsername,
        String content,
        Date sentAt) {

    public static MessageResponse from(Message message) {
        var ticket = message.getTicket();
        var sender = message.getSender();
        return new MessageResponse(
                message.getId(),
                ticket != null ? ticket.getId() : null,
                sender != null ? sender.getId() : null,
                sender != null ? sender.getUsername() : null,
                message.getContent(),
                message.getSentAt());
    }
}
