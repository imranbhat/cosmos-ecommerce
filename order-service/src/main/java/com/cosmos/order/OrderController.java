package com.cosmos.order;

import com.cosmos.order.client.InventoryClient;
import com.cosmos.order.client.ProductClient;
import com.cosmos.order.client.UserClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductClient productClient;

    @Autowired
    private InventoryClient inventoryClient;

    @Autowired
    private UserClient userClient;

    @PostMapping
    @CircuitBreaker(name = "orderService", fallbackMethod = "createOrderFallback")
    public String createOrder(@RequestBody Order order) {
        logger.info("Received order request for Product ID: {}, User ID: {}, Quantity: {}", order.getProductId(),
                order.getUserId(), order.getQuantity());

        // Validate Product
        logger.info("Calling Product Service for Product ID: {}", order.getProductId());
        Object product = productClient.getProductById(order.getProductId());
        if (product == null) {
            logger.error("Product not found: {}", order.getProductId());
            return "Product not found";
        }

        // Validate User
        logger.info("Calling User Service for User ID: {}", order.getUserId());
        Object user = userClient.getUserById(order.getUserId());
        if (user == null) {
            logger.error("User not found: {}", order.getUserId());
            return "User not found";
        }

        // Validate Inventory and Deduct
        try {
            logger.info("Calling Inventory Service to deduct quantity: {}", order.getQuantity());
            inventoryClient.deductQuantity(order.getProductId(), order.getQuantity());
        } catch (Exception e) {
            logger.error("Inventory deduction failed", e);
            return "Inventory deduction failed. Product might be out of stock or service unavailable.";
        }

        order.setOrderDate(LocalDateTime.now());
        order.setStatus("CREATED");
        orderRepository.save(order);
        logger.info("Order created successfully with ID: {}", order.getId());

        return "Order created successfully with ID: " + order.getId();
    }

    public String createOrderFallback(Order order, Throwable t) {
        logger.error("Order creation fallback triggered", t);
        return "Order creation failed. Service unavailable or error occurred: " + t.getMessage();
    }
}
