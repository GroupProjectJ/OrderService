package com.ecommerce.orderservice.service;

import com.ecommerce.orderservice.dto.OrderEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OrderEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(OrderEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final String exchangeName;
    private final String routingKey;

    public OrderEventPublisher(RabbitTemplate rabbitTemplate,
                               @Value("${rabbitmq.exchange.name}") String exchangeName,
                               @Value("${rabbitmq.routing.key.order.created}") String routingKey) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchangeName = exchangeName;
        this.routingKey = routingKey;
    }

    public void publishOrderCreatedEvent(OrderEvent event) {
        log.info("Publishing order event for orderId={}", event.getOrderId());
        rabbitTemplate.convertAndSend(exchangeName, routingKey, event);
        log.info("Order event published successfully for orderId={}", event.getOrderId());
    }
}
