package com.uade.tpo.demo.entity.dto;

import com.uade.tpo.demo.entity.TicketStatus;

import jakarta.validation.constraints.NotNull;

public record TicketStatusRequest(@NotNull TicketStatus status) {
}
