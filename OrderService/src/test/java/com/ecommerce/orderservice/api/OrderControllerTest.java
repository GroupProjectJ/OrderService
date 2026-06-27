package com.ecommerce.orderservice.api;

import com.ecommerce.orderservice.dto.CreateOrderRequest;
import com.ecommerce.orderservice.dto.OrderResponse;
import com.ecommerce.orderservice.exception.ProductServiceException;
import com.ecommerce.orderservice.exception.ResourceNotFoundException;
import com.ecommerce.orderservice.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private OrderService orderService;

    private OrderResponse orderResponse;

    @BeforeEach
    void setUp() {
        orderResponse = new OrderResponse();
        orderResponse.setOrderId(1L);
        orderResponse.setCustomerId(10L);
        orderResponse.setProductId(1L);
        orderResponse.setProductName("Test Product");
        orderResponse.setQuantity(2);
        orderResponse.setTotalPrice(new BigDecimal("100.00"));
        orderResponse.setOrderDate(LocalDateTime.now());
        orderResponse.setStatus("CREATED");
    }

    @Test
    void createOrder_returns201_whenValidRequest() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerId(10L);
        request.setProductId(1L);
        request.setQuantity(2);

        when(orderService.createOrder(any(CreateOrderRequest.class))).thenReturn(orderResponse);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").value(1))
                .andExpect(jsonPath("$.customerId").value(10))
                .andExpect(jsonPath("$.productName").value("Test Product"))
                .andExpect(jsonPath("$.quantity").value(2))
                .andExpect(jsonPath("$.status").value("CREATED"));
    }

    @Test
    void createOrder_returns400_whenCustomerIdMissing() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setProductId(1L);
        request.setQuantity(2);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.customerId").exists());
    }

    @Test
    void createOrder_returns400_whenProductIdMissing() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerId(10L);
        request.setQuantity(2);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.productId").exists());
    }

    @Test
    void createOrder_returns400_whenQuantityIsZero() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerId(10L);
        request.setProductId(1L);
        request.setQuantity(0);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.quantity").exists());
    }

    @Test
    void createOrder_returns400_whenQuantityIsNegative() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerId(10L);
        request.setProductId(1L);
        request.setQuantity(-1);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.quantity").exists());
    }

    @Test
    void createOrder_returns404_whenProductNotFound() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerId(10L);
        request.setProductId(99L);
        request.setQuantity(1);

        when(orderService.createOrder(any(CreateOrderRequest.class)))
                .thenThrow(new ResourceNotFoundException("Product not found with id: 99"));

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Product not found with id: 99"))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void createOrder_returns503_whenProductServiceUnavailable() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerId(10L);
        request.setProductId(1L);
        request.setQuantity(1);

        when(orderService.createOrder(any(CreateOrderRequest.class)))
                .thenThrow(new ProductServiceException("Failed to reach Product Service"));

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.message").value("Failed to reach Product Service"))
                .andExpect(jsonPath("$.status").value(503));
    }
}
