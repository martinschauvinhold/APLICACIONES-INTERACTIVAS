package com.uade.tpo.demo.service;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uade.tpo.demo.entity.SupportTicket;
import com.uade.tpo.demo.entity.TicketStatus;
import com.uade.tpo.demo.entity.dto.SupportTicketRequest;
import com.uade.tpo.demo.exceptions.NotFoundException;
import com.uade.tpo.demo.repository.SupportTicketRepository;
import com.uade.tpo.demo.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SupportTicketServiceImpl implements SupportTicketService {

    private final SupportTicketRepository ticketRepository;
    private final UserRepository userRepository;

    @Override
    public List<SupportTicket> getAll() {
        return ticketRepository.findAll();
    }

    @Override
    public SupportTicket getById(Integer id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("SupportTicket", id));
    }

    @Override
    @Transactional
    public SupportTicket create(SupportTicketRequest request) {
        var user = userRepository.findById(request.userId())
                .orElseThrow(() -> new NotFoundException("User", request.userId()));

        var ticket = SupportTicket.builder()
                .user(user)
                .subject(request.subject())
                .status(request.status())
                .createdAt(new Date())
                .build();

        return ticketRepository.save(ticket);
    }

    @Override
    @Transactional
    public SupportTicket updateStatus(Integer id, TicketStatus status) {
        var ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("SupportTicket", id));

        ticket.setStatus(status);
        return ticketRepository.save(ticket);
    }
}
