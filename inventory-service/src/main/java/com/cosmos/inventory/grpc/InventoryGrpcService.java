package com.cosmos.inventory.grpc;

import com.cosmos.inventory.Inventory;
import com.cosmos.inventory.InventoryRepository;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

@GrpcService
public class InventoryGrpcService extends InventoryServiceGrpc.InventoryServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(InventoryGrpcService.class);

    @Autowired
    private InventoryRepository inventoryRepository;

    @Override
    public void getInventory(GetInventoryRequest request, StreamObserver<InventoryResponse> responseObserver) {
        logger.info("[gRPC] GetInventory request for Product ID: {}", request.getProductId());

        Optional<Inventory> inventoryOpt = inventoryRepository.findByProductId(request.getProductId());

        InventoryResponse.Builder responseBuilder = InventoryResponse.newBuilder();

        if (inventoryOpt.isPresent()) {
            Inventory inventory = inventoryOpt.get();
            responseBuilder
                    .setId(inventory.getId())
                    .setProductId(inventory.getProductId())
                    .setQuantity(inventory.getQuantity())
                    .setFound(true);
            logger.info("[gRPC] Inventory found for Product ID: {}, Quantity: {}",
                    request.getProductId(), inventory.getQuantity());
        } else {
            responseBuilder.setFound(false);
            logger.warn("[gRPC] Inventory not found for Product ID: {}", request.getProductId());
        }

        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void deductInventory(DeductInventoryRequest request,
            StreamObserver<DeductInventoryResponse> responseObserver) {
        logger.info("[gRPC] DeductInventory request for Product ID: {}, Quantity: {}",
                request.getProductId(), request.getQuantity());

        Optional<Inventory> inventoryOpt = inventoryRepository.findByProductId(request.getProductId());

        DeductInventoryResponse.Builder responseBuilder = DeductInventoryResponse.newBuilder();

        if (inventoryOpt.isPresent()) {
            Inventory inventory = inventoryOpt.get();

            if (inventory.getQuantity() >= request.getQuantity()) {
                inventory.setQuantity(inventory.getQuantity() - request.getQuantity());
                Inventory saved = inventoryRepository.save(inventory);

                responseBuilder
                        .setSuccess(true)
                        .setMessage("Inventory deducted successfully")
                        .setRemainingQuantity(saved.getQuantity());

                logger.info("[gRPC] Deduction successful. Remaining quantity: {}", saved.getQuantity());
            } else {
                responseBuilder
                        .setSuccess(false)
                        .setMessage("Insufficient stock. Available: " + inventory.getQuantity() + ", Requested: "
                                + request.getQuantity())
                        .setRemainingQuantity(inventory.getQuantity());

                logger.warn("[gRPC] Insufficient stock for Product ID: {}. Available: {}, Requested: {}",
                        request.getProductId(), inventory.getQuantity(), request.getQuantity());
            }
        } else {
            responseBuilder
                    .setSuccess(false)
                    .setMessage("Product not found in inventory")
                    .setRemainingQuantity(0);

            logger.error("[gRPC] Inventory not found for Product ID: {}", request.getProductId());
        }

        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }
}
