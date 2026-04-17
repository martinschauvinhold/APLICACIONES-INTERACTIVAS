package com.uade.tpo.demo.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.uade.tpo.demo.entity.dto.PaymentResult;

@Service
public class SimulatedPaymentProcessor implements PaymentProcessor {

    private static final List<String> VALID_METHODS = List.of("CREDIT_CARD", "DEBIT_CARD", "CASH");

    private boolean simulateFailure = false;

    public void setSimulateFailure(boolean simulateFailure) {
        this.simulateFailure = simulateFailure;
    }

    @Override
    public PaymentResult process(BigDecimal amount, String paymentMethod) {
        if (!VALID_METHODS.contains(paymentMethod)) {
            return PaymentResult.builder()
                    .transactionId(null)
                    .status("FAILED")
                    .message("Método de pago inválido: " + paymentMethod)
                    .build();
        }

        if (simulateFailure) {
            return PaymentResult.builder()
                    .transactionId(null)
                    .status("FAILED")
                    .message("Pago simulado fallido")
                    .build();
        }

        return PaymentResult.builder()
                .transactionId(UUID.randomUUID().toString())
                .status("COMPLETED")
                .message("Pago procesado exitosamente")
                .build();
    }
}
