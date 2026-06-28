package com.ecommerce.orderservice.service;

import com.ecommerce.orderservice.dto.CreateOrderRequest;
import com.ecommerce.orderservice.dto.OrderEvent;
import com.ecommerce.orderservice.dto.OrderResponse;
import com.ecommerce.orderservice.dto.ProductResponse;
import com.ecommerce.orderservice.exception.ProductServiceException;
import com.ecommerce.orderservice.exception.ResourceNotFoundException;
import com.ecommerce.orderservice.model.Order;
import com.ecommerce.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpException;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductServiceClient productServiceClient;

    @Mock
    private OrderEventPublisher orderEventPublisher;

    @InjectMocks
    private OrderServiceImpl orderServiceImpl;

    private ProductResponse productResponse;
    private CreateOrderRequest request;

    @BeforeEach
    void setUp() {
        productResponse = new ProductResponse();
        productResponse.setProductId(1L);
        productResponse.setName("Test Product");
        productResponse.setUnitPrice(new BigDecimal("50.00"));

        request = new CreateOrderRequest();
        request.setCustomerId(10L);
        request.setProductId(1L);
        request.setQuantity(3);
    }

    @Test
    void createOrder_success_returnsOrderResponse() {
        Order savedOrder = new Order(10L, 1L, "Test Product", 3, new BigDecimal("150.00"),
                LocalDateTime.now(), "CREATED");
        savedOrder.setOrderId(100L);

        when(productServiceClient.getProductById(1L)).thenReturn(productResponse);
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        OrderResponse response = orderServiceImpl.createOrder(request);

        assertThat(response.getOrderId()).isEqualTo(100L);
        assertThat(response.getCustomerId()).isEqualTo(10L);
        assertThat(response.getProductId()).isEqualTo(1L);
        assertThat(response.getProductName()).isEqualTo("Test Product");
        assertThat(response.getQuantity()).isEqualTo(3);
        assertThat(response.getTotalPrice()).isEqualByComparingTo(new BigDecimal("150.00"));
        assertThat(response.getStatus()).isEqualTo("CREATED");
        verify(orderEventPublisher).publishOrderCreatedEvent(any(OrderEvent.class));
    }

    @Test
    void createOrder_calculatesCorrectTotalPrice() {
        request.setQuantity(4);
        productResponse.setUnitPrice(new BigDecimal("25.50"));

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        when(productServiceClient.getProductById(1L)).thenReturn(productResponse);
        when(orderRepository.save(captor.capture())).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setOrderId(1L);
            return o;
        });

        orderServiceImpl.createOrder(request);

        assertThat(captor.getValue().getTotalPrice()).isEqualByComparingTo(new BigDecimal("102.00"));
    }

    @Test
    void createOrder_setsStatusToCreated() {
        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        when(productServiceClient.getProductById(1L)).thenReturn(productResponse);
        when(orderRepository.save(captor.capture())).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setOrderId(1L);
            return o;
        });

        orderServiceImpl.createOrder(request);

        assertThat(captor.getValue().getStatus()).isEqualTo("CREATED");
    }

    @Test
    void createOrder_setsOrderDateToNow() {
        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        when(productServiceClient.getProductById(1L)).thenReturn(productResponse);
        when(orderRepository.save(captor.capture())).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setOrderId(1L);
            return o;
        });

        orderServiceImpl.createOrder(request);

        assertThat(captor.getValue().getOrderDate()).isNotNull();
    }

    @Test
    void createOrder_throwsResourceNotFoundException_whenProductNotFound() {
        when(productServiceClient.getProductById(1L))
                .thenThrow(new ResourceNotFoundException("Product not found with id: 1"));

        assertThatThrownBy(() -> orderServiceImpl.createOrder(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product not found with id: 1");

        verify(orderRepository, never()).save(any());
        verify(orderEventPublisher, never()).publishOrderCreatedEvent(any());
    }

    @Test
    void createOrder_throwsProductServiceException_whenServiceUnavailable() {
        when(productServiceClient.getProductById(1L))
                .thenThrow(new ProductServiceException("Failed to reach Product Service"));

        assertThatThrownBy(() -> orderServiceImpl.createOrder(request))
                .isInstanceOf(ProductServiceException.class)
                .hasMessageContaining("Failed to reach Product Service");

        verify(orderRepository, never()).save(any());
        verify(orderEventPublisher, never()).publishOrderCreatedEvent(any());
    }

    @Test
    void createOrder_savesOrderWithCorrectProductName() {
        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        when(productServiceClient.getProductById(1L)).thenReturn(productResponse);
        when(orderRepository.save(captor.capture())).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setOrderId(1L);
            return o;
        });

        orderServiceImpl.createOrder(request);

        assertThat(captor.getValue().getProductName()).isEqualTo("Test Product");
    }

    @Test
    void createOrder_stillReturnsResponse_whenPublishFails() {
        Order savedOrder = new Order(10L, 1L, "Test Product", 3, new BigDecimal("150.00"),
                LocalDateTime.now(), "CREATED");
        savedOrder.setOrderId(100L);

        when(productServiceClient.getProductById(1L)).thenReturn(productResponse);
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        doThrow(new AmqpException("Broker down"))
                .when(orderEventPublisher).publishOrderCreatedEvent(any(OrderEvent.class));

        OrderResponse response = orderServiceImpl.createOrder(request);

        assertThat(response.getOrderId()).isEqualTo(100L);
        assertThat(response.getStatus()).isEqualTo("CREATED");
        verify(orderRepository).save(any(Order.class));
    }
}
