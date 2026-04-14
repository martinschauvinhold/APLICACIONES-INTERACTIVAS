package com.uade.tpo.demo.entity;

import java.math.BigDecimal;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "DISCOUNTS")
public class Discount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "discount_id")
    private Integer id;

    @Column(name = "name")
    private String name;

    @Column(name = "discount_type")
    private String discountType;

    @Column(name = "value", precision = 10, scale = 2)
    private BigDecimal value;

    @Column(name = "applies_to")
    private String appliesTo;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(name = "min_price", precision = 10, scale = 2)
    private BigDecimal minPrice;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "starts_at")
    private Date startsAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "expires_at")
    private Date expiresAt;

    @Builder.Default
    @Column(name = "is_active")
    private boolean isActive = true;
}
