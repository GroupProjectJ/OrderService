package com.ecommerce.orderservice.service;

import com.ecommerce.orderservice.dto.ProductResponse;
import com.ecommerce.orderservice.exception.ProductServiceException;
import com.ecommerce.orderservice.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class ProductServiceClient {

    private final RestTemplate restTemplate;
    private final String productServiceUrl;

    public ProductServiceClient(RestTemplate restTemplate,
                                @Value("${product.service.url}") String productServiceUrl) {
        this.restTemplate = restTemplate;
        this.productServiceUrl = productServiceUrl;
    }

    public ProductResponse getProductById(Long productId) {
        String url = productServiceUrl + "/api/products/" + productId;
        try {
            ResponseEntity<ProductResponse> response = restTemplate.getForEntity(url, ProductResponse.class);
            return response.getBody();
        } catch (HttpClientErrorException.NotFound ex) {
            throw new ResourceNotFoundException("Product not found with id: " + productId);
        } catch (RestClientException ex) {
            throw new ProductServiceException("Failed to reach Product Service: " + ex.getMessage(), ex);
        }
    }
}
