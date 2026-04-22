package com.uade.tpo.demo.service;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uade.tpo.demo.entity.Message;
import com.uade.tpo.demo.entity.TicketStatus;
import com.uade.tpo.demo.entity.dto.MessageRequest;
import com.uade.tpo.demo.exceptions.BusinessRuleException;
import com.uade.tpo.demo.exceptions.NotFoundException;
import com.uade.tpo.demo.repository.MessageRepository;
import com.uade.tpo.demo.repository.SupportTicketRepository;
import com.uade.tpo.demo.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final SupportTicketRepository ticketRepository;
    private final UserRepository userRepository;

    @Override
    public List<Message> getByTicketId(Integer ticketId) {
        if (!ticketRepository.existsById(ticketId)) {
            throw new NotFoundException("SupportTicket", ticketId);
        }
        return messageRepository.findByTicketId(ticketId);
    }

    @Override
    @Transactional
    public Message send(Integer ticketId, MessageRequest request) {
        var ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new NotFoundException("SupportTicket", ticketId));

        if (ticket.getStatus() == TicketStatus.CLOSED) {
            throw new BusinessRuleException("Cannot send a message to a closed ticket");
        }

        var sender = userRepository.findById(request.senderId())
                .orElseThrow(() -> new NotFoundException("User", request.senderId()));

        var message = Message.builder()
                .ticket(ticket)
                .sender(sender)
                .content(request.content())
                .sentAt(new Date())
                .build();

        return messageRepository.save(message);
    }
}
