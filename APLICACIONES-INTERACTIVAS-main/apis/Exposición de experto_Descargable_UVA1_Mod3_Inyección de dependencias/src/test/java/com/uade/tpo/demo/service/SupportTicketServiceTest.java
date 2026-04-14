package com.uade.tpo.demo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.uade.tpo.demo.entity.SupportTicket;
import com.uade.tpo.demo.entity.TicketStatus;
import com.uade.tpo.demo.entity.User;
import com.uade.tpo.demo.entity.dto.SupportTicketRequest;
import com.uade.tpo.demo.exceptions.NotFoundException;
import com.uade.tpo.demo.repository.SupportTicketRepository;
import com.uade.tpo.demo.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class SupportTicketServiceTest {

    @Mock
    private SupportTicketRepository ticketRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SupportTicketServiceImpl ticketService;

    @Test
    void getAll_deberiaRetornarListaCompleta() {
        // Arrange
        var tickets = List.of(
                SupportTicket.builder().id(1).subject("No funciona el pago").status(TicketStatus.OPEN).build(),
                SupportTicket.builder().id(2).subject("Quiero cancelar").status(TicketStatus.PENDING).build());
        when(ticketRepository.findAll()).thenReturn(tickets);

        // Act
        var result = ticketService.getAll();

        // Assert
        assertThat(result).hasSize(2);
    }

    @Test
    void getById_deberiaRetornarTicket_cuandoIdExiste() {
        // Arrange
        var ticket = SupportTicket.builder().id(1).subject("No funciona el pago").status(TicketStatus.OPEN).build();
        when(ticketRepository.findById(1)).thenReturn(Optional.of(ticket));

        // Act
        var result = ticketService.getById(1);

        // Assert
        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getSubject()).isEqualTo("No funciona el pago");
        assertThat(result.getStatus()).isEqualTo(TicketStatus.OPEN);
    }

    @Test
    void getById_deberiaLanzarNotFoundException_cuandoIdNoExiste() {
        // Arrange
        when(ticketRepository.findById(99)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> ticketService.getById(99))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void create_deberiaGuardarYRetornarTicket_cuandoUserExiste() {
        // Arrange
        var user = User.builder().id(1).username("john").build();
        var request = new SupportTicketRequest(1, "No funciona el pago", TicketStatus.OPEN);
        var savedTicket = SupportTicket.builder().id(10).user(user).subject("No funciona el pago")
                .status(TicketStatus.OPEN).build();
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(ticketRepository.save(any())).thenReturn(savedTicket);

        // Act
        var result = ticketService.create(request);

        // Assert
        assertThat(result.getId()).isEqualTo(10);
        assertThat(result.getSubject()).isEqualTo("No funciona el pago");
        verify(ticketRepository).save(any());
    }

    @Test
    void create_deberiaLanzarNotFoundException_cuandoUserNoExiste() {
        // Arrange
        var request = new SupportTicketRequest(99, "No funciona el pago", TicketStatus.OPEN);
        when(userRepository.findById(99)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> ticketService.create(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User")
                .hasMessageContaining("99");
    }

    @Test
    void updateStatus_deberiaActualizarStatus_cuandoIdExiste() {
        // Arrange
        var ticket = SupportTicket.builder().id(1).status(TicketStatus.OPEN).build();
        when(ticketRepository.findById(1)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        var result = ticketService.updateStatus(1, TicketStatus.CLOSED);

        // Assert
        assertThat(result.getStatus()).isEqualTo(TicketStatus.CLOSED);
        verify(ticketRepository).save(any());
    }

    @Test
    void updateStatus_deberiaLanzarNotFoundException_cuandoIdNoExiste() {
        // Arrange
        when(ticketRepository.findById(99)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> ticketService.updateStatus(99, TicketStatus.CLOSED))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }
}
