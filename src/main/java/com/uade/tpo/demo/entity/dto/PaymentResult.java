package com.uade.tpo.demo.entity.dto;

import com.uade.tpo.demo.entity.PaymentResultStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResult {

    private String transactionId;
    private PaymentResultStatus status;
    private String message;
}
