package com.uade.tpo.demo.service;

import java.util.List;

import com.uade.tpo.demo.entity.Refund;
import com.uade.tpo.demo.entity.RefundStatus;
import com.uade.tpo.demo.entity.dto.RefundRequest;

public interface RefundService {
    List<Refund> getByReturnId(Integer returnId);

    Refund getById(Integer refundId);

    Refund create(RefundRequest request);

    Refund updateStatus(Integer refundId, RefundStatus status);
}
