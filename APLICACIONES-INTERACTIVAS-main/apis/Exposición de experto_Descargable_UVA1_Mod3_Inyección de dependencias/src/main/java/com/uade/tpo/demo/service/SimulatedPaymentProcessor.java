package com.uade.tpo.demo.service;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.uade.tpo.demo.entity.dto.PaymentResult;

@Service
public class SimulatedPaymentProcessor implements PaymentProcessor {

    private static final Set<String> VALID_METHODS = Set.of(
            "CREDIT_CARD", "DEBIT_CARD", "CASH");

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
                    .message("Metodo de pago invalido: " + paymentMethod
                            + ". Metodos validos: " + VALID_METHODS)
                    .build();
        }

        if (simulateFailure) {
            return PaymentResult.builder()
                    .transactionId(null)
                    .status("FAILED")
                    .message("Pago rechazado (simulacion de fallo activada)")
                    .build();
        }

        String transactionId = UUID.randomUUID().toString();

        return PaymentResult.builder()
                .transactionId(transactionId)
                .status("COMPLETED")
                .message("Pago procesado exitosamente por $" + amount + " via " + paymentMethod)
                .build();
    }
}
