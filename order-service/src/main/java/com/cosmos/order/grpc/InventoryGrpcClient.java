package com.cosmos.order.grpc;

import io.grpc.StatusRuntimeException;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class InventoryGrpcClient {

    private static final Logger logger = LoggerFactory.getLogger(InventoryGrpcClient.class);

    @GrpcClient("inventory-service")
    private InventoryServiceGrpc.InventoryServiceBlockingStub inventoryStub;

    /**
     * Get inventory by product ID using gRPC
     */
    public InventoryResponse getInventory(Long productId) {
        logger.info("[gRPC Client] Getting inventory for Product ID: {}", productId);

        try {
            GetInventoryRequest request = GetInventoryRequest.newBuilder()
                    .setProductId(productId)
                    .build();

            InventoryResponse response = inventoryStub.getInventory(request);

            if (response.getFound()) {
                logger.info("[gRPC Client] Inventory found - Product ID: {}, Quantity: {}",
                        productId, response.getQuantity());
            } else {
                logger.warn("[gRPC Client] Inventory not found for Product ID: {}", productId);
            }

            return response;
        } catch (StatusRuntimeException e) {
            logger.error("[gRPC Client] Error getting inventory for Product ID: {}", productId, e);
            throw new RuntimeException("Failed to get inventory via gRPC: " + e.getMessage(), e);
        }
    }

    /**
     * Deduct quantity from inventory using gRPC
     */
    public DeductInventoryResponse deductInventory(Long productId, Integer quantity) {
        logger.info("[gRPC Client] Deducting {} units from Product ID: {}", quantity, productId);

        try {
            DeductInventoryRequest request = DeductInventoryRequest.newBuilder()
                    .setProductId(productId)
                    .setQuantity(quantity)
                    .build();

            DeductInventoryResponse response = inventoryStub.deductInventory(request);

            if (response.getSuccess()) {
                logger.info("[gRPC Client] Deduction successful. Remaining: {}", response.getRemainingQuantity());
            } else {
                logger.warn("[gRPC Client] Deduction failed: {}", response.getMessage());
            }

            return response;
        } catch (StatusRuntimeException e) {
            logger.error("[gRPC Client] Error deducting inventory for Product ID: {}", productId, e);
            throw new RuntimeException("Failed to deduct inventory via gRPC: " + e.getMessage(), e);
        }
    }
}
