package com.uade.tpo.demo.service;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uade.tpo.demo.entity.Refund;
import com.uade.tpo.demo.entity.RefundStatus;
import com.uade.tpo.demo.entity.ReturnStatus;
import com.uade.tpo.demo.entity.dto.RefundRequest;
import com.uade.tpo.demo.exceptions.BusinessRuleException;
import com.uade.tpo.demo.exceptions.NotFoundException;
import com.uade.tpo.demo.repository.ProductReturnRepository;
import com.uade.tpo.demo.repository.RefundRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefundServiceImpl implements RefundService {

    private final RefundRepository refundRepository;
    private final ProductReturnRepository returnRepository;

    @Override
    public List<Refund> getByReturnId(Integer returnId) {
        if (!returnRepository.existsById(returnId))
            throw new NotFoundException("ProductReturn", returnId);
        return refundRepository.findByProductReturnId(returnId);
    }

    @Override
    public Refund getById(Integer refundId) {
        return refundRepository.findById(refundId)
                .orElseThrow(() -> new NotFoundException("Refund", refundId));
    }

    @Override
    @Transactional
    public Refund create(RefundRequest request) {
        var productReturn = returnRepository.findById(request.returnId())
                .orElseThrow(() -> new NotFoundException("ProductReturn", request.returnId()));

        if (productReturn.getStatus() != ReturnStatus.APPROVED)
            throw new BusinessRuleException(
                    "Cannot create a refund: the return must be in APPROVED status");

        var refund = Refund.builder()
                .productReturn(productReturn)
                .amount(request.amount())
                .currency(request.currency() != null ? request.currency() : "ARS")
                .status(RefundStatus.PENDING)
                .processedAt(new Date())
                .build();
        return refundRepository.save(refund);
    }

    @Override
    @Transactional
    public Refund updateStatus(Integer refundId, RefundStatus status) {
        var refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new NotFoundException("Refund", refundId));
        refund.setStatus(status);
        return refundRepository.save(refund);
    }
}
