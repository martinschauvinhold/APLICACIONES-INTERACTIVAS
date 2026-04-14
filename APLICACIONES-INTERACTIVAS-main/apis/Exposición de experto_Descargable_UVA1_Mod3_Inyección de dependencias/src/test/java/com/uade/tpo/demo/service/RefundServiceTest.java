package com.uade.tpo.demo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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

import com.uade.tpo.demo.entity.ProductReturn;
import com.uade.tpo.demo.entity.Refund;
import com.uade.tpo.demo.entity.RefundStatus;
import com.uade.tpo.demo.entity.ReturnStatus;
import com.uade.tpo.demo.entity.dto.RefundRequest;
import com.uade.tpo.demo.exceptions.BusinessRuleException;
import com.uade.tpo.demo.exceptions.NotFoundException;
import com.uade.tpo.demo.repository.ProductReturnRepository;
import com.uade.tpo.demo.repository.RefundRepository;

@ExtendWith(MockitoExtension.class)
class RefundServiceTest {

    @Mock
    private RefundRepository refundRepository;

    @Mock
    private ProductReturnRepository returnRepository;

    @InjectMocks
    private RefundServiceImpl refundService;

    @Test
    void getByReturnId_shouldReturnRefunds_whenReturnExists() {
        // Arrange
        var productReturn = ProductReturn.builder().id(1).status(ReturnStatus.APPROVED).build();
        var refunds = List.of(
                Refund.builder().id(1).productReturn(productReturn).amount(new BigDecimal("150.00")).build());
        when(returnRepository.existsById(1)).thenReturn(true);
        when(refundRepository.findByProductReturnId(1)).thenReturn(refunds);

        // Act
        var result = refundService.getByReturnId(1);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAmount()).isEqualByComparingTo("150.00");
    }

    @Test
    void getByReturnId_shouldThrowNotFoundException_whenReturnNotFound() {
        // Arrange
        when(returnRepository.existsById(99)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> refundService.getByReturnId(99))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("ProductReturn")
                .hasMessageContaining("99");
    }

    @Test
    void getById_shouldReturnRefund_whenIdExists() {
        // Arrange
        var refund = Refund.builder().id(1).amount(new BigDecimal("200.00")).status(RefundStatus.PROCESSED).build();
        when(refundRepository.findById(1)).thenReturn(Optional.of(refund));

        // Act
        var result = refundService.getById(1);

        // Assert
        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getStatus()).isEqualTo(RefundStatus.PROCESSED);
    }

    @Test
    void getById_shouldThrowNotFoundException_whenIdNotFound() {
        // Arrange
        when(refundRepository.findById(99)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> refundService.getById(99))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void create_shouldSaveAndReturnRefund_whenReturnIsApproved() {
        // Arrange
        var productReturn = ProductReturn.builder().id(1).status(ReturnStatus.APPROVED).build();
        var request = new RefundRequest(1, new BigDecimal("300.00"), "ARS");
        var savedRefund = Refund.builder().id(10).productReturn(productReturn)
                .amount(new BigDecimal("300.00")).status(RefundStatus.PENDING).build();
        when(returnRepository.findById(1)).thenReturn(Optional.of(productReturn));
        when(refundRepository.save(any())).thenReturn(savedRefund);

        // Act
        var result = refundService.create(request);

        // Assert
        assertThat(result.getId()).isEqualTo(10);
        assertThat(result.getAmount()).isEqualByComparingTo("300.00");
        verify(refundRepository).save(any());
    }

    @Test
    void create_shouldThrowNotFoundException_whenReturnNotFound() {
        // Arrange
        var request = new RefundRequest(99, new BigDecimal("300.00"), "ARS");
        when(returnRepository.findById(99)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> refundService.create(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("ProductReturn")
                .hasMessageContaining("99");
    }

    @Test
    void create_shouldThrowBusinessRuleException_whenReturnNotApproved() {
        // Arrange
        var pendingReturn = ProductReturn.builder().id(1).status(ReturnStatus.PENDING).build();
        var request = new RefundRequest(1, new BigDecimal("300.00"), "ARS");
        when(returnRepository.findById(1)).thenReturn(Optional.of(pendingReturn));

        // Act & Assert
        assertThatThrownBy(() -> refundService.create(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("APPROVED");
    }

    @Test
    void updateStatus_shouldUpdateStatus_whenIdExists() {
        // Arrange
        var refund = Refund.builder().id(1).status(RefundStatus.PENDING).build();
        when(refundRepository.findById(1)).thenReturn(Optional.of(refund));
        when(refundRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        var result = refundService.updateStatus(1, RefundStatus.PROCESSED);

        // Assert
        assertThat(result.getStatus()).isEqualTo(RefundStatus.PROCESSED);
        verify(refundRepository).save(any());
    }

    @Test
    void updateStatus_shouldThrowNotFoundException_whenIdNotFound() {
        // Arrange
        when(refundRepository.findById(99)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> refundService.updateStatus(99, RefundStatus.PROCESSED))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }
}
