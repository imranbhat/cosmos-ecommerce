package com.cosmos.inventory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(InventoryController.class);

    @Autowired
    private InventoryRepository inventoryRepository;

    @GetMapping("/{productId}")
    public ResponseEntity<Inventory> getInventoryByProductId(@PathVariable Long productId) {
        logger.info("Fetching inventory for Product ID: {}", productId);
        Optional<Inventory> inventory = inventoryRepository.findByProductId(productId);
        return inventory.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public Inventory createInventory(@RequestBody Inventory inventory) {
        logger.info("Creating inventory for Product ID: {}, Quantity: {}", inventory.getProductId(),
                inventory.getQuantity());
        return inventoryRepository.save(inventory);
    }

    @PutMapping("/{productId}/deduct")
    public ResponseEntity<Inventory> deductQuantity(@PathVariable Long productId, @RequestParam Integer quantity) {
        logger.info("Request to deduct {} items for Product ID: {}", quantity, productId);
        Optional<Inventory> inventoryOpt = inventoryRepository.findByProductId(productId);
        if (inventoryOpt.isPresent()) {
            Inventory inventory = inventoryOpt.get();
            if (inventory.getQuantity() >= quantity) {
                inventory.setQuantity(inventory.getQuantity() - quantity);
                Inventory saved = inventoryRepository.save(inventory);
                logger.info("Deduction successful. New quantity: {}", saved.getQuantity());
                return ResponseEntity.ok(saved);
            } else {
                logger.warn("Not enough stock for Product ID: {}. Requested: {}, Available: {}", productId, quantity,
                        inventory.getQuantity());
                return ResponseEntity.badRequest().build(); // Not enough stock
            }
        }
        logger.error("Inventory not found for Product ID: {}", productId);
        return ResponseEntity.notFound().build();
    }
}
