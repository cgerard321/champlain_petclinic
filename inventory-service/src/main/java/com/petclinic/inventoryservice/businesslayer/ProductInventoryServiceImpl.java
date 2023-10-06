package com.petclinic.inventoryservice.businesslayer;

import com.petclinic.inventoryservice.datalayer.Inventory.InventoryRepository;
import com.petclinic.inventoryservice.datalayer.Inventory.InventoryTypeRepository;
import com.petclinic.inventoryservice.datalayer.Product.Product;
import com.petclinic.inventoryservice.datalayer.Product.ProductRepository;
import com.petclinic.inventoryservice.presentationlayer.*;
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
    private final InventoryTypeRepository inventoryTypeRepository;

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
                                return productRepository.save(product)
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
                                            existingProduct.setProductName(requestDTO.getProductName());
                                            existingProduct.setProductDescription(requestDTO.getProductDescription());
                                            existingProduct.setProductPrice(requestDTO.getProductPrice());
                                            existingProduct.setProductQuantity(requestDTO.getProductQuantity());

                                            return productRepository.save(existingProduct)
                                                    .map(EntityDTOUtil::toProductResponseDTO);
                                        })
                                        .switchIfEmpty(Mono.error(new NotFoundException("Product not found with id: " + productId)));
                            }
                        }))
                .switchIfEmpty(Mono.error(new InvalidInputException("Unable to update product in the repository, an error occurred.")));
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
                    .findAllProductsByInventoryIdAndProductPrice(inventoryId, productPrice )
                    .map(EntityDTOUtil::toProductResponseDTO)
                    .switchIfEmpty(Mono.error(new NotFoundException("Inventory not found with InventoryId: " + inventoryId +
                            "\nOr ProductPrice: " + productPrice)));
        }
        if (productQuantity != null){
            return productRepository
                    .findAllProductsByInventoryIdAndProductQuantity(inventoryId, productQuantity )

                    .map(EntityDTOUtil::toProductResponseDTO)
                    .switchIfEmpty(Mono.error(new NotFoundException("Inventory not found with InventoryId: " + inventoryId +
                            "\nOr ProductQuantity: " + productQuantity)));
        }
        if (productName != null){
            char firstChar = productName.charAt(0);
            return productRepository
                    .findAllProductsByInventoryIdAndProductNameRegex(inventoryId,"(?i)^" + firstChar + ".*")
                    .map(EntityDTOUtil::toProductResponseDTO)
                    .switchIfEmpty(Mono.error(new NotFoundException("Inventory not found with InventoryId: " + inventoryId +
                            "\nOr ProductName: " + productName)));
        }


        return productRepository
                .findAllProductsByInventoryId(inventoryId)
                .map(EntityDTOUtil::toProductResponseDTO);
        //where the 404 not found issue lies if we switchIfEmpty

    }


    @Override
    public Mono<InventoryResponseDTO> getInventoryById(String inventoryId){
    return inventoryRepository.findInventoryByInventoryId(inventoryId)
            .switchIfEmpty(Mono.error(new NotFoundException("No inventory with this id was found" + inventoryId)))
            .map(EntityDTOUtil::toInventoryResponseDTO);

    }


    @Override
    public Mono<Void> deleteInventoryByInventoryId(String inventoryId) {
        return inventoryRepository.findInventoryByInventoryId(inventoryId)
                .switchIfEmpty(Mono.error(new RuntimeException("The InventoryId is invalid")))
                .flatMapMany(inventory -> inventoryRepository.delete(inventory))
                .then();
    }

    @Override
    public Flux<InventoryResponseDTO>searchInventories(String inventoryName, String inventoryType, String inventoryDescription) {

        if (inventoryName != null && inventoryType != null && inventoryDescription != null){
            return inventoryRepository
                    .findAllByInventoryNameAndInventoryTypeAndInventoryDescription(inventoryName, inventoryType, inventoryDescription)
                    .map(EntityDTOUtil::toInventoryResponseDTO)
                    .switchIfEmpty(Mono.error(new NotFoundException("Inventory not found with Name: " + inventoryName +
                            ", Type: " + inventoryType + ", Description: " + inventoryDescription)));
        }

        if (inventoryType != null && inventoryDescription != null){
            return inventoryRepository
                    .findAllByInventoryTypeAndInventoryDescription(inventoryType, inventoryDescription)
                    .map(EntityDTOUtil::toInventoryResponseDTO)
                    .switchIfEmpty(Mono.error(new NotFoundException("Inventory not found with Type: " + inventoryType +
                            " and Description: " + inventoryDescription)));
        }

        if (inventoryName != null){
            char firstChar1 = inventoryName.charAt(0);
            return inventoryRepository
                    .findByInventoryNameRegex("(?i)^" + firstChar1 + ".*")
                    .map(EntityDTOUtil::toInventoryResponseDTO)
                    .switchIfEmpty(Mono.error(new NotFoundException("Inventory not found with Name: " + inventoryName)));
        }

        if (inventoryType != null){
            return inventoryRepository
                    .findAllByInventoryType(inventoryType)
                    .map(EntityDTOUtil::toInventoryResponseDTO)
                    .switchIfEmpty(Mono.error(new NotFoundException("Inventory not found with Type: " + inventoryType)));
        }

        if (inventoryDescription != null){
            char firstChar = inventoryDescription.charAt(0);
            return inventoryRepository
                    .findByInventoryDescriptionRegex("(?i)^" + firstChar + ".*")
                    .map(EntityDTOUtil::toInventoryResponseDTO)
                    .switchIfEmpty(Mono.error(new NotFoundException("Inventory not found with Description: " + inventoryDescription)));
        }

        // Default - fetch all if no criteria provided.
        return inventoryRepository
                .findAll()
                .map(EntityDTOUtil::toInventoryResponseDTO);
    }



    @Override
    public Mono<ProductResponseDTO> getProductByProductIdInInventory(String inventoryId, String productId) {
     if(inventoryId == null ){
        return Mono.error(new NotFoundException("Inventory id not found:" + inventoryId ));

  }
     else if (productId == null) {
         return Mono.error(new NotFoundException("product id not found:" + productId ));
     }


        return productRepository
                .findProductByInventoryIdAndProductId(inventoryId, productId)
                .map(EntityDTOUtil::toProductResponseDTO)
                .switchIfEmpty(Mono.error(new NotFoundException("Inventory id:" + inventoryId + "and product:" + productId + "are not found")));


    }


    //delete all products and delete all inventory
    @Override
    public Mono<Void> deleteAllProductInventory (String inventoryId){
        return inventoryRepository.findInventoryByInventoryId(inventoryId)
                .switchIfEmpty(Mono.error(new RuntimeException("Invalid Inventory Id")))
                .flatMapMany(inv -> productRepository.deleteByInventoryId(inventoryId))
                .then();
    }

    @Override
    public Mono<Void> deleteAllInventory () {
        return inventoryRepository.deleteAll();

    }

    @Override
    public Mono<InventoryTypeResponseDTO> addInventoryType(Mono<InventoryTypeRequestDTO> inventoryTypeRequestDTO) {
        return inventoryTypeRequestDTO
                .map(EntityDTOUtil::toInventoryTypeEntity)
                .doOnNext(e -> {
                    e.setTypeId(EntityDTOUtil.generateUUID());
                })
                .flatMap(inventoryTypeRepository::insert)
                .map(EntityDTOUtil::toInventoryTypeResponseDTO);
    }
}

