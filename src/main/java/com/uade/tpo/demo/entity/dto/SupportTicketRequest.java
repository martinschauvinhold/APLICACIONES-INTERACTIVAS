package com.uade.tpo.demo.entity.dto;

import com.uade.tpo.demo.entity.TicketStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SupportTicketRequest(
        @NotNull Integer userId,
        @NotBlank String subject,
        @NotNull TicketStatus status) {
}
