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
//    @Override
//    public Mono<ProductResponseDTO> addProductToInventory(Mono<ProductRequestDTO> productRequestDTOMono, String inventoryId) {
//        Inventory inventory = inventoryRepository.findInventoryByInventoryId(inventoryId);
//        return productRequestDTOMono
//                .publishOn(Schedulers.boundedElastic())
//                .flatMap(requestDTO -> {
//                    if (requestDTO.getProductName() == null || requestDTO.getProductPrice() == null || requestDTO.getProductQuantity() == null) {
//                        return Mono.error(new InvalidInputException("Product must have an inventory id, product name, product price, and product quantity."));
//                    } else if (requestDTO.getProductPrice() < 0 || requestDTO.getProductQuantity() < 0) {
//                        return Mono.error(new InvalidInputException("Product price and quantity must be greater than 0."));
//                    } else if (inventory == null) {
//                        return Mono.error(new NotFoundException("Inventory not found with id: " + inventoryId));
//                    } else {
//                        return generateUniqueSku()
//                                .flatMap(sku ->{
//                                    Product productEntity = EntityDTOUtil.toProductEntity(requestDTO);
//                                    productEntity.setSku(sku);
//                                    return productRepository.insert(productEntity)
//                                            .map(EntityDTOUtil::toProductResponseDTO);
//                                });
//                    }
//                }).switchIfEmpty(Mono.error(new InvalidInputException("Unable to save product to repository, an error occurred.")));
//    }
@Override
public Mono<ProductResponseDTO> addProductToInventory(Mono<ProductRequestDTO> productRequestDTOMono, String inventoryId) {
    return productRequestDTOMono
            .publishOn(Schedulers.boundedElastic())
            .flatMap(requestDTO -> inventoryRepository.findInventoryByInventoryId(inventoryId)
                    .switchIfEmpty(Mono.error(new NotFoundException("Inventory not found with id: " + inventoryId)))
                    .flatMap(inventory -> {
                        if (requestDTO.getProductName() == null || requestDTO.getProductPrice() == null || requestDTO.getProductQuantity() == null) {
                            return Mono.error(new InvalidInputException("Product must have an inventory id, product name, product price, and product quantity."));
                        } else if (requestDTO.getProductPrice() < 0 || requestDTO.getProductQuantity() < 0) {
                            return Mono.error(new InvalidInputException("Product price and quantity must be greater than 0."));
                        } else {
                            Product product = EntityDTOUtil.toProductEntity(requestDTO);
                            product.setInventoryId(inventoryId);
                            product.setProductId(EntityDTOUtil.generateUUID());
                            return productRepository.insert(product)
                                    .map(EntityDTOUtil::toProductResponseDTO);
                        }
                    }))
            .switchIfEmpty(Mono.error(new InvalidInputException("Unable to save product to the repository, an error occurred.")));
}

    @Override
    public Mono<ProductResponseDTO> updateProductInInventory(Mono<ProductRequestDTO> productRequestDTOMono, String inventoryId, String productId) {
        return productRequestDTOMono
                .publishOn(Schedulers.boundedElastic())
                .flatMap(requestDTO -> inventoryRepository.findInventoryByInventoryId(inventoryId)
                        .switchIfEmpty(Mono.error(new NotFoundException("Inventory not found with id: " + inventoryId)))
                        .flatMap(inventory -> {
                            if (requestDTO.getProductName() == null || requestDTO.getProductPrice() == null || requestDTO.getProductQuantity() == null) {
                                return Mono.error(new InvalidInputException("Product must have an inventory id, product name, product price, and product quantity."));
                            } else if (requestDTO.getProductPrice() < 0 || requestDTO.getProductQuantity() < 0) {
                                return Mono.error(new InvalidInputException("Product price and quantity must be greater than 0."));
                            } else {
                                return productRepository.findProductByProductId(productId)
                                        .flatMap(existingProduct -> {
                                            if (!existingProduct.getInventoryId().equals(inventoryId)) {
                                                return Mono.error(new NotFoundException("Product not found in the specified inventory."));
                                            }
                                            Product updatedProduct = EntityDTOUtil.toProductEntity(requestDTO);
                                            updatedProduct.setProductId(existingProduct.getProductId());
                                            updatedProduct.setInventoryId(existingProduct.getInventoryId());

                                            return productRepository.save(updatedProduct)
                                                    .map(EntityDTOUtil::toProductResponseDTO);
                                        })
                                        .switchIfEmpty(Mono.error(new NotFoundException("Product not found with id: " + productId)));
                            }
                        }))
                .switchIfEmpty(Mono.error(new InvalidInputException("Unable to update product in the repository, an error occurred.")));
    }

}
