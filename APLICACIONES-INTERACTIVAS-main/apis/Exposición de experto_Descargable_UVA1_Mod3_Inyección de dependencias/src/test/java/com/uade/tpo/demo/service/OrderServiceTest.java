package com.uade.tpo.demo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.uade.tpo.demo.entity.Address;
import com.uade.tpo.demo.entity.Category;
import com.uade.tpo.demo.entity.Inventory;
import com.uade.tpo.demo.entity.Order;
import com.uade.tpo.demo.entity.OrderItem;
import com.uade.tpo.demo.entity.OrderStatus;
import com.uade.tpo.demo.entity.Payment;
import com.uade.tpo.demo.entity.PaymentStatus;
import com.uade.tpo.demo.entity.Product;
import com.uade.tpo.demo.entity.ProductVariant;
import com.uade.tpo.demo.entity.User;
import com.uade.tpo.demo.entity.dto.OrderItemRequest;
import com.uade.tpo.demo.entity.dto.OrderRequest;
import com.uade.tpo.demo.exceptions.BusinessRuleException;
import com.uade.tpo.demo.exceptions.NotFoundException;
import com.uade.tpo.demo.repository.AddressRepository;
import com.uade.tpo.demo.repository.CouponRepository;
import com.uade.tpo.demo.repository.DeliveryRepository;
import com.uade.tpo.demo.repository.InventoryRepository;
import com.uade.tpo.demo.repository.OrderItemRepository;
import com.uade.tpo.demo.repository.OrderRepository;
import com.uade.tpo.demo.repository.PaymentRepository;
import com.uade.tpo.demo.repository.PriceTierRepository;
import com.uade.tpo.demo.repository.ProductReturnRepository;
import com.uade.tpo.demo.repository.ProductVariantRepository;
import com.uade.tpo.demo.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private UserRepository userRepository;
    @Mock private AddressRepository addressRepository;
    @Mock private OrderItemRepository orderItemRepository;
    @Mock private ProductVariantRepository productVariantRepository;
    @Mock private InventoryRepository inventoryRepository;
    @Mock private PriceTierRepository priceTierRepository;
    @Mock private CouponRepository couponRepository;
    @Mock private PaymentRepository paymentRepository;
    @Mock private DeliveryRepository deliveryRepository;
    @Mock private ProductReturnRepository productReturnRepository;
    @Mock private DiscountService discountService;
    @Mock private CouponService couponService;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    void getOrders_deberiaRetornarListaCompleta() {
        // Arrange
        var orders = List.of(
                Order.builder().id(1).status(OrderStatus.PENDING).build(),
                Order.builder().id(2).status(OrderStatus.PAID).build());
        when(orderRepository.findAll()).thenReturn(orders);

        // Act
        var result = orderService.getOrders();

        // Assert
        assertThat(result).hasSize(2);
    }

    @Test
    void getOrderById_deberiaRetornarOrden_cuandoIdExiste() {
        // Arrange
        var order = Order.builder().id(1).status(OrderStatus.PENDING).totalAmount(BigDecimal.TEN).build();
        when(orderRepository.findById(1)).thenReturn(Optional.of(order));

        // Act
        var result = orderService.getOrderById(1);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    void getOrderById_deberiaRetornarVacio_cuandoIdNoExiste() {
        // Arrange
        when(orderRepository.findById(99)).thenReturn(Optional.empty());

        // Act
        var result = orderService.getOrderById(99);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void getOrdersByUser_deberiaRetornarOrdenesFiltradas() {
        // Arrange
        var orders = List.of(Order.builder().id(1).status(OrderStatus.PENDING).build());
        when(userRepository.existsById(5)).thenReturn(true);
        when(orderRepository.findByUserId(5)).thenReturn(orders);

        // Act
        var result = orderService.getOrdersByUser(5);

        // Assert
        assertThat(result).hasSize(1);
    }

    @Test
    void getOrdersByUser_deberiaLanzarNotFoundException_cuandoUserNoExiste() {
        // Arrange
        when(userRepository.existsById(99)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> orderService.getOrdersByUser(99))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void createOrder_deberiaCrearOrden_cuandoDatosValidos() {
        // Arrange
        var user = User.builder().id(1).build();
        var address = Address.builder().id(1).build();
        var category = Category.builder().id(1).build();
        var product = Product.builder().id(10).category(category).build();
        var variant = ProductVariant.builder().id(1).product(product).basePrice(new BigDecimal("100")).build();
        var inventory = Inventory.builder().id(1).variant(variant).stockQuantity(50).build();
        var savedOrder = Order.builder().id(100).status(OrderStatus.PENDING).totalAmount(new BigDecimal("200")).build();

        var itemReq = new OrderItemRequest();
        itemReq.setVariantId(1);
        itemReq.setQuantity(2);

        var request = new OrderRequest();
        request.setUserId(1);
        request.setShippingAddressId(1);
        request.setItems(List.of(itemReq));

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(addressRepository.findById(1)).thenReturn(Optional.of(address));
        when(productVariantRepository.findById(1)).thenReturn(Optional.of(variant));
        when(inventoryRepository.findByVariantId(1)).thenReturn(List.of(inventory));
        when(priceTierRepository.findByVariantId(1)).thenReturn(List.of());
        when(discountService.getActiveDiscountsForProduct(10)).thenReturn(List.of());
        when(orderRepository.save(any())).thenReturn(savedOrder);
        when(orderItemRepository.saveAll(anyList())).thenReturn(List.of());

        // Act
        var result = orderService.createOrder(request);

        // Assert
        assertThat(result.getId()).isEqualTo(100);
        assertThat(result.getStatus()).isEqualTo(OrderStatus.PENDING);
        verify(orderRepository).save(any());
        verify(orderItemRepository).saveAll(anyList());
    }

    @Test
    void createOrder_deberiaLanzarNotFoundException_cuandoUserNoExiste() {
        // Arrange
        var request = new OrderRequest();
        request.setUserId(99);
        request.setShippingAddressId(1);
        when(userRepository.findById(99)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void createOrder_deberiaLanzarNotFoundException_cuandoDireccionNoExiste() {
        // Arrange
        var user = User.builder().id(1).build();
        var request = new OrderRequest();
        request.setUserId(1);
        request.setShippingAddressId(99);
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(addressRepository.findById(99)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void createOrder_deberiaLanzarBusinessRuleException_cuandoItemsVacios() {
        // Arrange
        var user = User.builder().id(1).build();
        var address = Address.builder().id(1).build();
        var request = new OrderRequest();
        request.setUserId(1);
        request.setShippingAddressId(1);
        request.setItems(List.of());
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(addressRepository.findById(1)).thenReturn(Optional.of(address));

        // Act & Assert
        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("item");
    }

    @Test
    void createOrder_deberiaLanzarBusinessRuleException_cuandoStockInsuficiente() {
        // Arrange
        var user = User.builder().id(1).build();
        var address = Address.builder().id(1).build();
        var category = Category.builder().id(1).build();
        var product = Product.builder().id(10).category(category).build();
        var variant = ProductVariant.builder().id(1).product(product).basePrice(new BigDecimal("100")).build();
        var inventory = Inventory.builder().id(1).stockQuantity(1).build();

        var itemReq = new OrderItemRequest();
        itemReq.setVariantId(1);
        itemReq.setQuantity(10);

        var request = new OrderRequest();
        request.setUserId(1);
        request.setShippingAddressId(1);
        request.setItems(List.of(itemReq));

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(addressRepository.findById(1)).thenReturn(Optional.of(address));
        when(productVariantRepository.findById(1)).thenReturn(Optional.of(variant));
        when(inventoryRepository.findByVariantId(1)).thenReturn(List.of(inventory));

        // Act & Assert
        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Stock insuficiente");
    }

    @Test
    void updateOrder_deberiaActualizarYRetornar_cuandoIdExiste() {
        // Arrange
        var order = Order.builder().id(1).status(OrderStatus.PENDING).build();
        when(orderRepository.findById(1)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        var result = orderService.updateOrder(1, new OrderRequest());

        // Assert
        assertThat(result.getId()).isEqualTo(1);
        verify(orderRepository).save(any());
    }

    @Test
    void updateOrder_deberiaLanzarNotFoundException_cuandoIdNoExiste() {
        // Arrange
        when(orderRepository.findById(99)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> orderService.updateOrder(99, new OrderRequest()))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void deleteOrder_deberiaEliminar() {
        // Arrange
        when(orderItemRepository.findByOrderId(1)).thenReturn(List.of());
        when(paymentRepository.findByOrderId(1)).thenReturn(List.of());
        when(deliveryRepository.findByOrderId(1)).thenReturn(List.of());
        when(productReturnRepository.findByOrderId(1)).thenReturn(List.of());

        // Act
        orderService.deleteOrder(1);

        // Assert
        verify(orderRepository).deleteById(1);
    }

    @Test
    void cancelOrder_deberiaCancelarOrden_cuandoEstaEnPending() {
        // Arrange
        var order = Order.builder().id(1).status(OrderStatus.PENDING).build();
        when(orderRepository.findById(1)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        var result = orderService.cancelOrder(1);

        // Assert
        assertThat(result.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    void cancelOrder_deberiaCancelarYRevertirStock_cuandoEstaEnPaid() {
        // Arrange
        var order = Order.builder().id(1).status(OrderStatus.PAID).build();
        var variant = ProductVariant.builder().id(1).build();
        var item = OrderItem.builder().id(1).variant(variant).quantity(2).build();
        var inventory = Inventory.builder().id(1).stockQuantity(0).build();
        var payment = Payment.builder().id(1).paymentStatus(PaymentStatus.COMPLETED).build();

        when(orderRepository.findById(1)).thenReturn(Optional.of(order));
        when(orderItemRepository.findByOrderId(1)).thenReturn(List.of(item));
        when(inventoryRepository.findByVariantId(1)).thenReturn(List.of(inventory));
        when(inventoryRepository.save(any())).thenReturn(inventory);
        when(paymentRepository.findByOrderId(1)).thenReturn(List.of(payment));
        when(paymentRepository.save(any())).thenReturn(payment);
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        var result = orderService.cancelOrder(1);

        // Assert
        assertThat(result.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(inventory.getStockQuantity()).isEqualTo(2);
        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.REFUNDED);
    }

    @Test
    void cancelOrder_deberiaLanzarBusinessRuleException_cuandoEstadoInvalido() {
        // Arrange
        var order = Order.builder().id(1).status(OrderStatus.CANCELLED).build();
        when(orderRepository.findById(1)).thenReturn(Optional.of(order));

        // Act & Assert
        assertThatThrownBy(() -> orderService.cancelOrder(1))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("CANCELLED");
    }

    @Test
    void cancelOrder_deberiaLanzarNotFoundException_cuandoIdNoExiste() {
        // Arrange
        when(orderRepository.findById(99)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> orderService.cancelOrder(99))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void cancelExpiredOrders_deberiaCancelarOrdenesPendingAntiguas() {
        // Arrange
        var order1 = Order.builder().id(1).status(OrderStatus.PENDING).createdAt(new Date(0)).build();
        var order2 = Order.builder().id(2).status(OrderStatus.PENDING).createdAt(new Date(0)).build();
        when(orderRepository.findByStatusAndCreatedAtBefore(any(), any())).thenReturn(List.of(order1, order2));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        var count = orderService.cancelExpiredOrders();

        // Assert
        assertThat(count).isEqualTo(2);
        assertThat(order1.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(order2.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }
}
