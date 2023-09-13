package com.petclinic.inventoryservice.businesslayer;

import com.petclinic.inventoryservice.datalayer.Inventory.Inventory;
import com.petclinic.inventoryservice.datalayer.Inventory.InventoryRepository;
import com.petclinic.inventoryservice.datalayer.Product.Product;
import com.petclinic.inventoryservice.datalayer.Product.ProductRepository;
import com.petclinic.inventoryservice.presentationlayer.ProductRequestDTO;
import com.petclinic.inventoryservice.presentationlayer.ProductResponseDTO;
import com.petclinic.inventoryservice.utils.EntityDTOUtil;
import com.petclinic.inventoryservice.utils.exceptions.InvalidInputException;
import com.petclinic.inventoryservice.utils.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;


@Service
@RequiredArgsConstructor
public class ProductInventoryServiceImpl implements ProductInventoryService{

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    @Override
    public Mono<ProductResponseDTO> addProductToInventory(Mono<ProductRequestDTO> productRequestDTOMono, String inventoryId) {
        Inventory inventory = inventoryRepository.findInventoryByInventoryId(inventoryId);
        return productRequestDTOMono
                .publishOn(Schedulers.boundedElastic())
                .flatMap(requestDTO -> {
                    if (requestDTO.getProductName() == null || requestDTO.getProductPrice() == null || requestDTO.getProductQuantity() == null) {
                        return Mono.error(new InvalidInputException("Product must have an inventory id, product name, product price, and product quantity."));
                    } else if (requestDTO.getProductPrice() < 0 || requestDTO.getProductQuantity() < 0) {
                        return Mono.error(new InvalidInputException("Product price and quantity must be greater than 0."));
                    } else if (inventory == null) {
                        return Mono.error(new NotFoundException("Inventory not found with id: " + inventoryId));
                    } else {
                        return generateUniqueSku()
                                .flatMap(sku ->{
                                    Product productEntity = EntityDTOUtil.toProductEntity(requestDTO);
                                    productEntity.setSku(sku);
                                    return productRepository.insert(productEntity)
                                            .map(EntityDTOUtil::toProductResponseDTO);
                                });
                    }
                }).switchIfEmpty(Mono.error(new InvalidInputException("Unable to save product to repository, an error occurred.")));
    }

    private Mono<Integer> generateUniqueSku() {
        return Mono.defer(() -> {
            int generatedSku = EntityDTOUtil.generateSKU();
            return productRepository.existsBySku(generatedSku)
                    .flatMap(exists -> {
                        // Generating a new SKU since the once generated is not unique
                        if (exists) {
                            return generateUniqueSku();
                        } else {
                            // Return the generated SKU since it is unique
                            return Mono.just(generatedSku);
                        }
                    });
        });
    }

}
