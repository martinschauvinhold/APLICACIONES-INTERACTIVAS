package com.uade.tpo.demo.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uade.tpo.demo.entity.Address;
import com.uade.tpo.demo.entity.Coupon;
import com.uade.tpo.demo.entity.Discount;
import com.uade.tpo.demo.entity.Inventory;
import com.uade.tpo.demo.entity.Order;
import com.uade.tpo.demo.entity.OrderItem;
import com.uade.tpo.demo.entity.OrderStatus;
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

    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

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
        User user = userRepository.findById(orderRequest.getUserId())
                .orElseThrow(() -> new NotFoundException("User", orderRequest.getUserId()));

        Address address = addressRepository.findById(orderRequest.getShippingAddressId())
                .orElseThrow(() -> new NotFoundException("Address", orderRequest.getShippingAddressId()));

        if (orderRequest.getItems() == null || orderRequest.getItems().isEmpty()) {
            throw new BusinessRuleException("La orden debe contener al menos un item");
        }

        Coupon coupon = null;
        if (orderRequest.getCouponCode() != null && !orderRequest.getCouponCode().isEmpty()) {
            coupon = couponService.validateCoupon(orderRequest.getCouponCode());
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderItemRequest itemReq : orderRequest.getItems()) {
            ProductVariant variant = productVariantRepository.findById(itemReq.getVariantId())
                    .orElseThrow(() -> new NotFoundException("ProductVariant", itemReq.getVariantId()));

            List<Inventory> inventoryRows = inventoryRepository.findByVariantId(itemReq.getVariantId());
            int stock = inventoryRows.stream().mapToInt(Inventory::getStockQuantity).sum();

            if (stock < itemReq.getQuantity()) {
                throw new BusinessRuleException("Stock insuficiente para variante " + itemReq.getVariantId()
                        + ". Stock disponible: " + stock + ", solicitado: " + itemReq.getQuantity());
            }

            // Determinar unitPrice usando PriceTiers (precio mayorista):
            // busca el tier aplicable con mayor minQuantity, si no hay usa basePrice
            BigDecimal unitPrice = priceTierRepository.findByVariantId(itemReq.getVariantId()).stream()
                    .filter(tier -> itemReq.getQuantity() >= tier.getMinQuantity())
                    .max(Comparator.comparingInt(PriceTier::getMinQuantity))
                    .map(PriceTier::getUnitPrice)
                    .orElse(variant.getBasePrice());

            Product product = variant.getProduct();
            List<Discount> activeDiscounts = discountService.getActiveDiscountsForProduct(product.getId());

            // Descuento por producto: tomar el mejor, tope = unitPrice
            final BigDecimal priceForDiscount = unitPrice;
            BigDecimal productDiscountApplied = activeDiscounts.stream()
                    .map(d -> calculateDiscountValue(d, priceForDiscount))
                    .max(Comparator.naturalOrder())
                    .orElse(BigDecimal.ZERO)
                    .min(unitPrice);

            // Descuento por cupon: solo si aplica al producto/categoria, tope = precio restante
            BigDecimal maxCoupon = unitPrice.subtract(productDiscountApplied).max(BigDecimal.ZERO);
            BigDecimal couponDiscountApplied = Optional.ofNullable(coupon)
                    .map(Coupon::getDiscount)
                    .filter(d -> couponAppliesTo(d, product))
                    .map(d -> calculateDiscountValue(d, unitPrice))
                    .orElse(BigDecimal.ZERO)
                    .min(maxCoupon);

            // Descuento total aplicado al item (producto + cupon)
            BigDecimal discountApplied = productDiscountApplied.add(couponDiscountApplied);

            BigDecimal effectivePrice = unitPrice.subtract(discountApplied).max(BigDecimal.ZERO);
            BigDecimal subtotal = effectivePrice.multiply(BigDecimal.valueOf(itemReq.getQuantity()));

            OrderItem orderItem = OrderItem.builder()
                    .variant(variant)
                    .quantity(itemReq.getQuantity())
                    .unitPriceAtTime(unitPrice)
                    .discountApplied(discountApplied)
                    .subtotal(subtotal)
                    .build();

            orderItems.add(orderItem);
            totalAmount = totalAmount.add(subtotal);
        }

        Order order = Order.builder()
                .user(user)
                .shippingAddress(address)
                .status(OrderStatus.PENDING)
                .totalAmount(totalAmount)
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();

        order = orderRepository.save(order);

        for (OrderItem item : orderItems) {
            item.setOrder(order);
        }
        orderItemRepository.saveAll(orderItems);

        if (coupon != null) {
            coupon.setTimesUsed(coupon.getTimesUsed() + 1);
            couponRepository.save(coupon);
        }

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
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order", orderId));

        OrderStatus status = order.getStatus();
        if (status != OrderStatus.PENDING && status != OrderStatus.PAID) {
            throw new BusinessRuleException("No se puede cancelar una orden con estado: " + status);
        }

        if (status == OrderStatus.PAID) {
            logger.warn("Cancelando orden {} en estado PAID — se restaurará stock y se marcarán pagos como REFUNDED", orderId);
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

        order.setStatus(OrderStatus.CANCELLED);
        order.setUpdatedAt(new Date());
        return orderRepository.save(order);
    }

    // Calcula el valor en $ de un descuento aplicado a un precio unitario.
    // PERCENTAGE: porcentaje sobre el precio. FIXED: monto fijo.
    private BigDecimal calculateDiscountValue(Discount discount, BigDecimal unitPrice) {
        if ("PERCENTAGE".equals(discount.getDiscountType())) {
            return unitPrice.multiply(discount.getValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }
        return discount.getValue();
    }

    // Verifica si un descuento aplica a un producto dado (por producto o por categoria)
    private boolean couponAppliesTo(Discount discount, Product product) {
        if ("PRODUCT".equals(discount.getAppliesTo()) && discount.getProduct() != null) {
            return discount.getProduct().getId().equals(product.getId());
        }
        if ("CATEGORY".equals(discount.getAppliesTo()) && discount.getCategory() != null) {
            return discount.getCategory().getId().equals(product.getCategory().getId());
        }
        return false;
    }

    @Override
    public int cancelExpiredOrders() {
        // 1. Calculate cutoff (48 hours ago)
        long fortyEightHoursMs = 48L * 60 * 60 * 1000;
        Date cutoff = new Date(System.currentTimeMillis() - fortyEightHoursMs);

        List<Order> expiredOrders = orderRepository.findByStatusAndCreatedAtBefore(OrderStatus.PENDING, cutoff);

        for (Order order : expiredOrders) {
            order.setStatus(OrderStatus.CANCELLED);
            order.setUpdatedAt(new Date());
            orderRepository.save(order);
        }

        if (!expiredOrders.isEmpty()) {
            logger.warn("Se cancelaron {} órdenes expiradas (PENDING hace más de 48hs). IDs: {}",
                    expiredOrders.size(),
                    expiredOrders.stream().map(o -> String.valueOf(o.getId())).toList());
        }

        return expiredOrders.size();
    }
}
