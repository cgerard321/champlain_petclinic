package com.petclinic.inventoryservice.businesslayer;

import com.petclinic.inventoryservice.datalayer.Inventory.InventoryRepository;
import com.petclinic.inventoryservice.datalayer.Product.Product;
import com.petclinic.inventoryservice.datalayer.Product.ProductRepository;
import com.petclinic.inventoryservice.presentationlayer.InventoryRequestDTO;
import com.petclinic.inventoryservice.presentationlayer.InventoryResponseDTO;
import com.petclinic.inventoryservice.presentationlayer.ProductRequestDTO;
import com.petclinic.inventoryservice.presentationlayer.ProductResponseDTO;
import com.petclinic.inventoryservice.utils.EntityDTOUtil;
import com.petclinic.inventoryservice.utils.exceptions.InvalidInputException;
import com.petclinic.inventoryservice.utils.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;


@Service
@RequiredArgsConstructor
public class ProductInventoryServiceImpl implements ProductInventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
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

    public Mono<InventoryResponseDTO> addInventory(Mono<InventoryRequestDTO> inventoryRequestDTO) {
        return inventoryRequestDTO
                .map(EntityDTOUtil::toInventoryEntity)
                .doOnNext(e -> {
                    if (e.getInventoryType() == null) {
                        throw new InvalidInputException("Invalid input data: inventory type cannot be blank.");
                    }
                    e.setInventoryId(EntityDTOUtil.generateUUID());
                })
                .flatMap(inventoryRepository::insert)
                .map(EntityDTOUtil::toInventoryResponseDTO);

    }


    @Override
    public Mono<InventoryResponseDTO> updateInventory(Mono<InventoryRequestDTO> inventoryRequestDTO, String inventoryId) {

        return inventoryRepository.findInventoryByInventoryId(inventoryId)
                .flatMap(existingInventory -> inventoryRequestDTO.map(requestDTO -> {

                    existingInventory.setInventoryName(requestDTO.getInventoryName());



                    existingInventory.setInventoryType(requestDTO.getInventoryType());
                    existingInventory.setInventoryDescription(requestDTO.getInventoryDescription());
                    return existingInventory;

                }))
                .switchIfEmpty(Mono.error(new NotFoundException("Inventory not found with id: " + inventoryId)))
                .flatMap(inventoryRepository::save)
                .map(EntityDTOUtil::toInventoryResponseDTO);

    }


    @Override
    public Mono<Void> deleteProductInInventory(String inventoryId, String productId) {
        return inventoryRepository.existsByInventoryId(inventoryId)
                .flatMap(invExist -> {
                    if (!invExist) {
                        return Mono.error(new NotFoundException("Inventory not found, make sure it exists, inventoryId: " + inventoryId));
                    } else {
                        return productRepository.existsByProductId(productId)
                                .flatMap(prodExist -> {
                                    if (!prodExist) {
                                        return Mono.error(new NotFoundException("Product not found, make sure it exists, productId: " + productId));
                                    } else {
                                        return productRepository.deleteByProductId(productId);
                                    }
                                });
                    }
                });


    }

    @Override
    public Flux<ProductResponseDTO> getProductsInInventoryByInventoryIdAndProductsField(String inventoryId, String productName, Double productPrice, Integer productQuantity) {

        if (productName != null && productPrice != null && productQuantity != null){
            return  productRepository
                    .findAllProductsByInventoryIdAndProductNameAndProductPriceAndProductQuantity(inventoryId,
                            productName, productPrice, productQuantity)
                    .map(EntityDTOUtil::toProductResponseDTO)
                    .switchIfEmpty(Mono.error(new NotFoundException("Inventory not found with InventoryId: " + inventoryId +
                            "\nOr ProductName: " + productName + "\nOr ProductPrice: " + productPrice + "\nOr ProductQuantity: " + productQuantity)));
        }
        if (productPrice != null && productQuantity != null){
            return productRepository
                    .findAllProductsByInventoryIdAndProductPriceAndProductQuantity(inventoryId, productPrice, productQuantity)
                    .map(EntityDTOUtil::toProductResponseDTO)
                    .switchIfEmpty(Mono.error(new NotFoundException("Inventory not found with InventoryId: " + inventoryId +
                            "\nOr ProductPrice: " + productPrice + "\nOr ProductQuantity: " + productQuantity)));
        }
        if (productPrice != null){
            return productRepository
                    .findAllProductsByInventoryIdAndProductPrice(inventoryId, productPrice)
                    .map(EntityDTOUtil::toProductResponseDTO)
                    .switchIfEmpty(Mono.error(new NotFoundException("Inventory not found with InventoryId: " + inventoryId +
                            "\nOr ProductPrice: " + productPrice)));
        }
        if (productQuantity != null){
            return productRepository
                    .findAllProductsByInventoryIdAndProductQuantity(inventoryId, productQuantity)
                    .map(EntityDTOUtil::toProductResponseDTO)
                    .switchIfEmpty(Mono.error(new NotFoundException("Inventory not found with InventoryId: " + inventoryId +
                            "\nOr ProductQuantity: " + productQuantity)));
        }
        if (productName != null){
            return productRepository
                    .findAllProductsByInventoryIdAndProductName(inventoryId, productName)
                    .map(EntityDTOUtil::toProductResponseDTO)
                    .switchIfEmpty(Mono.error(new NotFoundException("Inventory not found with InventoryId: " + inventoryId +
                            "\nOr ProductName: " + productName)));
        }

        return productRepository
                .findAllProductsByInventoryId(inventoryId)
                .map(EntityDTOUtil::toProductResponseDTO)
                .switchIfEmpty(Mono.error(new NotFoundException("Inventory not found with InventoryId: " + inventoryId)));
    }

    @Override
    public Flux<InventoryResponseDTO> getAllInventory() {
        return inventoryRepository.findAll()
                .map(EntityDTOUtil::toInventoryResponseDTO);
    }
}
