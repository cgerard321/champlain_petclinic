package com.petclinic.inventoryservice.businesslayer;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
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
import com.petclinic.inventoryservice.utils.exceptions.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import org.springframework.data.domain.Pageable;

import javax.security.auth.callback.TextInputCallback;
import java.io.ByteArrayOutputStream;
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
    public Mono<ProductResponseDTO> updateProductInInventory(Mono<ProductRequestDTO> productRequestDTOMono, String inventoryId, String productId) {

        return productRequestDTOMono
                .publishOn(Schedulers.boundedElastic())
                .flatMap(requestDTO ->
                        inventoryRepository.findInventoryByInventoryId(inventoryId)
                                .switchIfEmpty(Mono.error(new NotFoundException("Inventory not found with id: " + inventoryId)))
                                .flatMap(inventory ->
                                        productRepository.findProductByProductId(productId)
                                                .switchIfEmpty(Mono.error(new NotFoundException("Product not found with id: " + productId)))
                                                .flatMap(existingProduct -> {
                                            existingProduct.setProductName(requestDTO.getProductName());
                                            existingProduct.setProductDescription(requestDTO.getProductDescription());
                                            existingProduct.setProductPrice(requestDTO.getProductPrice());
                                            existingProduct.setProductQuantity(requestDTO.getProductQuantity());
                                            existingProduct.setProductSalePrice(requestDTO.getProductSalePrice());
                                            existingProduct.setLastUpdatedAt(LocalDateTime.now());

                                            // Set Status based on the product quantity
//                                            if (existingProduct.getProductQuantity() == 0) {
//                                                existingProduct.setStatus(Status.OUT_OF_STOCK);
//                                            } else if (existingProduct.getProductQuantity() < 20) {
//                                                existingProduct.setStatus(Status.RE_ORDER);
//                                            } else {
//                                                existingProduct.setStatus(Status.AVAILABLE);
//                                            }

                                            return validator.validateProductForUpdate(existingProduct, inventoryId, productId)
                                                    .flatMap(productRepository::save)
                                                    .map(EntityDTOUtil::toProductResponseDTO);
                                        })
                            )
                        )
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
    public Flux<ProductResponseDTO> getProductsInInventoryByInventoryIdAndProductsField(String inventoryId, String productName, Double productPrice, Integer productQuantity, Double productSalePrice) {

        if (productName != null && productPrice != null && productQuantity != null && productSalePrice != null) {
            return productRepository
                    .findAllProductsByInventoryIdAndProductNameAndProductPriceAndProductQuantityAndProductSalePrice(inventoryId,
                            productName, productPrice, productQuantity, productSalePrice)
                    .map(EntityDTOUtil::toProductResponseDTO)
                    .switchIfEmpty(Mono.error(new NotFoundException("Inventory not found with InventoryId: " + inventoryId +
                            "\nOr ProductName: " + productName + "\nOr ProductPrice: " + productPrice + "\nOr ProductQuantity: " + productQuantity + "\nOr ProductSalePrice: " + productSalePrice)));
        }
        if (productPrice != null && productQuantity != null) {
            return productRepository
                    .findAllProductsByInventoryIdAndProductPriceAndProductQuantity(inventoryId, productPrice, productQuantity)
                    .map(EntityDTOUtil::toProductResponseDTO)
                    .switchIfEmpty(Mono.error(new NotFoundException("Inventory not found with InventoryId: " + inventoryId +
                            "\nOr ProductPrice: " + productPrice + "\nOr ProductQuantity: " + productQuantity)));
        }
        if (productPrice != null) {
            return productRepository
                    .findAllProductsByInventoryIdAndProductPrice(inventoryId, productPrice)
                    .map(EntityDTOUtil::toProductResponseDTO)
                    .switchIfEmpty(Mono.error(new NotFoundException("Inventory not found with InventoryId: " + inventoryId +
                            "\nOr ProductPrice: " + productPrice)));
        }
        if (productQuantity != null) {
            return productRepository
                    .findAllProductsByInventoryIdAndProductQuantity(inventoryId, productQuantity)

                    .map(EntityDTOUtil::toProductResponseDTO)
                    .switchIfEmpty(Mono.error(new NotFoundException("Inventory not found with InventoryId: " + inventoryId +
                            "\nOr ProductQuantity: " + productQuantity)));
        }
        if (productSalePrice != null) {
            return productRepository
                    .findAllProductsByInventoryIdAndProductSalePrice(inventoryId, productSalePrice)

                    .map(EntityDTOUtil::toProductResponseDTO)
                    .switchIfEmpty(Mono.error(new NotFoundException("Inventory not found with InventoryId: " + inventoryId +
                            "\nOr ProductSalePrice: " + productSalePrice)));
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
    public Flux<ProductResponseDTO> getProductsInInventoryByInventoryIdAndProductsFieldsPagination(String inventoryId, String productName, Double productPrice, Integer productQuantity, Pageable pageable) {

        if (productName != null && productPrice != null && productQuantity != null) {
            return productRepository
                    .findAllProductsByInventoryIdAndProductNameAndProductPriceAndProductQuantity(inventoryId,
                            productName, productPrice, productQuantity)
                    .map(EntityDTOUtil::toProductResponseDTO)
                    .skip((long) pageable.getPageNumber() * pageable.getPageSize())
                    .take(pageable.getPageSize())
                    .switchIfEmpty(Mono.error(new NotFoundException("Inventory not found with InventoryId: " + inventoryId +
                            "\nOr ProductName: " + productName + "\nOr ProductPrice: " + productPrice + "\nOr ProductQuantity: " + productQuantity)));
        }
        if (productPrice != null && productQuantity != null) {
            return productRepository
                    .findAllProductsByInventoryIdAndProductPriceAndProductQuantity(inventoryId, productPrice, productQuantity)
                    .map(EntityDTOUtil::toProductResponseDTO)
                    .skip((long) pageable.getPageNumber() * pageable.getPageSize())
                    .take(pageable.getPageSize())
                    .switchIfEmpty(Mono.error(new NotFoundException("Inventory not found with InventoryId: " + inventoryId +
                            "\nOr ProductPrice: " + productPrice + "\nOr ProductQuantity: " + productQuantity)));
        }
        if (productPrice != null) {
            return productRepository
                    .findAllProductsByInventoryIdAndProductPrice(inventoryId, productPrice)
                    .map(EntityDTOUtil::toProductResponseDTO)
                    .skip((long) pageable.getPageNumber() * pageable.getPageSize())
                    .take(pageable.getPageSize())
                    .switchIfEmpty(Mono.error(new NotFoundException("Inventory not found with InventoryId: " + inventoryId +
                            "\nOr ProductPrice: " + productPrice)));
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
                .map(EntityDTOUtil::toProductResponseDTO)
                .switchIfEmpty(Mono.error(new NotFoundException("No products below threshold in inventory: " + inventoryId)));
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
    public Mono<ProductResponseDTO> addSupplyToInventory(Mono<ProductRequestDTO> productRequestDTOMono, String inventoryId) {
        return productRequestDTOMono
                .publishOn(Schedulers.boundedElastic())
                .flatMap(requestDTO -> inventoryRepository.findInventoryByInventoryId(inventoryId)
                        .switchIfEmpty(Mono.error(new NotFoundException("Inventory not found with id: " + inventoryId)))
                        .flatMap(inventory -> {
                            if (requestDTO.getProductName() == null || requestDTO.getProductPrice() == null || requestDTO.getProductQuantity() == null || requestDTO.getProductSalePrice() == null) {
                                return Mono.error(new InvalidInputException("Product must have an inventory id, product name, product price, and product quantity."));
                            }
                            if (requestDTO.getProductPrice() <= 0 || requestDTO.getProductQuantity() <= 0 || requestDTO.getProductSalePrice() <= 0) {
                                return Mono.error(new InvalidInputException("Product price quantity and sale price must be greater than 0."));
                            }
                            return productRepository.existsByInventoryIdAndProductNameIgnoreCase(inventoryId,requestDTO.getProductName())
                                    .flatMap(exists -> {
                                        if (exists) {
                                            return Mono.error(new UnprocessableEntityException("A product with the name '" + requestDTO.getProductName() + "' already exists in this inventory."));
                                    }

                                Product product = EntityDTOUtil.toProductEntity(requestDTO);
                                product.setInventoryId(inventoryId);
                                product.setProductId(EntityDTOUtil.generateUUID());
                                product.setLastUpdatedAt(LocalDateTime.now());
                                return productRepository.save(product)
                                        .map(EntityDTOUtil::toProductResponseDTO);
                            });
                        }))
                .switchIfEmpty(Mono.error(new InvalidInputException("Unable to save supply to the inventory, an error occurred.")));

    }

    @Override
    public Mono<ProductResponseDTO> consumeProduct(String inventoryId, String productId) {
        return productRepository.findProductByInventoryIdAndProductId(inventoryId, productId)
                .flatMap(product -> {
                    if (product.getProductQuantity() - 1 < 0) {
                        return Mono.error(new InvalidInputException("Not enough stock to consume."));
                    } else {
                        product.setProductQuantity(product.getProductQuantity() - 1);
                        product.setLastUpdatedAt(LocalDateTime.now());
                        return productRepository.save(product)
                                .map(EntityDTOUtil::toProductResponseDTO);
                    }
                })
                .switchIfEmpty(Mono.error(new NotFoundException("Product not found with id: " + productId)));
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
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        Document document = new Document();
                        PdfWriter.getInstance(document, byteArrayOutputStream);

                        document.open();
                        document.add(new Paragraph("Supplies Report for Inventory ID: " + inventoryId));
                        document.add(new Paragraph(" ")); // Blank line

                        for (Product supply : supplies) {
                            Double cost = supply.getProductPrice();
                            Double sell = supply.getProductSalePrice();
                            Double profit = sell - cost;
                            document.add(new Paragraph("Product Name: " + supply.getProductName()));
                            document.add(new Paragraph("Quantity: " + supply.getProductQuantity()));
                            document.add(new Paragraph("Description: " + supply.getProductDescription()));
                            document.add(new Paragraph("Regular Price: $" + supply.getProductPrice()));
                            document.add(new Paragraph("Sale Price: $" + supply.getProductSalePrice()));
                            document.add(new Paragraph("Profit Margin: $" + profit));
                            document.add(new Paragraph(" "));
                        }

                        document.close();

                        return Mono.just(byteArrayOutputStream.toByteArray());
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
    public Mono<ProductResponseDTO> restockLowStockProduct(String inventoryId, String productId, Integer productQuantity) {
        return productRepository.findProductByInventoryIdAndProductId(inventoryId, productId)
                .switchIfEmpty(Mono.error(new NotFoundException("Product with id: " + productId + "not found in inventory " + inventoryId)))
                .flatMap(product -> {
                    if (productQuantity <= 0) {
                        return Mono.error(new InvalidInputException("The restock quantity must be greater than 0."));
                    }

                    product.setProductQuantity(product.getProductQuantity() + productQuantity);
                    product.setLastUpdatedAt(LocalDateTime.now());

                    return productRepository.save(product)
                            .map(EntityDTOUtil::toProductResponseDTO);
                });
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

}



