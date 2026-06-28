package com.ecommerce.orderservice.repository;

import com.ecommerce.orderservice.model.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class OrderRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void save_andFindById_returnsOrder() {
        Order order = new Order(10L, 1L, "Test Product", 2, new BigDecimal("100.00"),
                LocalDateTime.now(), "CREATED");
        entityManager.persistAndFlush(order);

        Optional<Order> found = orderRepository.findById(order.getOrderId());

        assertThat(found).isPresent();
        assertThat(found.get().getCustomerId()).isEqualTo(10L);
        assertThat(found.get().getProductId()).isEqualTo(1L);
    }

    @Test
    void save_persistsAllFields() {
        LocalDateTime now = LocalDateTime.now().withNano(0);
        Order order = new Order(20L, 5L, "Laptop", 1, new BigDecimal("999.99"), now, "CREATED");
        entityManager.persistAndFlush(order);

        Order found = entityManager.find(Order.class, order.getOrderId());

        assertThat(found.getCustomerId()).isEqualTo(20L);
        assertThat(found.getProductId()).isEqualTo(5L);
        assertThat(found.getProductName()).isEqualTo("Laptop");
        assertThat(found.getQuantity()).isEqualTo(1);
        assertThat(found.getTotalPrice()).isEqualByComparingTo(new BigDecimal("999.99"));
        assertThat(found.getStatus()).isEqualTo("CREATED");
    }

    @Test
    void findById_returnsEmpty_whenOrderDoesNotExist() {
        Optional<Order> found = orderRepository.findById(999L);

        assertThat(found).isEmpty();
    }

    @Test
    void save_generatesOrderId() {
        Order order = new Order(10L, 1L, "Product", 1, new BigDecimal("50.00"),
                LocalDateTime.now(), "CREATED");

        Order saved = orderRepository.save(order);

        assertThat(saved.getOrderId()).isNotNull();
        assertThat(saved.getOrderId()).isGreaterThan(0L);
    }

    @Test
    void delete_removesOrder() {
        Order order = new Order(10L, 1L, "Product", 1, new BigDecimal("50.00"),
                LocalDateTime.now(), "CREATED");
        entityManager.persistAndFlush(order);
        Long id = order.getOrderId();

        orderRepository.deleteById(id);
        entityManager.flush();

        assertThat(orderRepository.findById(id)).isEmpty();
    }
}
