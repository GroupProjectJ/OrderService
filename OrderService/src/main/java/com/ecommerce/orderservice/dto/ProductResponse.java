package com.ecommerce.orderservice.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProductResponse {

    private Long productId;
    private String name;
    private BigDecimal unitPrice;
    private String description;
    private String category;
    private Integer stock;
}
