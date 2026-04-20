package com.uade.tpo.demo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.uade.tpo.demo.entity.Inventory;
import com.uade.tpo.demo.entity.Order;
import com.uade.tpo.demo.entity.OrderItem;
import com.uade.tpo.demo.entity.OrderStatus;
import com.uade.tpo.demo.entity.PaymentResultStatus;
import com.uade.tpo.demo.entity.Payment;
import com.uade.tpo.demo.entity.ProductVariant;
import com.uade.tpo.demo.entity.dto.PaymentRequest;
import com.uade.tpo.demo.entity.dto.PaymentResult;
import com.uade.tpo.demo.exceptions.BusinessRuleException;
import com.uade.tpo.demo.exceptions.NotFoundException;
import com.uade.tpo.demo.repository.InventoryRepository;
import com.uade.tpo.demo.repository.OrderItemRepository;
import com.uade.tpo.demo.repository.OrderRepository;
import com.uade.tpo.demo.repository.PaymentRepository;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock private PaymentRepository paymentRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private OrderItemRepository orderItemRepository;
    @Mock private InventoryRepository inventoryRepository;
    @Mock private PaymentProcessor paymentProcessor;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Test
    void getPayments_deberiaRetornarListaCompleta() {
        // Arrange
        var payments = List.of(
                Payment.builder().id(1).paymentStatus("COMPLETED").build(),
                Payment.builder().id(2).paymentStatus("FAILED").build());
        when(paymentRepository.findAll()).thenReturn(payments);

        // Act
        var result = paymentService.getPayments();

        // Assert
        assertThat(result).hasSize(2);
    }

    @Test
    void getPaymentById_deberiaRetornarPago_cuandoIdExiste() {
        // Arrange
        var payment = Payment.builder().id(1).paymentStatus("COMPLETED").build();
        when(paymentRepository.findById(1)).thenReturn(Optional.of(payment));

        // Act
        var result = paymentService.getPaymentById(1);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getPaymentStatus()).isEqualTo("COMPLETED");
    }

    @Test
    void getPaymentById_deberiaRetornarVacio_cuandoIdNoExiste() {
        // Arrange
        when(paymentRepository.findById(99)).thenReturn(Optional.empty());

        // Act
        var result = paymentService.getPaymentById(99);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void getPaymentsByOrder_deberiaRetornarListaFiltrada() {
        // Arrange
        var payments = List.of(Payment.builder().id(1).paymentStatus("COMPLETED").build());
        when(orderRepository.existsById(5)).thenReturn(true);
        when(paymentRepository.findByOrderId(5)).thenReturn(payments);

        // Act
        var result = paymentService.getPaymentsByOrder(5);

        // Assert
        assertThat(result).hasSize(1);
    }

    @Test
    void getPaymentsByOrder_deberiaLanzarNotFoundException_cuandoOrdenNoExiste() {
        // Arrange
        when(orderRepository.existsById(99)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> paymentService.getPaymentsByOrder(99))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void processPayment_deberiaProcesarExitosamente_cuandoOrdenEnPending() {
        // Arrange
        var order = Order.builder().id(1).status(OrderStatus.PENDING).totalAmount(new BigDecimal("500")).build();
        var variant = ProductVariant.builder().id(1).build();
        var item = OrderItem.builder().id(1).variant(variant).quantity(2).build();
        var inventory = Inventory.builder().id(1).stockQuantity(10).build();
        var paymentResult = PaymentResult.builder().status(PaymentResultStatus.COMPLETED).transactionId("TXN-001").build();
        var savedPayment = Payment.builder().id(1).paymentStatus("COMPLETED").transactionId("TXN-001").build();

        var request = new PaymentRequest();
        request.setOrderId(1);
        request.setPaymentMethod("CREDIT_CARD");

        when(orderRepository.findById(1)).thenReturn(Optional.of(order));
        when(orderItemRepository.findByOrderId(1)).thenReturn(List.of(item));
        when(inventoryRepository.findByVariantIdForUpdate(1)).thenReturn(List.of(inventory));
        when(paymentProcessor.process(any(), any())).thenReturn(paymentResult);
        when(paymentRepository.save(any())).thenReturn(savedPayment);
        when(inventoryRepository.saveAll(anyList())).thenReturn(List.of(inventory));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        var result = paymentService.processPayment(request, false);

        // Assert
        assertThat(result.getPaymentStatus()).isEqualTo("COMPLETED");
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
        verify(inventoryRepository).saveAll(anyList());
        verify(orderRepository).save(any());
    }

    @Test
    void processPayment_deberiaProcesarFallo_cuandoProcessorRetornaFailed() {
        // Arrange
        var order = Order.builder().id(1).status(OrderStatus.PENDING).totalAmount(new BigDecimal("500")).build();
        var variant = ProductVariant.builder().id(1).build();
        var item = OrderItem.builder().id(1).variant(variant).quantity(2).build();
        var inventory = Inventory.builder().id(1).stockQuantity(10).build();
        var paymentResult = PaymentResult.builder().status(PaymentResultStatus.FAILED).build();
        var savedPayment = Payment.builder().id(1).paymentStatus("FAILED").build();

        var request = new PaymentRequest();
        request.setOrderId(1);
        request.setPaymentMethod("CREDIT_CARD");

        when(orderRepository.findById(1)).thenReturn(Optional.of(order));
        when(orderItemRepository.findByOrderId(1)).thenReturn(List.of(item));
        when(inventoryRepository.findByVariantIdForUpdate(1)).thenReturn(List.of(inventory));
        when(paymentProcessor.process(any(), any())).thenReturn(paymentResult);
        when(paymentRepository.save(any())).thenReturn(savedPayment);

        // Act
        var result = paymentService.processPayment(request, true);

        // Assert
        assertThat(result.getPaymentStatus()).isEqualTo("FAILED");
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    void processPayment_deberiaLanzarNotFoundException_cuandoOrdenNoExiste() {
        // Arrange
        var request = new PaymentRequest();
        request.setOrderId(99);
        when(orderRepository.findById(99)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> paymentService.processPayment(request, false))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void processPayment_deberiaLanzarBusinessRuleException_cuandoOrdenNoEstaPending() {
        // Arrange
        var order = Order.builder().id(1).status(OrderStatus.PAID).build();
        var request = new PaymentRequest();
        request.setOrderId(1);
        when(orderRepository.findById(1)).thenReturn(Optional.of(order));

        // Act & Assert
        assertThatThrownBy(() -> paymentService.processPayment(request, false))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("PENDING");
    }
}
