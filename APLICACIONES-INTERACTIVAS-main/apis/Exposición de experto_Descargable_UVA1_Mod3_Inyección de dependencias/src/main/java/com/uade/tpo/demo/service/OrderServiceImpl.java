package com.uade.tpo.demo.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uade.tpo.demo.entity.Address;
import com.uade.tpo.demo.entity.Coupon;
import com.uade.tpo.demo.entity.Discount;
import com.uade.tpo.demo.entity.Inventory;
import com.uade.tpo.demo.entity.Order;
import com.uade.tpo.demo.entity.OrderItem;
import com.uade.tpo.demo.entity.Payment;
import com.uade.tpo.demo.entity.PriceTier;
import com.uade.tpo.demo.entity.Product;
import com.uade.tpo.demo.entity.ProductVariant;
import com.uade.tpo.demo.entity.User;
import com.uade.tpo.demo.entity.dto.OrderItemRequest;
import com.uade.tpo.demo.entity.dto.OrderRequest;
import com.uade.tpo.demo.exceptions.BusinessRuleException;
import com.uade.tpo.demo.exceptions.NotFoundException;
import com.uade.tpo.demo.repository.AddressRepository;
import com.uade.tpo.demo.repository.CouponRepository;
import com.uade.tpo.demo.repository.InventoryRepository;
import com.uade.tpo.demo.repository.OrderItemRepository;
import com.uade.tpo.demo.repository.OrderRepository;
import com.uade.tpo.demo.repository.PaymentRepository;
import com.uade.tpo.demo.repository.PriceTierRepository;
import com.uade.tpo.demo.repository.ProductVariantRepository;
import com.uade.tpo.demo.repository.UserRepository;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private PriceTierRepository priceTierRepository;

    @Autowired
    private DiscountService discountService;

    @Autowired
    private CouponService couponService;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    public ArrayList<Order> getOrders() {
        return new ArrayList<>(orderRepository.findAll());
    }

    public Optional<Order> getOrderById(int orderId) {
        return orderRepository.findById(orderId);
    }

    public List<Order> getOrdersByUser(int userId) {
        return orderRepository.findByUserId(userId);
    }

    @Override
    @Transactional
    public Order createOrder(OrderRequest orderRequest) {
        // 1. Validate user
        User user = userRepository.findById(orderRequest.getUserId())
                .orElseThrow(() -> new NotFoundException("User", orderRequest.getUserId()));

        // 2. Validate address
        Address address = addressRepository.findById(orderRequest.getShippingAddressId())
                .orElseThrow(() -> new NotFoundException("Address", orderRequest.getShippingAddressId()));

        // 3. Validate items
        if (orderRequest.getItems() == null || orderRequest.getItems().isEmpty()) {
            throw new BusinessRuleException("La orden debe contener al menos un item");
        }

        // 4. Validate coupon if present
        Coupon coupon = null;
        if (orderRequest.getCouponCode() != null && !orderRequest.getCouponCode().isEmpty()) {
            coupon = couponService.validateCoupon(orderRequest.getCouponCode());
        }

        // Build the order first
        Order order = Order.builder()
                .user(user)
                .shippingAddress(address)
                .status("PENDING")
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();

        order = orderRepository.save(order);

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        // 5. Process each item
        for (OrderItemRequest itemReq : orderRequest.getItems()) {
            // 5a. Find variant
            ProductVariant variant = productVariantRepository.findById(itemReq.getVariantId())
                    .orElseThrow(() -> new NotFoundException("ProductVariant", itemReq.getVariantId()));

            // 5b-5c. Check stock
            List<Inventory> inventoryRows = inventoryRepository.findByVariantId(itemReq.getVariantId());
            int stock = inventoryRows.stream().mapToInt(Inventory::getStockQuantity).sum();

            // 5d. Validate stock
            if (stock < itemReq.getQuantity()) {
                throw new BusinessRuleException("Stock insuficiente para variante " + itemReq.getVariantId()
                        + ". Stock disponible: " + stock + ", solicitado: " + itemReq.getQuantity());
            }

            // 5e. Determine unit price using PriceTiers
            List<PriceTier> tiers = priceTierRepository.findByVariantId(itemReq.getVariantId());
            tiers.sort(Comparator.comparingInt(PriceTier::getMinQuantity).reversed());

            BigDecimal unitPrice = variant.getBasePrice();
            for (PriceTier tier : tiers) {
                if (itemReq.getQuantity() >= tier.getMinQuantity()) {
                    unitPrice = tier.getUnitPrice();
                    break;
                }
            }

            // 5f. Get active discounts for product
            Product product = variant.getProduct();
            List<Discount> activeDiscounts = discountService.getActiveDiscountsForProduct(product.getId());

            // 5g. Calculate productDiscountApplied (best discount)
            BigDecimal productDiscountApplied = BigDecimal.ZERO;
            if (!activeDiscounts.isEmpty()) {
                BigDecimal bestDiscount = BigDecimal.ZERO;
                for (Discount d : activeDiscounts) {
                    BigDecimal discountValue;
                    if ("PERCENTAGE".equals(d.getDiscountType())) {
                        discountValue = unitPrice.multiply(d.getValue()).divide(BigDecimal.valueOf(100), 2,
                                RoundingMode.HALF_UP);
                    } else {
                        discountValue = d.getValue();
                    }
                    if (discountValue.compareTo(bestDiscount) > 0) {
                        bestDiscount = discountValue;
                    }
                }
                // Cap at unitPrice
                productDiscountApplied = bestDiscount.min(unitPrice);
            }

            // 5h. Calculate couponDiscountApplied
            BigDecimal couponDiscountApplied = BigDecimal.ZERO;
            if (coupon != null) {
                Discount couponDiscount = coupon.getDiscount();
                boolean applies = false;

                if ("PRODUCT".equals(couponDiscount.getAppliesTo()) && couponDiscount.getProduct() != null
                        && couponDiscount.getProduct().getId().equals(product.getId())) {
                    applies = true;
                } else if ("CATEGORY".equals(couponDiscount.getAppliesTo()) && couponDiscount.getCategory() != null
                        && couponDiscount.getCategory().getId().equals(product.getCategory().getId())) {
                    applies = true;
                }

                if (applies) {
                    if ("PERCENTAGE".equals(couponDiscount.getDiscountType())) {
                        couponDiscountApplied = unitPrice.multiply(couponDiscount.getValue())
                                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                    } else {
                        couponDiscountApplied = couponDiscount.getValue();
                    }
                    // Cap so total discount doesn't exceed unitPrice
                    BigDecimal maxCoupon = unitPrice.subtract(productDiscountApplied);
                    if (maxCoupon.compareTo(BigDecimal.ZERO) < 0) {
                        maxCoupon = BigDecimal.ZERO;
                    }
                    couponDiscountApplied = couponDiscountApplied.min(maxCoupon);
                }
            }

            // 5i. effectivePrice
            BigDecimal effectivePrice = unitPrice.subtract(productDiscountApplied).subtract(couponDiscountApplied);
            if (effectivePrice.compareTo(BigDecimal.ZERO) < 0) {
                effectivePrice = BigDecimal.ZERO;
            }

            // 5j. subtotal
            BigDecimal subtotal = effectivePrice.multiply(BigDecimal.valueOf(itemReq.getQuantity()));

            // 5k. Build OrderItem
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .variant(variant)
                    .quantity(itemReq.getQuantity())
                    .unitPriceAtTime(unitPrice)
                    .productDiscountApplied(productDiscountApplied)
                    .couponDiscountApplied(couponDiscountApplied)
                    .subtotal(subtotal)
                    .build();

            orderItems.add(orderItem);
            totalAmount = totalAmount.add(subtotal);
        }

        // 6. Set total
        order.setTotalAmount(totalAmount);
        order.setUpdatedAt(new Date());

        // 7. Save order
        order = orderRepository.save(order);

        // 8. Save all order items
        orderItemRepository.saveAll(orderItems);

        // 9. Increment coupon usage
        if (coupon != null) {
            coupon.setTimesUsed(coupon.getTimesUsed() + 1);
            couponRepository.save(coupon);
        }

        // 10. Return
        return order;
    }

    public Order updateOrder(int orderId, OrderRequest orderRequest) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order", orderId));
        order.setUpdatedAt(new Date());
        return orderRepository.save(order);
    }

    public void deleteOrder(int orderId) {
        orderRepository.deleteById(orderId);
    }

    @Override
    @Transactional
    public Order cancelOrder(int orderId) {
        // 1. Find order
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order", orderId));

        // 2. Validate status
        String status = order.getStatus();
        if (!"PENDING".equals(status) && !"PAID".equals(status)) {
            throw new BusinessRuleException("No se puede cancelar una orden con estado: " + status);
        }

        // 3. If PAID, restore stock and refund
        if ("PAID".equals(status)) {
            List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
            for (OrderItem item : items) {
                List<Inventory> inventoryRows = inventoryRepository.findByVariantId(item.getVariant().getId());
                if (!inventoryRows.isEmpty()) {
                    Inventory inv = inventoryRows.get(0);
                    inv.setStockQuantity(inv.getStockQuantity() + item.getQuantity());
                    inventoryRepository.save(inv);
                }
            }

            List<Payment> payments = paymentRepository.findByOrderId(orderId);
            for (Payment payment : payments) {
                payment.setPaymentStatus("REFUNDED");
                paymentRepository.save(payment);
            }
        }

        // 4. Cancel order
        order.setStatus("CANCELLED");
        order.setUpdatedAt(new Date());
        return orderRepository.save(order);
    }

    @Override
    public int cancelExpiredOrders() {
        // 1. Calculate cutoff (48 hours ago)
        long fortyEightHoursMs = 48L * 60 * 60 * 1000;
        Date cutoff = new Date(System.currentTimeMillis() - fortyEightHoursMs);

        // 2. Find expired pending orders
        List<Order> expiredOrders = orderRepository.findByStatusAndCreatedAtBefore("PENDING", cutoff);

        // 3. Cancel each
        for (Order order : expiredOrders) {
            order.setStatus("CANCELLED");
            order.setUpdatedAt(new Date());
            orderRepository.save(order);
        }

        // 4. Return count
        return expiredOrders.size();
    }
}
