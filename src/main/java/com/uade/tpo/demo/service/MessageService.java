package com.uade.tpo.demo.service;

import java.util.List;

import com.uade.tpo.demo.entity.Message;
import com.uade.tpo.demo.entity.dto.MessageRequest;

public interface MessageService {

    List<Message> getByTicketId(Integer ticketId);

    Message send(Integer ticketId, MessageRequest request);
}
