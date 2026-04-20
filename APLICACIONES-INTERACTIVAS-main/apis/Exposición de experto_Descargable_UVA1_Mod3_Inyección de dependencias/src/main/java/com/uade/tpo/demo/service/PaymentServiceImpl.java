package com.uade.tpo.demo.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uade.tpo.demo.entity.Inventory;
import com.uade.tpo.demo.entity.Order;
import com.uade.tpo.demo.entity.OrderItem;
import com.uade.tpo.demo.entity.OrderStatus;
import com.uade.tpo.demo.entity.PaymentResultStatus;
import com.uade.tpo.demo.entity.Payment;
import com.uade.tpo.demo.entity.dto.PaymentRequest;
import com.uade.tpo.demo.entity.dto.PaymentResult;
import com.uade.tpo.demo.exceptions.BusinessRuleException;
import com.uade.tpo.demo.exceptions.NotFoundException;
import com.uade.tpo.demo.repository.InventoryRepository;
import com.uade.tpo.demo.repository.OrderItemRepository;
import com.uade.tpo.demo.repository.OrderRepository;
import com.uade.tpo.demo.repository.PaymentRepository;

@Service
public class PaymentServiceImpl implements PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentServiceImpl.class);

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private PaymentProcessor paymentProcessor;

    public ArrayList<Payment> getPayments() {
        return new ArrayList<>(paymentRepository.findAll());
    }

    public Optional<Payment> getPaymentById(int paymentId) {
        return paymentRepository.findById(paymentId);
    }

    public List<Payment> getPaymentsByOrder(int orderId) {
        if (!orderRepository.existsById(orderId)) {
            throw new NotFoundException("Order", orderId);
        }
        return paymentRepository.findByOrderId(orderId);
    }

    /**
     * Procesa el pago de una orden:
     * 1. Valida que la orden exista y este en PENDING
     * 2. Lockea el inventario (PESSIMISTIC_WRITE) para evitar race conditions
     * 3. Revalida stock (pudo cambiar desde el checkout)
     * 4. Llama al PaymentProcessor (simulado o real)
     * 5. Si COMPLETED: descuenta stock y marca orden como PAID
     * 6. Si FAILED: guarda el Payment con status FAILED, orden sigue PENDING
     */
    @Override
    @Transactional
    public Payment processPayment(PaymentRequest paymentRequest, boolean simulateFailure) {
        Order order = orderRepository.findById(paymentRequest.getOrderId())
                .orElseThrow(() -> new NotFoundException("Order", paymentRequest.getOrderId()));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessRuleException(
                    "Solo se pueden pagar ordenes en estado PENDING. Estado actual: " + order.getStatus());
        }

        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());

        // Lockear todo el inventario de las variantes de la orden
        // y validar que haya stock suficiente antes de cobrar
        for (OrderItem item : items) {
            int variantId = item.getVariant().getId();
            List<Inventory> lockedInventory = inventoryRepository.findByVariantIdForUpdate(variantId);
            int stockAvailable = lockedInventory.stream().mapToInt(Inventory::getStockQuantity).sum();

            if (stockAvailable < item.getQuantity()) {
                throw new BusinessRuleException("Stock insuficiente para variante " + variantId
                        + ". Stock disponible: " + stockAvailable + ", solicitado: " + item.getQuantity());
            }
        }

        if (simulateFailure) {
            logger.warn("processPayment llamado con simulateFailure=true para orden {}", paymentRequest.getOrderId());
        }

        // Configurar simulacion de fallo si se pide (flag de testing, no en prod)
        if (paymentProcessor instanceof SimulatedPaymentProcessor simulated) {
            simulated.setSimulateFailure(simulateFailure);
        }

        PaymentResult result = paymentProcessor.process(order.getTotalAmount(), paymentRequest.getPaymentMethod());

        Payment payment = Payment.builder()
                .order(order)
                .paymentMethod(paymentRequest.getPaymentMethod())
                .transactionId(result.getTransactionId())
                .paymentStatus(result.getStatus().name())
                .paidAt(new Date())
                .build();

        payment = paymentRepository.save(payment);

        if (result.getStatus() == PaymentResultStatus.COMPLETED) {
            // Descontar stock (estrategia: primero disponible)
            for (OrderItem item : items) {
                decreaseStock(item.getVariant().getId(), item.getQuantity());
            }

            order.setStatus(OrderStatus.PAID);
            order.setUpdatedAt(new Date());
            orderRepository.save(order);
        }
        // Si FAILED: la orden sigue en PENDING y el usuario puede reintentar
        if (result.getStatus() != PaymentResultStatus.COMPLETED) {
            logger.warn("Pago fallido para orden {}. Método: {}, TransactionId: {}, Status: {}",
                    order.getId(), paymentRequest.getPaymentMethod(), result.getTransactionId(), result.getStatus());
        }

        return payment;
    }

    /**
     * Descuenta stock de una variante usando estrategia "primero disponible":
     * va descontando del primer Inventory row hasta cubrir la cantidad,
     * pasando al siguiente si el actual se queda sin stock.
     */
    private void decreaseStock(int variantId, int quantityToDecrease) {
        List<Inventory> rows = inventoryRepository.findByVariantIdForUpdate(variantId);
        int remaining = quantityToDecrease;

        for (Inventory inv : rows) {
            int toTake = Math.min(remaining, inv.getStockQuantity());
            if (toTake > 0) {
                inv.setStockQuantity(inv.getStockQuantity() - toTake);
                inv.setLastUpdated(new Date());
                remaining -= toTake;
            }
        }

        inventoryRepository.saveAll(rows);
    }
}
