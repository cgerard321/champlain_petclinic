package com.petclinic.inventoryservice.businesslayer;


import com.lowagie.text.DocumentException;
import com.mongodb.internal.connection.tlschannel.NeedsWriteException;
import com.petclinic.inventoryservice.datalayer.Inventory.Inventory;
import com.petclinic.inventoryservice.datalayer.Inventory.InventoryNameRepository;
import com.petclinic.inventoryservice.datalayer.Inventory.InventoryRepository;
import com.petclinic.inventoryservice.datalayer.Inventory.InventoryTypeRepository;
import com.petclinic.inventoryservice.datalayer.Product.Product;
import com.petclinic.inventoryservice.datalayer.Product.ProductRepository;
import com.petclinic.inventoryservice.datalayer.Product.Status;
import com.petclinic.inventoryservice.presentationlayer.*;
import com.petclinic.inventoryservice.utils.EntityDTOUtil;
import com.petclinic.inventoryservice.utils.InventoryValidator;
import com.petclinic.inventoryservice.utils.SupplyReportPdfGenerator;
import com.petclinic.inventoryservice.utils.exceptions.*;
import com.petclinic.inventoryservice.datalayer.Audit.SupplyAuditAction;
import com.petclinic.inventoryservice.datalayer.Audit.SupplyAuditEntry;
import com.petclinic.inventoryservice.datalayer.Audit.SupplyAuditRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.aggregation.AggregationUpdate;
import org.springframework.data.mongodb.core.aggregation.ArithmeticOperators;
import org.springframework.data.mongodb.core.aggregation.ConditionalOperators;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import javax.security.auth.callback.TextInputCallback;
import java.time.Instant;
import java.util.Comparator;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.petclinic.inventoryservice.utils.EntityDTOUtil.toProductEntity;


@Service
@RequiredArgsConstructor
public class ProductInventoryServiceImpl implements ProductInventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final InventoryTypeRepository inventoryTypeRepository;
    private final InventoryValidator validator;
    private final ReactiveMongoTemplate mongoTemplate;

    private final SupplyAuditRepository supplyAuditRepository;

    @Override
    public Mono<InventoryResponseDTO> addInventory(Mono<InventoryRequestDTO> inventoryRequestDTO) {
        return inventoryRepository.count()
                .flatMap(count -> inventoryRequestDTO
                        .map(EntityDTOUtil::toInventoryEntity)
                        .flatMap(validator::validateInventory)
                        .doOnNext(e -> {
                            if (e.getInventoryType() == null) {
                                throw new InvalidInputException("Invalid input data: inventory type cannot be blank.");
                            }
                            e.setInventoryId(EntityDTOUtil.generateUUID());
                            e.setInventoryCode(String.format("INV-%04d", count + 1));
                        })
                        .flatMap(inventoryRepository::insert)
                        .map(EntityDTOUtil::toInventoryResponseDTO));
    }


    @Override
    public Mono<InventoryResponseDTO> updateInventory(Mono<InventoryRequestDTO> inventoryRequestDTO, String inventoryId) {

        return inventoryRepository.findInventoryByInventoryId(inventoryId)
                .flatMap(existingInventory -> inventoryRequestDTO.map(requestDTO -> {

                    existingInventory.setInventoryName(requestDTO.getInventoryName());


                    existingInventory.setInventoryType(requestDTO.getInventoryType());
                    existingInventory.setInventoryDescription(requestDTO.getInventoryDescription());
                    existingInventory.setInventoryImage(requestDTO.getInventoryImage());
                    existingInventory.setInventoryBackupImage(requestDTO.getInventoryBackupImage());
                    existingInventory.setImageUploaded(requestDTO.getImageUploaded());
                    return existingInventory;

                }))
                .switchIfEmpty(Mono.error(new NotFoundException("Inventory not found with id: " + inventoryId)))
                .flatMap(inv -> validator.validateInventoryForUpdate(inv, inventoryId))
                .flatMap(inventoryRepository::save)
                .map(EntityDTOUtil::toInventoryResponseDTO);
    }

    @Override
    public Mono<ProductResponseDTO> updateProductInInventory(
            Mono<ProductRequestDTO> productRequestDTOMono,
            String inventoryId,
            String productId,
            String changedBy
    ) {
        return productRequestDTOMono
                .publishOn(Schedulers.boundedElastic())
                .flatMap(requestDTO ->
                        inventoryRepository.findInventoryByInventoryId(inventoryId)
                                .switchIfEmpty(Mono.error(
                                        new NotFoundException("Inventory not found with id: " + inventoryId)))
                                .flatMap(inventory ->
                                        productRepository.findProductByProductId(productId)
                                                .switchIfEmpty(Mono.error(
                                                        new NotFoundException("Product not found with id: " + productId)))
                                                .flatMap(existingProduct -> {
                                                    int previousQuantity = existingProduct.getProductQuantity();

                                                    validatePhoto(requestDTO);

                                                    if (requestDTO.getPhotoData() != null) {
                                                        boolean removePhoto = requestDTO.getPhotoData().length == 0;
                                                        existingProduct.setPhotoData(
                                                                removePhoto ? null : requestDTO.getPhotoData());
                                                        existingProduct.setPhotoType(
                                                                removePhoto ? null : requestDTO.getPhotoType());
                                                    }

                                                    existingProduct.setProductName(requestDTO.getProductName());
                                                    existingProduct.setProductDescription(requestDTO.getProductDescription());
                                                    existingProduct.setProductPrice(requestDTO.getProductPrice());
                                                    existingProduct.setProductQuantity(requestDTO.getProductQuantity());
                                                    existingProduct.setProductSalePrice(requestDTO.getProductSalePrice());
                                                    existingProduct.setLastUpdatedAt(LocalDateTime.now());
                                                    if (requestDTO.getSupplierName() != null) {
                                                        existingProduct.setSupplierName(
                                                                normalizeSupplierName(requestDTO.getSupplierName()));
                                                    }
                                                    existingProduct.setExpirationDate(requestDTO.getExpirationDate());

                                                    return validator
                                                            .validateProductForUpdate(existingProduct, inventoryId, productId)
                                                            .flatMap(productRepository::save)
                                                            .flatMap(saved -> {
                                                                if (previousQuantity == saved.getProductQuantity()) {
                                                                    return Mono.just(
                                                                            EntityDTOUtil.toProductResponseDTO(saved));
                                                                }

                                                                return recordSupplyChange(
                                                                                inventoryId,
                                                                                saved.getProductId(),
                                                                                saved.getProductName(),
                                                                                previousQuantity,
                                                                                saved.getProductQuantity(),
                                                                                SupplyAuditAction.QUANTITY_UPDATED,
                                                                                changedBy)
                                                                        .thenReturn(
                                                                                EntityDTOUtil.toProductResponseDTO(saved));
                                                            });
                                                })
                                )
                )
                .switchIfEmpty(Mono.error(new InvalidInputException(
                        "Unable to update product in the repository, an error occurred.")));
    }


    @Override
    public Mono<Void> deleteProductInInventory(
            String inventoryId,
            String productId,
            String changedBy
    ) {
        return inventoryRepository.findInventoryByInventoryId(inventoryId)
                .switchIfEmpty(Mono.error(new NotFoundException(
                        "Inventory not found, make sure it exists, inventoryId: " + inventoryId)))
                .then(productRepository.findProductByInventoryIdAndProductId(inventoryId, productId)
                        .switchIfEmpty(Mono.error(new NotFoundException(
                                "Product not found, make sure it exists, productId: " + productId)))
                        .flatMap(product ->
                                productRepository.delete(product)
                                        .then(recordSupplyChange(
                                                inventoryId,
                                                product.getProductId(),
                                                product.getProductName(),
                                                product.getProductQuantity(),
                                                0,
                                                SupplyAuditAction.DELETED,
                                                changedBy))));
    }

    @Override
    public Flux<ProductResponseDTO> getProductsInInventoryByInventoryIdAndProductsField(String inventoryId, String productName, Double minPrice, Double maxPrice, Integer productQuantity, Double minSalePrice, Double maxSalePrice) {

        Double effectiveMinPrice = minPrice != null ? minPrice : 0.0;
        Double effectiveMaxPrice = maxPrice != null ? maxPrice : Double.MAX_VALUE;
        Double effectiveMinSalePrice = minSalePrice != null ? minSalePrice : 0.0;
        Double effectiveMaxSalePrice = maxSalePrice != null ? maxSalePrice : Double.MAX_VALUE;

        if (minPrice != null || maxPrice != null) {
            Flux<Product> result = productRepository.findAllProductsByInventoryIdAndProductPriceBetween(inventoryId, effectiveMinPrice, effectiveMaxPrice);
            if (minSalePrice != null || maxSalePrice != null) {
                result = result.filter(p -> p.getProductSalePrice() >= effectiveMinSalePrice && p.getProductSalePrice() <= effectiveMaxSalePrice);
            }
            if (productQuantity != null) {
                result = result.filter(p -> p.getProductQuantity().equals(productQuantity));
            }
            if (productName != null) {
                String escapedName = Pattern.quote(productName);
                String regexPattern = "(?i)^" + escapedName + ".*";
                result = result.filter(p -> p.getProductName().matches(regexPattern));
            }
            return result.map(EntityDTOUtil::toProductResponseDTO).switchIfEmpty(Mono.error(new NotFoundException("No products found in price range")));
        }

        if (minSalePrice != null || maxSalePrice != null) {
            Flux<Product> result = productRepository.findAllProductsByInventoryIdAndProductSalePriceBetween(inventoryId, effectiveMinSalePrice, effectiveMaxSalePrice);
            if (productQuantity != null) {
                result = result.filter(p -> p.getProductQuantity().equals(productQuantity));
            }
            if (productName != null) {
                String escapedName = Pattern.quote(productName);
                String regexPattern = "(?i)^" + escapedName + ".*";
                result = result.filter(p -> p.getProductName().matches(regexPattern));
            }
            return result.map(EntityDTOUtil::toProductResponseDTO).switchIfEmpty(Mono.error(new NotFoundException("No products found in sale price range")));
        }

        if (productQuantity != null) {
            return productRepository
                    .findAllProductsByInventoryIdAndProductQuantity(inventoryId, productQuantity)

                    .map(EntityDTOUtil::toProductResponseDTO)
                    .switchIfEmpty(Mono.error(new NotFoundException("Inventory not found with InventoryId: " + inventoryId +
                            "\nOr ProductQuantity: " + productQuantity)));
        }


        if (productName != null) {
            String escapedInventoryName = Pattern.quote(productName);

            String regexPattern = "(?i)^" + escapedInventoryName + ".*";

            if (productName.length() == 1) {
                return productRepository
                        .findAllProductsByInventoryIdAndProductNameRegex(inventoryId, regexPattern)
                        .map(EntityDTOUtil::toProductResponseDTO)
                        .switchIfEmpty(Mono.error(new NotFoundException("Inventory not found with InventoryId: " + inventoryId +
                                "\nOr ProductName: " + productName)));
            } else {
                return productRepository
                        .findAllProductsByInventoryIdAndProductNameRegex(inventoryId, regexPattern)
                        .map(EntityDTOUtil::toProductResponseDTO)
                        .switchIfEmpty(Mono.error(new NotFoundException("Inventory not found with Name starting with or matching: " + productName)));
            }

        }
        return productRepository
                .findAllProductsByInventoryId(inventoryId)
                .map(EntityDTOUtil::toProductResponseDTO);
        //where the 404 not found issue lies if we switchIfEmpty

    }

    @Override
    public Flux<ProductResponseDTO> getProductsInInventoryByInventoryIdAndProductsFieldsPagination(String inventoryId, String productName, Double minPrice, Double maxPrice, Integer productQuantity, Double minSalePrice, Double maxSalePrice, Pageable pageable) {

        Double effectiveMinPrice = minPrice != null ? minPrice : 0.0;
        Double effectiveMaxPrice = maxPrice != null ? maxPrice : Double.MAX_VALUE;
        Double effectiveMinSalePrice = minSalePrice != null ? minSalePrice : 0.0;
        Double effectiveMaxSalePrice = maxSalePrice != null ? maxSalePrice : Double.MAX_VALUE;

        if (minPrice != null || maxPrice != null) {
            Flux<Product> result = productRepository.findAllProductsByInventoryIdAndProductPriceBetween(inventoryId, effectiveMinPrice, effectiveMaxPrice);
            if (minSalePrice != null || maxSalePrice != null) {
                result = result.filter(p -> p.getProductSalePrice() >= effectiveMinSalePrice && p.getProductSalePrice() <= effectiveMaxSalePrice);
            }
            if (productQuantity != null) {
                result = result.filter(p -> p.getProductQuantity().equals(productQuantity));
            }
            if (productName != null) {
                String escapedName = Pattern.quote(productName);
                String regexPattern = "(?i)^" + escapedName + ".*";
                result = result.filter(p -> p.getProductName().matches(regexPattern));
            }
            return result.map(EntityDTOUtil::toProductResponseDTO)
                    .skip((long) pageable.getPageNumber() * pageable.getPageSize())
                    .take(pageable.getPageSize())
                    .switchIfEmpty(Mono.error(new NotFoundException("No products found in price range")));
        }

        if (minSalePrice != null || maxSalePrice != null) {
            Flux<Product> result = productRepository.findAllProductsByInventoryIdAndProductSalePriceBetween(inventoryId, effectiveMinSalePrice, effectiveMaxSalePrice);
            if (productQuantity != null) {
                result = result.filter(p -> p.getProductQuantity().equals(productQuantity));
            }
            if (productName != null) {
                String escapedName = Pattern.quote(productName);
                String regexPattern = "(?i)^" + escapedName + ".*";
                result = result.filter(p -> p.getProductName().matches(regexPattern));
            }
            return result.map(EntityDTOUtil::toProductResponseDTO)
                    .skip((long) pageable.getPageNumber() * pageable.getPageSize())
                    .take(pageable.getPageSize())
                    .switchIfEmpty(Mono.error(new NotFoundException("No products found in sale price range")));
        }

        if (productQuantity != null) {
            return productRepository
                    .findAllProductsByInventoryIdAndProductQuantity(inventoryId, productQuantity)
                    .map(EntityDTOUtil::toProductResponseDTO)
                    .skip((long) pageable.getPageNumber() * pageable.getPageSize())
                    .take(pageable.getPageSize())
                    .switchIfEmpty(Mono.error(new NotFoundException("Inventory not found with InventoryId: " + inventoryId +
                            "\nOr ProductQuantity: " + productQuantity)));
        }
        if (productName != null) {
            return productRepository
                    .findAllProductsByInventoryIdAndProductName(inventoryId, productName)
                    .map(EntityDTOUtil::toProductResponseDTO)
                    .skip((long) pageable.getPageNumber() * pageable.getPageSize())
                    .take(pageable.getPageSize())
                    .switchIfEmpty(Mono.error(new NotFoundException("Inventory not found with InventoryId: " + inventoryId +
                            "\nOr ProductName: " + productName)));
        }


        return productRepository
                .findAllProductsByInventoryId(inventoryId)
                .map(EntityDTOUtil::toProductResponseDTO)
                .skip((long) pageable.getPageNumber() * pageable.getPageSize())
                .take(pageable.getPageSize());
        //where the 404 not found issue lies if we switchIfEmpty

    }


    @Override
    public Mono<InventoryResponseDTO> getInventoryById(String inventoryId) {
        return inventoryRepository.findInventoryByInventoryId(inventoryId)
                .switchIfEmpty(Mono.error(new NotFoundException("No inventory with this id was found" + inventoryId)))
                .flatMap(inventory ->
                        getRecentUpdateMessage(inventoryId)
                                .map(message -> {
                                    InventoryResponseDTO dto = EntityDTOUtil.toInventoryResponseDTO(inventory);
                                    dto.setRecentUpdateMessage(message);
                                    return dto;
                                })
                );
    }

    @Override
    public Mono<Void> deleteInventoryByInventoryId(String inventoryId) {
        return inventoryRepository.findInventoryByInventoryId(inventoryId)
                .switchIfEmpty(Mono.error(new NotFoundException("The Inventory with the inventoryId: " + inventoryId + " is invalid. Please enter a valid inventory id.")))
                .flatMapMany(inventoryRepository::delete)
                .then();
    }

    @Override
    public Mono<Void> updateImportantStatus(String inventoryId, Boolean important) {
        return inventoryRepository.findInventoryByInventoryId(inventoryId)
                .switchIfEmpty(Mono.error(new NotFoundException("Inventory not found with id: " + inventoryId)))
                .flatMap(inventory -> {
                    inventory.setImportant(important);
                    return inventoryRepository.save(inventory);
                })
                .then();
    }

    @Override
    public Flux<InventoryResponseDTO> searchInventories(Pageable page, String inventoryCode, String inventoryName, String inventoryType, String inventoryDescription, Boolean importantOnly) {

        Comparator<Inventory> byCode = (a, b) -> {
            String c1 = a.getInventoryCode();
            String c2 = b.getInventoryCode();
            if (c1 == null && c2 == null) return 0;
            if (c1 == null) return 1;
            if (c2 == null) return -1;
            try {
                int n1 = Integer.parseInt(c1.replace("INV-", ""));
                int n2 = Integer.parseInt(c2.replace("INV-", ""));
                return Integer.compare(n1, n2);
            } catch (NumberFormatException e) {
                return c1.compareTo(c2);
            }
        };

        if (inventoryCode != null && !inventoryCode.trim().isEmpty()) {
            return inventoryRepository.findInventoryByInventoryCode(inventoryCode)
                    .flatMap(inventory ->
                            getRecentUpdateMessage(inventory.getInventoryId())
                                    .defaultIfEmpty("-")
                                    .map(message -> {
                                        InventoryResponseDTO dto = EntityDTOUtil.toInventoryResponseDTO(inventory);
                                        dto.setRecentUpdateMessage(message);
                                        return dto;
                                    })
                    )
                    .flux()
                    .switchIfEmpty(Mono.error(new NotFoundException("Inventory not found with Code: " + inventoryCode)));
        }


        if (inventoryName != null && inventoryType != null && inventoryDescription != null) {
            return inventoryRepository
                    .findAllByInventoryNameAndInventoryTypeAndInventoryDescription(inventoryName, inventoryType, inventoryDescription)
                    .sort(byCode)
                    .concatMap(inventory ->
                            getRecentUpdateMessage(inventory.getInventoryId())
                                    .defaultIfEmpty("-")
                                    .map(message -> {
                                        InventoryResponseDTO dto = EntityDTOUtil.toInventoryResponseDTO(inventory);
                                        dto.setRecentUpdateMessage(message);
                                        return dto;
                                    })
                    )
                    .skip(page.getPageNumber() * page.getPageSize())
                    .take(page.getPageSize())
                    .switchIfEmpty(Mono.error(new NotFoundException("Inventory not found with Name: " + inventoryName +
                            ", Type: " + inventoryType + ", Description: " + inventoryDescription)));
        }

        if (inventoryType != null && inventoryDescription != null) {
            return inventoryRepository
                    .findAllByInventoryTypeAndInventoryDescription(inventoryType, inventoryDescription)
                    .sort(byCode)
                    .concatMap(inventory ->
                            getRecentUpdateMessage(inventory.getInventoryId())
                                    .defaultIfEmpty("-")
                                    .map(message -> {
                                        InventoryResponseDTO dto = EntityDTOUtil.toInventoryResponseDTO(inventory);
                                        dto.setRecentUpdateMessage(message);
                                        return dto;
                                    })
                    )
                    .skip(page.getPageNumber() * page.getPageSize())
                    .take(page.getPageSize())
                    .switchIfEmpty(Mono.error(new NotFoundException("Inventory not found with Type: " + inventoryType +
                            " and Description: " + inventoryDescription)));
        }
        if (inventoryName != null) {

            String escapedInventoryName = Pattern.quote(inventoryName);
            String regexPattern = "(?i)^" + escapedInventoryName + ".*";

            Flux<Inventory> base = inventoryRepository.findByInventoryNameRegex(regexPattern);

            return base
                    .sort(byCode)
                    .concatMap(inv ->
                            getRecentUpdateMessage(inv.getInventoryId())
                                    .defaultIfEmpty("—")
                                    .map(msg -> {
                                        InventoryResponseDTO dto = EntityDTOUtil.toInventoryResponseDTO(inv);
                                        dto.setRecentUpdateMessage(msg);
                                        return dto;
                                    })
                    )
                    .skip(page.getPageNumber() * page.getPageSize())
                    .take(page.getPageSize())
                    .switchIfEmpty(Mono.error(new NotFoundException(
                            (inventoryName.length() == 1)
                                    ? "Inventory not found starting with: " + inventoryName
                                    : "Inventory not found with Name starting with or matching: " + inventoryName
                    )));        }

        if (inventoryType != null /* … */) {
            return inventoryRepository
                    .findAllByInventoryType(inventoryType)
                    .sort(byCode)
                    .concatMap(inventory ->
                            getRecentUpdateMessage(inventory.getInventoryId())
                                    .defaultIfEmpty("-")
                                    .map(message -> {
                                        InventoryResponseDTO dto = EntityDTOUtil.toInventoryResponseDTO(inventory);
                                        dto.setRecentUpdateMessage(message);
                                        return dto;
                                    })
                    )
                    .skip(page.getPageNumber() * page.getPageSize())
                    .take(page.getPageSize())
                    .switchIfEmpty(Mono.error(new NotFoundException("Inventory not found with Type: " + inventoryType)));
        }

        if (inventoryDescription != null) {
            String escapedInventoryDescription = Pattern.quote(inventoryDescription);
            String regexPattern = "(?i)^" + escapedInventoryDescription + ".*";

            Flux<Inventory> base = inventoryRepository.findByInventoryDescriptionRegex(regexPattern);

            return base
                    .sort(byCode)
                    .concatMap(inv ->
                            getRecentUpdateMessage(inv.getInventoryId())
                                    .defaultIfEmpty("—")
                                    .map(msg -> {
                                        InventoryResponseDTO dto = EntityDTOUtil.toInventoryResponseDTO(inv);
                                        dto.setRecentUpdateMessage(msg);
                                        return dto;
                                    })
                    )
                    .skip(page.getPageNumber() * page.getPageSize())
                    .take(page.getPageSize())
                    .switchIfEmpty(Mono.error(new NotFoundException(
                            (inventoryDescription.length() == 1)
                                    ? "Inventory not found with Description: " + inventoryDescription
                                    : "Inventory not found with Name starting with or matching: " + inventoryName
                    )));
        }

        // Default - fetch all if no criteria provided.
        Flux<Inventory> inventoryFlux = inventoryRepository.findAll();

        if (importantOnly != null && importantOnly) {
            inventoryFlux = inventoryFlux.filter(inventory ->
                    inventory.getImportant() != null && inventory.getImportant());
        }

        return inventoryFlux
                .sort(byCode)
                .concatMap(inv ->
                        getRecentUpdateMessage(inv.getInventoryId())
                                .defaultIfEmpty("—")
                                .map(msg -> {
                                    InventoryResponseDTO dto = EntityDTOUtil.toInventoryResponseDTO(inv);
                                    dto.setRecentUpdateMessage(msg);
                                    return dto;
                                })
                )
                .skip(page.getPageNumber() * page.getPageSize())
                .take(page.getPageSize());
    }


    @Override
    public Mono<ProductResponseDTO> getProductByProductIdInInventory(String inventoryId, String productId) {
        return productRepository
                .findProductByInventoryIdAndProductId(inventoryId, productId)
                .map(EntityDTOUtil::toProductResponseDTO)
                .switchIfEmpty(Mono.error(new NotFoundException("Inventory id:" + inventoryId + "and product:" + productId + "are not found")));
    }

    @Override
    public Mono<Void> deleteAllProductsForAnInventory(String inventoryId) {
        return inventoryRepository.findInventoryByInventoryId(inventoryId)
                .switchIfEmpty(Mono.error(new NotFoundException("Invalid Inventory Id")))
                .flatMapMany(inv -> productRepository.deleteByInventoryId(inventoryId))
                .then();
    }


    @Override
    public Mono<Void> deleteAllInventory() {
        return inventoryRepository.deleteAll();

    }

    @Override
    public Mono<InventoryTypeResponseDTO> addInventoryType(Mono<InventoryTypeRequestDTO> inventoryTypeRequestDTO) {
        return inventoryTypeRequestDTO
                .map(EntityDTOUtil::toInventoryTypeEntity)
                .flatMap(validator::validateInventoryType)
                .doOnNext(e -> {
                    e.setTypeId(EntityDTOUtil.generateUUID());
                })
                .flatMap(inventoryTypeRepository::insert)
                .map(EntityDTOUtil::toInventoryTypeResponseDTO);
    }

    @Override
    public Flux<InventoryTypeResponseDTO> getAllInventoryTypes() {
        return inventoryTypeRepository.findAll()
                .map(EntityDTOUtil::toInventoryTypeResponseDTO);
    }

    @Override
    public Flux<ProductResponseDTO> getLowStockProducts(String inventoryId, int stockThreshold) {
        return productRepository
                .findAllByInventoryIdAndProductQuantityLessThan(inventoryId, stockThreshold)
                .map(EntityDTOUtil::toProductResponseDTO);
    }

    @Override
    public Flux<ProductResponseDTO> searchProducts(String inventoryId, String productName, String productDescription, Status status) {
        if (productName != null && productDescription != null && status != null) {
            return productRepository
                    .findAllProductsByInventoryIdAndProductNameAndProductDescriptionAndStatus(inventoryId, productName, productDescription, status)
                    .map(EntityDTOUtil::toProductResponseDTO)
                    .switchIfEmpty(Mono.error(new NotFoundException("Product not found with Name: " + productName +
                            ", Description: " + productDescription + ", Status: " + status)));
        }
        if (productName != null && productDescription != null) {
            return productRepository
                    .findAllProductsByInventoryIdAndProductNameAndProductDescription(inventoryId, productName, productDescription)
                    .map(EntityDTOUtil::toProductResponseDTO)
                    .switchIfEmpty(Mono.error(new NotFoundException("Product not found with Name: " + productName +
                            ", Description: " + productDescription)));
        }
        if (productName != null && status != null) {
            return productRepository
                    .findAllProductsByInventoryIdAndProductNameAndStatus(inventoryId, productName, status)
                    .map(EntityDTOUtil::toProductResponseDTO)
                    .switchIfEmpty(Mono.error(new NotFoundException("Product not found with Name: " + productName +
                            ", Status: " + status)));
        }
        if (productDescription != null && status != null) {
            return productRepository
                    .findAllProductsByInventoryIdAndProductDescriptionAndStatus(inventoryId, productDescription, status)
                    .map(EntityDTOUtil::toProductResponseDTO)
                    .switchIfEmpty(Mono.error(new NotFoundException("Product not found with Description: " + productDescription +
                            ", Status: " + status)));
        }
        if (productName != null) {
            return productRepository
                    .findAllProductsByInventoryIdAndProductName(inventoryId, productName)
                    .map(EntityDTOUtil::toProductResponseDTO)
                    .switchIfEmpty(Mono.error(new NotFoundException("Product not found with Name: " + productName)));
        }
        if (productDescription != null) {
            return productRepository
                    .findAllProductsByInventoryIdAndProductDescription(inventoryId, productDescription)
                    .map(EntityDTOUtil::toProductResponseDTO)
                    .switchIfEmpty(Mono.error(new NotFoundException("Product not found with Description: " + productDescription)));
        }
        if (status != null) {
            return productRepository
                    .findAllProductsByInventoryIdAndStatus(inventoryId, status)
                    .map(EntityDTOUtil::toProductResponseDTO)
                    .switchIfEmpty(Mono.error(new NotFoundException("Product not found with Status: " + status)));
        }

        return productRepository
                .findAllProductsByInventoryId(inventoryId)
                .map(EntityDTOUtil::toProductResponseDTO)
                .switchIfEmpty(Mono.error(new NotFoundException("Product not found with InventoryId: " + inventoryId)));
    }


    @Override
    public Mono<ProductResponseDTO> addSupplyToInventory(
            Mono<ProductRequestDTO> productRequestDTOMono,
            String inventoryId,
            String changedBy
    ) {
        return productRequestDTOMono
                .publishOn(Schedulers.boundedElastic())
                .flatMap(requestDTO ->
                        inventoryRepository.findInventoryByInventoryId(inventoryId)
                                .switchIfEmpty(Mono.error(
                                        new NotFoundException("Inventory not found with id: " + inventoryId)))
                                .flatMap(inventory -> {
                                    if (requestDTO.getProductName() == null
                                            || requestDTO.getProductPrice() == null
                                            || requestDTO.getProductQuantity() == null
                                            || requestDTO.getProductSalePrice() == null) {
                                        return Mono.error(new InvalidInputException(
                                                "Product must have an inventory id, product name, product price, and product quantity."));
                                    }

                                    if (requestDTO.getProductPrice() <= 0
                                            || requestDTO.getProductQuantity() <= 0
                                            || requestDTO.getProductSalePrice() <= 0) {
                                        return Mono.error(new InvalidInputException(
                                                "Product price, quantity and sale price must be greater than 0."));
                                    }

                                    return productRepository
                                            .existsByInventoryIdAndProductNameIgnoreCase(
                                                    inventoryId, requestDTO.getProductName())
                                            .flatMap(exists -> {
                                                if (exists) {
                                                    return Mono.error(new UnprocessableEntityException(
                                                            "A product with the name '" + requestDTO.getProductName()
                                                                    + "' already exists in this inventory."));
                                                }

                                                validatePhoto(requestDTO);

                                                Product product = EntityDTOUtil.toProductEntity(requestDTO);
                                                product.setSupplierName(normalizeSupplierName(requestDTO.getSupplierName()));
                                                if (product.getPhotoData() != null && product.getPhotoData().length == 0) {
                                                    product.setPhotoData(null);
                                                    product.setPhotoType(null);
                                                }

                                                product.setInventoryId(inventoryId);
                                                product.setProductId(EntityDTOUtil.generateUUID());
                                                product.setLastUpdatedAt(LocalDateTime.now());
                                                product.setExpirationDate(requestDTO.getExpirationDate());

                                                return productRepository.save(product)
                                                        .flatMap(saved ->
                                                                recordSupplyChange(
                                                                        inventoryId,
                                                                        saved.getProductId(),
                                                                        saved.getProductName(),
                                                                        0,
                                                                        saved.getProductQuantity(),
                                                                        SupplyAuditAction.CREATED,
                                                                        changedBy)
                                                                        .thenReturn(
                                                                                EntityDTOUtil.toProductResponseDTO(saved)));
                                            });
                                }))
                .switchIfEmpty(Mono.error(new InvalidInputException(
                        "Unable to save supply to the inventory, an error occurred.")));
    }

    @Override
    public Mono<ProductResponseDTO> consumeProduct(
            String inventoryId,
            String productId,
            String changedBy
    ) {
        return productRepository.findProductByInventoryIdAndProductId(inventoryId, productId)
                .switchIfEmpty(Mono.error(new NotFoundException(
                        "Product not found with id: " + productId)))
                .flatMap(product -> {
                    int previousQuantity = product.getProductQuantity();

                    if (previousQuantity <= 0) {
                        return Mono.error(new InvalidInputException("Not enough stock to consume."));
                    }

                    product.setProductQuantity(previousQuantity - 1);
                    product.setLastUpdatedAt(LocalDateTime.now());

                    return productRepository.save(product)
                            .flatMap(saved ->
                                    recordSupplyChange(
                                            inventoryId,
                                            saved.getProductId(),
                                            saved.getProductName(),
                                            previousQuantity,
                                            saved.getProductQuantity(),
                                            SupplyAuditAction.CONSUMED,
                                            changedBy)
                                            .thenReturn(EntityDTOUtil.toProductResponseDTO(saved)));
                });
    }

    @Override
    public Mono<Integer> getQuantityOfProductsInInventory(String inventoryId) {
        return inventoryRepository.findInventoryByInventoryId(inventoryId)
                .switchIfEmpty(Mono.error(new NotFoundException("Inventory not found with id: " + inventoryId)))
                .flatMap(inventory ->
                        productRepository.countByInventoryId(inventoryId)
                );
    }

    @Override
    public Mono<byte[]> createSupplyPdf(String inventoryId) {
        if (inventoryId == null || inventoryId.trim().isEmpty()) {
            return Mono.error(new InvalidInputException("Invalid inventory ID provided."));
        }

        return productRepository.findAllProductsByInventoryId(inventoryId)
                .collectList()
                .flatMap(supplies -> {
                    if (supplies.isEmpty()) {
                        return Mono.error(new NotFoundException("No products found for inventory ID: " + inventoryId));
                    }
                    try {
                        return Mono.just(SupplyReportPdfGenerator.generate(inventoryId, supplies));
                    } catch (DocumentException e) {
                        return Mono.error(new UnprocessableEntityException("Error generating PDF"));
                    }
                });
    }

    @Override
    public Mono<ProductResponseDTO> updateProductInventoryId(String currentInventoryId, String productId, String newInventoryId) {
        return productRepository.findProductByInventoryIdAndProductId(currentInventoryId, productId)
                .flatMap(product -> {
                    product.setInventoryId(newInventoryId);
                    return productRepository.save(product);
                })
                .map(EntityDTOUtil::toProductResponseDTO)
                .switchIfEmpty(Mono.error(new NotFoundException("Product not found with id: " + productId)));
    }

    @Override
    public Flux<InventoryResponseDTO> getAllInventories() {
        return inventoryRepository.findAll()
                .switchIfEmpty(Flux.error(new NotFoundException("No inventories found")))
                .sort((inv1, inv2) -> {
                    if (inv1.getInventoryCode() == null && inv2.getInventoryCode() == null) return 0;
                    if (inv1.getInventoryCode() == null) return 1;
                    if (inv2.getInventoryCode() == null) return -1;

                    try {
                        int num1 = Integer.parseInt(inv1.getInventoryCode().replace("INV-", ""));
                        int num2 = Integer.parseInt(inv2.getInventoryCode().replace("INV-", ""));
                        return Integer.compare(num1, num2);
                    } catch (NumberFormatException e) {
                        return inv1.getInventoryCode().compareTo(inv2.getInventoryCode());
                    }
                })
                .map(EntityDTOUtil::toInventoryResponseDTO);
    }

    @Override
    public Mono<ProductResponseDTO> restockLowStockProduct(
            String inventoryId,
            String productId,
            Integer productQuantity,
            String changedBy
    ) {
        if (productQuantity == null || productQuantity <= 0) {
            return Mono.error(new InvalidInputException(
                    "The restock quantity must be greater than 0."));
        }

        Query eligible = Query.query(Criteria.where("inventoryId").is(inventoryId)
                .and("productId").is(productId)
                .and("status").in(Status.RE_ORDER, Status.OUT_OF_STOCK)
                .and("productQuantity").gte(0)
                .lte(Integer.MAX_VALUE - productQuantity));

        AggregationUpdate update = AggregationUpdate.update()
                .set("productQuantity")
                .toValue(ArithmeticOperators.valueOf("productQuantity").add(productQuantity))
                .set("status")
                .toValue(ConditionalOperators.when(
                        Criteria.where("productQuantity").lt(EntityDTOUtil.REORDER_THRESHOLD))
                        .then(Status.RE_ORDER.name())
                        .otherwise(Status.AVAILABLE.name()))
                .set("lastUpdatedAt").toValue(LocalDateTime.now());

        return mongoTemplate.findAndModify(
                        eligible,
                        update,
                        FindAndModifyOptions.options().returnNew(true),
                        Product.class)
                .switchIfEmpty(Mono.defer(() ->
                        productRepository.findProductByInventoryIdAndProductId(inventoryId, productId)
                                .switchIfEmpty(Mono.error(new NotFoundException(
                                        "Product with id: " + productId
                                                + " not found in inventory " + inventoryId)))
                                .flatMap(product -> Mono.error(new InvalidInputException(
                                        (product.getStatus() == Status.RE_ORDER
                                                || product.getStatus() == Status.OUT_OF_STOCK)
                                                ? "The restock quantity would exceed the maximum stock quantity."
                                                : "Only RE ORDER or OUT OF STOCK supplies can be restocked. Refresh and try again.")))))
                // Audit history is written after the stock update without a transaction.
                // Propagate audit failures; the stock addition has already been applied.
                .flatMap(updated ->
                        recordSupplyChange(
                                inventoryId,
                                updated.getProductId(),
                                updated.getProductName(),
                                updated.getProductQuantity() - productQuantity,
                                updated.getProductQuantity(),
                                SupplyAuditAction.QUANTITY_UPDATED,
                                changedBy)
                                .thenReturn(EntityDTOUtil.toProductResponseDTO(updated)));
    }

    @Override
    public Flux<SupplyAuditEntry> getSupplyHistory(String inventoryId, String productId) {
        return supplyAuditRepository
                .findTop20ByInventoryIdAndSupplyIdOrderByChangedAtDesc(inventoryId, productId);
    }

    private Mono<Void> recordSupplyChange(
            String inventoryId,
            String supplyId,
            String supplyName,
            int previousQuantity,
            int newQuantity,
            SupplyAuditAction action,
            String changedBy
    ) {
        SupplyAuditEntry entry = new SupplyAuditEntry(
                UUID.randomUUID().toString(),
                inventoryId,
                supplyId,
                supplyName,
                previousQuantity,
                newQuantity,
                newQuantity - previousQuantity,
                action,
                Instant.now(),
                changedBy
        );

        return supplyAuditRepository.insert(entry).then();
    }

    @Override
    public Mono<String> getRecentUpdateMessage(String inventoryId) {
        LocalDateTime fifteenMinutesAgo = LocalDateTime.now().minusMinutes(15);

        return productRepository.countByInventoryIdAndLastUpdatedAtAfter(inventoryId, fifteenMinutesAgo)
                .map(count -> {
                    if (count == 0) {
                        return "No recent updates.";
                    } else {
                        return count + " supplies updated in the last 15 min.";
                    }
                });
    }

    private String normalizeSupplierName(String supplierName) {
        if (supplierName == null) {
            return null; // Allow older clients to create supplies without this new field.
        }

        String normalized = supplierName.strip();
        if (normalized.isEmpty()) {
            throw new InvalidInputException("Supplier name cannot be empty.");
        }
        return normalized;
    }


    private void validatePhoto(ProductRequestDTO requestDTO) {
        byte[] data = requestDTO.getPhotoData();
        String type = requestDTO.getPhotoType();

        // No photo supplied, or an empty array requesting removal.
        if (data == null || data.length == 0) {
            if (type != null && !type.isBlank()) {
                throw new InvalidInputException("Photo data is missing.");
            }
            return;
        }

        if (data.length > 2 * 1024 * 1024) {
            throw new InvalidInputException("Photo must be 2 MB or smaller.");
        }

        boolean jpg = data.length >= 3
                && data[0] == (byte) 0xFF
                && data[1] == (byte) 0xD8
                && data[2] == (byte) 0xFF;

        boolean png = data.length >= 8
                && data[0] == (byte) 0x89
                && data[1] == 0x50
                && data[2] == 0x4E
                && data[3] == 0x47
                && data[4] == 0x0D
                && data[5] == 0x0A
                && data[6] == 0x1A
                && data[7] == 0x0A;

        if (!("image/jpeg".equals(type) && jpg)
                && !("image/png".equals(type) && png)) {
            throw new InvalidInputException("Only JPG and PNG photos are supported.");
        }
    }

}
