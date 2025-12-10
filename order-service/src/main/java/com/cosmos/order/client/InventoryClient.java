package com.cosmos.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "inventory-service", url = "${inventory.service.url:http://inventory-service:8080}")
public interface InventoryClient {

    @GetMapping("/inventory/{productId}")
    Object getInventoryByProductId(@PathVariable("productId") Long productId);

    @PutMapping("/inventory/{productId}/deduct")
    Object deductQuantity(@PathVariable("productId") Long productId, @RequestParam("quantity") Integer quantity);
}
