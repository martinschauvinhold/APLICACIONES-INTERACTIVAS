package com.uade.tpo.demo.controllers;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uade.tpo.demo.entity.Refund;
import com.uade.tpo.demo.entity.dto.RefundRequest;
import com.uade.tpo.demo.entity.dto.RefundResponse;
import com.uade.tpo.demo.entity.dto.RefundStatusRequest;
import com.uade.tpo.demo.service.RefundService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("refunds")
@RequiredArgsConstructor
@PreAuthorize("hasRole('admin')")
public class RefundsController {

    private final RefundService refundService;

    @GetMapping("/{refundId}")
    public ResponseEntity<RefundResponse> getById(@PathVariable Integer refundId) {
        return ResponseEntity.ok(RefundResponse.from(refundService.getById(refundId)));
    }

    @GetMapping("/return/{returnId}")
    public ResponseEntity<List<RefundResponse>> getByReturnId(@PathVariable Integer returnId) {
        List<RefundResponse> result = refundService.getByReturnId(returnId).stream().map(RefundResponse::from).toList();
        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<RefundResponse> create(@Valid @RequestBody RefundRequest request) {
        Refund created = refundService.create(request);
        return ResponseEntity.created(URI.create("/refunds/" + created.getId())).body(RefundResponse.from(created));
    }

    @PutMapping("/{refundId}/status")
    public ResponseEntity<RefundResponse> updateStatus(@PathVariable Integer refundId,
                                                @Valid @RequestBody RefundStatusRequest request) {
        return ResponseEntity.ok(RefundResponse.from(refundService.updateStatus(refundId, request.status())));
    }
}
