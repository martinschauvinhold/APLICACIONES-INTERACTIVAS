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

import com.uade.tpo.demo.entity.Message;
import com.uade.tpo.demo.entity.SupportTicket;
import com.uade.tpo.demo.entity.TicketStatus;
import com.uade.tpo.demo.entity.User;
import com.uade.tpo.demo.entity.dto.MessageRequest;
import com.uade.tpo.demo.exceptions.BusinessRuleException;
import com.uade.tpo.demo.exceptions.NotFoundException;
import com.uade.tpo.demo.repository.MessageRepository;
import com.uade.tpo.demo.repository.SupportTicketRepository;
import com.uade.tpo.demo.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private SupportTicketRepository ticketRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MessageServiceImpl messageService;

    @Test
    void getByTicketId_deberiaRetornarMensajes_cuandoTicketExiste() {
        // Arrange
        var ticket = SupportTicket.builder().id(1).status(TicketStatus.OPEN).build();
        var sender = User.builder().id(1).username("john").build();
        var messages = List.of(
                Message.builder().id(1).ticket(ticket).sender(sender).content("Hola, necesito ayuda").build(),
                Message.builder().id(2).ticket(ticket).sender(sender).content("Sigo esperando respuesta").build());
        when(ticketRepository.existsById(1)).thenReturn(true);
        when(messageRepository.findByTicketId(1)).thenReturn(messages);

        // Act
        var result = messageService.getByTicketId(1);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getContent()).isEqualTo("Hola, necesito ayuda");
    }

    @Test
    void getByTicketId_deberiaLanzarNotFoundException_cuandoTicketNoExiste() {
        // Arrange
        when(ticketRepository.existsById(99)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> messageService.getByTicketId(99))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("SupportTicket")
                .hasMessageContaining("99");
    }

    @Test
    void send_deberiaGuardarMensaje_cuandoTicketEstaAbierto() {
        // Arrange
        var ticket = SupportTicket.builder().id(1).status(TicketStatus.OPEN).build();
        var sender = User.builder().id(2).username("john").build();
        var request = new MessageRequest(2, "Hola, necesito ayuda");
        var savedMessage = Message.builder().id(10).ticket(ticket).sender(sender).content("Hola, necesito ayuda").build();
        when(ticketRepository.findById(1)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(2)).thenReturn(Optional.of(sender));
        when(messageRepository.save(any())).thenReturn(savedMessage);

        // Act
        var result = messageService.send(1, request);

        // Assert
        assertThat(result.getId()).isEqualTo(10);
        assertThat(result.getContent()).isEqualTo("Hola, necesito ayuda");
        verify(messageRepository).save(any());
    }

    @Test
    void send_deberiaGuardarMensaje_cuandoTicketEstaPending() {
        // Arrange
        var ticket = SupportTicket.builder().id(1).status(TicketStatus.PENDING).build();
        var sender = User.builder().id(2).username("john").build();
        var request = new MessageRequest(2, "Alguna novedad?");
        var savedMessage = Message.builder().id(11).ticket(ticket).sender(sender).content("Alguna novedad?").build();
        when(ticketRepository.findById(1)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(2)).thenReturn(Optional.of(sender));
        when(messageRepository.save(any())).thenReturn(savedMessage);

        // Act
        var result = messageService.send(1, request);

        // Assert
        assertThat(result.getId()).isEqualTo(11);
        verify(messageRepository).save(any());
    }

    @Test
    void send_deberiaLanzarBusinessRuleException_cuandoTicketEstaCerrado() {
        // Arrange
        var closedTicket = SupportTicket.builder().id(1).status(TicketStatus.CLOSED).build();
        var request = new MessageRequest(2, "Hola");
        when(ticketRepository.findById(1)).thenReturn(Optional.of(closedTicket));

        // Act & Assert
        assertThatThrownBy(() -> messageService.send(1, request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("closed");
    }

    @Test
    void send_deberiaLanzarNotFoundException_cuandoTicketNoExiste() {
        // Arrange
        var request = new MessageRequest(2, "Hola");
        when(ticketRepository.findById(99)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> messageService.send(99, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("SupportTicket")
                .hasMessageContaining("99");
    }

    @Test
    void send_deberiaLanzarNotFoundException_cuandoSenderNoExiste() {
        // Arrange
        var ticket = SupportTicket.builder().id(1).status(TicketStatus.OPEN).build();
        var request = new MessageRequest(99, "Hola");
        when(ticketRepository.findById(1)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(99)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> messageService.send(1, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User")
                .hasMessageContaining("99");
    }
}
