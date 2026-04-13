package com.uade.tpo.demo.service;

import java.util.List;

import com.uade.tpo.demo.entity.SupportTicket;
import com.uade.tpo.demo.entity.TicketStatus;
import com.uade.tpo.demo.entity.dto.SupportTicketRequest;

public interface SupportTicketService {

    List<SupportTicket> getAll();

    SupportTicket getById(Integer id);

    SupportTicket create(SupportTicketRequest request);

    SupportTicket updateStatus(Integer id, TicketStatus status);
}
