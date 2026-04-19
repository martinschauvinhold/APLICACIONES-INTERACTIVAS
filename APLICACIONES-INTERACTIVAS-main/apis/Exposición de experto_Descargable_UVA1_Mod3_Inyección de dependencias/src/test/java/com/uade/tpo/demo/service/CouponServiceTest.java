package com.uade.tpo.demo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.uade.tpo.demo.entity.Coupon;
import com.uade.tpo.demo.entity.Discount;
import com.uade.tpo.demo.entity.dto.CouponRequest;
import com.uade.tpo.demo.exceptions.BusinessRuleException;
import com.uade.tpo.demo.exceptions.NotFoundException;
import com.uade.tpo.demo.repository.CouponRepository;
import com.uade.tpo.demo.repository.DiscountRepository;

@ExtendWith(MockitoExtension.class)
class CouponServiceTest {

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private DiscountRepository discountRepository;

    @InjectMocks
    private CouponServiceImpl couponService;

    @Test
    void getCoupons_deberiaRetornarListaCompleta() {
        // Arrange
        var coupons = List.of(
                Coupon.builder().id(1).code("DESCUENTO10").build(),
                Coupon.builder().id(2).code("PROMO20").build());
        when(couponRepository.findAll()).thenReturn(coupons);

        // Act
        var result = couponService.getCoupons();

        // Assert
        assertThat(result).hasSize(2);
    }

    @Test
    void getCouponById_deberiaRetornarCupon_cuandoIdExiste() {
        // Arrange
        var coupon = Coupon.builder().id(1).code("DESCUENTO10").isActive(true).build();
        when(couponRepository.findById(1)).thenReturn(Optional.of(coupon));

        // Act
        var result = couponService.getCouponById(1);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getCode()).isEqualTo("DESCUENTO10");
    }

    @Test
    void getCouponById_deberiaRetornarVacio_cuandoIdNoExiste() {
        // Arrange
        when(couponRepository.findById(99)).thenReturn(Optional.empty());

        // Act
        var result = couponService.getCouponById(99);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void createCoupon_deberiaGuardarYRetornarCupon_cuandoDescuentoExiste() {
        // Arrange
        var discount = Discount.builder().id(1).name("10% OFF").build();
        var request = new CouponRequest();
        request.setDiscountId(1);
        request.setCode("DESCUENTO10");
        request.setUsageLimit(100);

        when(discountRepository.findById(1)).thenReturn(Optional.of(discount));
        when(couponRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        var result = couponService.createCoupon(request);

        // Assert
        assertThat(result.getCode()).isEqualTo("DESCUENTO10");
        assertThat(result.isActive()).isTrue();
        assertThat(result.getTimesUsed()).isEqualTo(0);
        verify(couponRepository).save(any());
    }

    @Test
    void createCoupon_deberiaLanzarNotFoundException_cuandoDescuentoNoExiste() {
        // Arrange
        var request = new CouponRequest();
        request.setDiscountId(99);
        when(discountRepository.findById(99)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> couponService.createCoupon(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void deleteCoupon_deberiaEliminar_cuandoIdExiste() {
        // Arrange
        when(couponRepository.existsById(1)).thenReturn(true);

        // Act
        couponService.deleteCoupon(1);

        // Assert
        verify(couponRepository).deleteById(1);
    }

    @Test
    void deleteCoupon_deberiaLanzarNotFoundException_cuandoIdNoExiste() {
        // Arrange
        when(couponRepository.existsById(99)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> couponService.deleteCoupon(99))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void validateCoupon_deberiaRetornarCupon_cuandoEsValido() {
        // Arrange
        var futureDate = new Date(System.currentTimeMillis() + 86400000L);
        var coupon = Coupon.builder()
                .id(1).code("DESCUENTO10").isActive(true)
                .usageLimit(100).timesUsed(5).expiresAt(futureDate)
                .build();
        when(couponRepository.findByCode("DESCUENTO10")).thenReturn(Optional.of(coupon));

        // Act
        var result = couponService.validateCoupon("DESCUENTO10");

        // Assert
        assertThat(result.getCode()).isEqualTo("DESCUENTO10");
    }

    @Test
    void validateCoupon_deberiaLanzarNotFoundException_cuandoCodigoNoExiste() {
        // Arrange
        when(couponRepository.findByCode("INVALIDO")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> couponService.validateCoupon("INVALIDO"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void validateCoupon_deberiaLanzarBusinessRuleException_cuandoCuponInactivo() {
        // Arrange
        var coupon = Coupon.builder().id(1).code("INACTIVO").isActive(false).build();
        when(couponRepository.findByCode("INACTIVO")).thenReturn(Optional.of(coupon));

        // Act & Assert
        assertThatThrownBy(() -> couponService.validateCoupon("INACTIVO"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("desactivado");
    }

    @Test
    void validateCoupon_deberiaLanzarBusinessRuleException_cuandoCuponExpirado() {
        // Arrange
        var pastDate = new Date(System.currentTimeMillis() - 86400000L);
        var coupon = Coupon.builder().id(1).code("EXPIRADO").isActive(true).expiresAt(pastDate).build();
        when(couponRepository.findByCode("EXPIRADO")).thenReturn(Optional.of(coupon));

        // Act & Assert
        assertThatThrownBy(() -> couponService.validateCoupon("EXPIRADO"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("expirado");
    }

    @Test
    void validateCoupon_deberiaLanzarBusinessRuleException_cuandoLimiteDeusoAlcanzado() {
        // Arrange
        var futureDate = new Date(System.currentTimeMillis() + 86400000L);
        var coupon = Coupon.builder()
                .id(1).code("AGOTADO").isActive(true)
                .usageLimit(10).timesUsed(10).expiresAt(futureDate)
                .build();
        when(couponRepository.findByCode("AGOTADO")).thenReturn(Optional.of(coupon));

        // Act & Assert
        assertThatThrownBy(() -> couponService.validateCoupon("AGOTADO"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("limite de usos");
    }
}
