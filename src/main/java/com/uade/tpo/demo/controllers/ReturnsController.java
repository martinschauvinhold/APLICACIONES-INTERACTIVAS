package com.uade.tpo.demo.controllers;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uade.tpo.demo.entity.Order;
import com.uade.tpo.demo.entity.ProductReturn;
import com.uade.tpo.demo.entity.dto.ProductReturnRequest;
import com.uade.tpo.demo.entity.dto.ProductReturnResponse;
import com.uade.tpo.demo.exceptions.NotFoundException;
import com.uade.tpo.demo.service.AuthorizationService;
import com.uade.tpo.demo.service.OrderService;
import com.uade.tpo.demo.service.ProductReturnService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("returns")
@RequiredArgsConstructor
public class ReturnsController {

    private final ProductReturnService productReturnService;
    private final OrderService orderService;
    private final AuthorizationService authorizationService;

    @GetMapping
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<List<ProductReturnResponse>> getReturns() {
        List<ProductReturnResponse> result = productReturnService.getReturns().stream()
                .map(ProductReturnResponse::from)
                .toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{returnId}")
    @PreAuthorize("hasAnyRole('buyer', 'admin')")
    public ResponseEntity<ProductReturnResponse> getReturnById(@PathVariable Integer returnId) {
        ProductReturn productReturn = productReturnService.getReturnById(returnId);
        authorizationService.requireSelfOrAdmin(productReturn.getOrder().getUser().getId());
        return ResponseEntity.ok(ProductReturnResponse.from(productReturn));
    }

    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasAnyRole('buyer', 'admin')")
    public ResponseEntity<List<ProductReturnResponse>> getReturnsByOrder(@PathVariable Integer orderId) {
        Order order = orderService.getOrderById(orderId)
                .orElseThrow(() -> new NotFoundException("Order", orderId));
        authorizationService.requireSelfOrAdmin(order.getUser().getId());
        List<ProductReturnResponse> result = productReturnService.getReturnsByOrder(orderId).stream()
                .map(ProductReturnResponse::from)
                .toList();
        return ResponseEntity.ok(result);
    }

    @PostMapping
    @PreAuthorize("hasRole('buyer')")
    public ResponseEntity<ProductReturnResponse> createReturn(@Valid @RequestBody ProductReturnRequest request) {
        Order order = orderService.getOrderById(request.orderId())
                .orElseThrow(() -> new NotFoundException("Order", request.orderId()));
        authorizationService.requireSelf(order.getUser().getId());
        ProductReturn created = productReturnService.createReturn(request);
        return ResponseEntity.created(URI.create("/returns/" + created.getId()))
                .body(ProductReturnResponse.from(created));
    }

    @PutMapping("/{returnId}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<ProductReturnResponse> updateReturn(@PathVariable Integer returnId,
                                                       @Valid @RequestBody ProductReturnRequest request) {
        return ResponseEntity.ok(ProductReturnResponse.from(productReturnService.updateReturn(returnId, request)));
    }

    @DeleteMapping("/{returnId}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<Void> deleteReturn(@PathVariable Integer returnId) {
        productReturnService.deleteReturn(returnId);
        return ResponseEntity.noContent().build();
    }
}
