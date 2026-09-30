package com.petclinic.products.businesslayer.products;

import com.petclinic.products.datalayer.products.*;
import com.petclinic.products.domainclientlayer.FileRequestDTO;
import com.petclinic.products.domainclientlayer.FileResponseDTO;
import com.petclinic.products.domainclientlayer.FilesServiceClient;
import org.springframework.beans.factory.annotation.Value;
import com.petclinic.products.datalayer.ratings.Rating;
import com.petclinic.products.datalayer.ratings.RatingRepository;
import com.petclinic.products.presentationlayer.products.*;
import com.petclinic.products.utils.EntityModelUtil;
import com.petclinic.products.utils.exceptions.InvalidAmountException;
import com.petclinic.products.utils.exceptions.InvalidInputException;
import com.petclinic.products.utils.exceptions.FailedDependencyException;
import com.petclinic.products.utils.exceptions.FileNotFoundInFilesServiceException;
import com.petclinic.products.utils.exceptions.NotFoundException;
import com.petclinic.products.utils.exceptions.ProductInBundleConflictException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    private final RatingRepository ratingRepository;
    private final ProductBundleRepository productBundleRepository;
    private final ProductBundleService productBundleService;
    private final ProductTypeRepository productTypeRepository;
    private final FilesServiceClient filesServiceClient;

    @Value("${app.files-service.default-image-ids:}")
    private String defaultImageIds = "";

    public ProductServiceImpl(ProductRepository productRepository, RatingRepository ratingRepository,
                              ProductBundleRepository productBundleRepository,
                              ProductBundleService productBundleService,
                              ProductTypeRepository productTypeRepository,
                              FilesServiceClient filesServiceClient) {
        this.productRepository = productRepository;
        this.ratingRepository = ratingRepository;
        this.productBundleRepository = productBundleRepository;
        this.productBundleService = productBundleService;
        this.productTypeRepository = productTypeRepository;
        this.filesServiceClient = filesServiceClient;
    }

    private Mono<Product> getAverageRating(Product product) {
        return ratingRepository.findRatingsByProductId(product.getProductId())
                .map(Rating::getRating)
                .collectList()
                .flatMap(ratings -> {
                    if (ratings.isEmpty()) {
                        product.setAverageRating(0.0);
                        return Mono.just(product);
                    }

                    Double sumOfRatings = ratings
                            .stream()
                            .mapToDouble(Byte::doubleValue)
                            .sum();
                    Double ratio = sumOfRatings / ratings.size();
                    // This sets truncates to 2 decimal places without converting data types
                    product.setAverageRating(Math.floor(ratio * 100) / 100);
                    return Mono.just(product);
                });
    }

    @Override
    public Flux<ProductResponseModel> getAllProducts(Double minPrice, Double maxPrice, Double minRating, Double maxRating, String sort, String deliveryType, String productType) {
        if (sort != null && !Arrays.asList("asc", "desc", "default").contains(sort.toLowerCase())) {
            throw new InvalidInputException("Invalid sort parameter: " + sort);
        }
        Flux<Product> products;

        if (minPrice != null && maxPrice != null) {
            products = productRepository.findByProductSalePriceBetween(minPrice, maxPrice);
        } else if (minPrice != null) {
            products = productRepository.findByProductSalePriceGreaterThanEqual(minPrice);
        } else if (maxPrice != null) {
            products = productRepository.findByProductSalePriceLessThanEqual(maxPrice);
        } else {
            products = productRepository.findAll();
        }

        return products
                .flatMap(this::getAverageRating)
                .filter(product -> {
                    double avgRating = product.getAverageRating();
                    boolean meetsMinRating = (minRating == null || avgRating >= minRating);
                    boolean meetsMaxRating = (maxRating == null || avgRating <= maxRating);
                    return meetsMinRating && meetsMaxRating;
                })
                .filter(product -> {
                    if (deliveryType == null || deliveryType.trim().isEmpty()) {
                        return true;
                    } else {
                        return product.getDeliveryType().toString().equalsIgnoreCase(deliveryType);
                    }
                })
                //Filter productType
                .filter(product -> {
                    if (productType == null || productType.trim().isEmpty()) {
                        return true;
                    }
                    return product.getProductType().toString().equalsIgnoreCase(productType);
                })
                .collectList()
                .flatMapMany(productList -> {
                    if ("asc".equals(sort)) {
                        productList.sort((p1, p2) -> Double.compare(p1.getAverageRating(), p2.getAverageRating()));
                    } else if ("desc".equals(sort)) {
                        productList.sort((p1, p2) -> Double.compare(p2.getAverageRating(), p1.getAverageRating()));
                    }
                    return Flux.fromIterable(productList);
                })
                .map(EntityModelUtil::toProductResponseModel);
    }


    @Override
    public Mono<ProductResponseModel> getProductByProductId(
            String productId, boolean includeImage) {

        return productRepository.findProductByProductId(productId)
                .switchIfEmpty(Mono.error(
                        new NotFoundException(
                                "Product id was not found: " + productId)))
                .flatMap(this::getAverageRating)
                .flatMap(product -> {
                    ProductResponseModel response =
                            EntityModelUtil.toProductResponseModel(product);

                    if (!includeImage
                            || product.getImageId() == null
                            || product.getImageId().isBlank()) {
                        return Mono.just(response);
                    }

                    return filesServiceClient.getFile(product.getImageId())
                            .map(file -> {
                                response.setImage(file);
                                return response;
                            })
                            .switchIfEmpty(Mono.fromSupplier(() -> {
                                log.warn("Files Service returned no image for product {}",
                                        productId);
                                return response;
                            }))
                            .onErrorResume(error -> {
                                log.warn("Unable to load image for product {}; "
                                                + "returning product without image",
                                        productId, error);
                                return Mono.just(response);
                            });
                });
    }

    @Override
    public Mono<ProductResponseModel> includeImage(ProductResponseModel product) {
        if (product.getImageId() == null || product.getImageId().isBlank()) {
            return Mono.just(product);
        }

        return filesServiceClient.getFile(product.getImageId())
                .map(file -> {
                    product.setImage(file);
                    return product;
                })
                .switchIfEmpty(Mono.fromSupplier(() -> {
                    log.warn("Files Service returned no image for product {}",
                            product.getProductId());
                    return product;
                }))
                .onErrorResume(error -> {
                    log.warn("Unable to load image for product {}; "
                                    + "returning product without image",
                            product.getProductId(), error);
                    return Mono.just(product);
                });
    }

    @Override
    public Mono<ProductResponseModel> addProduct(
            Mono<ProductRequestModel> productRequestModel) {

        return productRequestModel
                .filter(request -> request.getProductSalePrice() != null
                        && request.getProductSalePrice() > 0)
                .switchIfEmpty(Mono.error(new InvalidAmountException(
                        "Product sale price must be greater than 0")))
                .flatMap(request -> {
                    Product product = EntityModelUtil.toProductEntity(request);

                    // Only trust an ID returned by the Files Service.
                    product.setImageId(null);

                    LocalDate today = LocalDate.now();
                    product.setProductStatus(
                            product.getReleaseDate() != null
                                    && product.getReleaseDate().isAfter(today)
                                    ? ProductStatus.PRE_ORDER
                                    : ProductStatus.AVAILABLE);

                    // Creating a product without an image still works.
                    if (request.getImage() == null) {
                        return getAverageRating(product)
                                .flatMap(productRepository::save)
                                .map(EntityModelUtil::toProductResponseModel);
                    }

                    return filesServiceClient.addFile(request.getImage())
                            .switchIfEmpty(Mono.error(
                                    new FailedDependencyException(
                                            "Files Service returned no file")))
                            .flatMap(file -> {
                                product.setImageId(file.getFileId());

                                return getAverageRating(product)
                                        .flatMap(productRepository::save)
                                        .onErrorResume(saveError ->
                                                filesServiceClient
                                                        .deleteFile(file.getFileId())
                                                        .onErrorResume(cleanupError -> {
                                                            log.error(
                                                                    "Failed to clean up file {}",
                                                                    file.getFileId(),
                                                                    cleanupError);
                                                            saveError.addSuppressed(
                                                                    cleanupError);
                                                            return Mono.empty();
                                                        })
                                                        .then(Mono.<Product>error(
                                                                saveError)))
                                        .map(savedProduct -> {
                                            ProductResponseModel response =
                                                    EntityModelUtil
                                                            .toProductResponseModel(
                                                                    savedProduct);
                                            response.setImage(file);
                                            return response;
                                        });
                            });
                });
    }

    @Override
    public Mono<ProductResponseModel> updateProductByProductId(String productId, Mono<ProductRequestModel> productRequestModel) {
        return productRepository.findProductByProductId(productId)
                .switchIfEmpty(Mono.defer(() -> Mono.error(new NotFoundException("Product id was not found: " + productId))))
                .flatMap(found -> productRequestModel
                        .map(EntityModelUtil::toProductEntity)
                        .doOnNext(entity -> entity.setId(found.getId()))
                        .doOnNext(entity -> entity.setImageId(found.getImageId()))
                        .doOnNext(entity -> entity.setProductId(found.getProductId())))
                .flatMap(this::getAverageRating)
                .flatMap(productRepository::save)
                .map(EntityModelUtil::toProductResponseModel);
    }

    @Override
    public Mono<ProductResponseModel> patchListingStatus(String productId, Mono<ProductRequestModel> productRequestModel) {
        return productRepository.findProductByProductId(productId)
                .switchIfEmpty(Mono.defer(() -> Mono.error(new NotFoundException("Product id was not found: " + productId))))
                .flatMap(found -> productRequestModel
                        .doOnNext(request -> {
                            found.setIsUnlisted(request.getIsUnlisted());
                        })
                        .thenReturn(found)
                )
                .flatMap(productRepository::save)
                .map(EntityModelUtil::toProductResponseModel);
    }

    @Override
    public Mono<ProductResponseModel> deleteProductByProductId(String productId, boolean cascadeBundles) {
        return productRepository.findProductByProductId(productId)
                .switchIfEmpty(Mono.error(new NotFoundException("Product id was not found: " + productId)))
                .flatMap(found ->
                        productBundleRepository.findAllByProductIdsContaining(found.getProductId())
                                .collectList()
                                .flatMap(bundles -> {
                                    if (!bundles.isEmpty() && !cascadeBundles) {
                                        var bundlesDelete = bundles.stream()
                                                .map(EntityModelUtil::toProductBundleResponseModel)
                                                .toList();
                                        return Mono.error(new ProductInBundleConflictException("Bundles were found: " + bundlesDelete));
                                    }

                                    Mono<Void> deleteBundles = bundles.isEmpty()
                                            ? Mono.empty()
                                            : productBundleService.deleteAllProductBundlesByProductId(found.getProductId()).then();

                                    return deleteBundles
                                            .then(ratingRepository.deleteRatingsByProductId(
                                                    found.getProductId()).then())
                                            // Clean up the owned image before deleting the product.
                                            .then(Mono.defer(() -> deleteOwnedProductImage(found)))
                                            .then(productRepository.delete(found))
                                            .thenReturn(found);
                                })
                )
                .map(EntityModelUtil::toProductResponseModel);
    }


    @Override
    public Mono<Void> requestCount(String productId) {
        return productRepository.findProductByProductId(productId)
                .switchIfEmpty(Mono.defer(() -> Mono.error(new NotFoundException("Product id was not found: " + productId))))
                .flatMap(product -> {
                    Integer currentCount = product.getRequestCount() != null ? product.getRequestCount() : 0;
                    product.setRequestCount(currentCount + 1);
                    return productRepository.save(product).then(); // Save and complete
                });
    }


    @Scheduled(cron = "0 0 0 */30 * *")  // Runs every 30 days at midnight
    public Mono<Void> resetRequestCounts() {
        return productRepository.findAll()
                .flatMap(product -> {
                    product.setRequestCount(0);
                    return productRepository.save(product);
                })
                .then();
    }

    @Override
    public Flux<ProductResponseModel> getProductsByType(String productType) {
        return productRepository.findProductsByProductType(productType)
                .map(product -> {
                    ProductResponseModel responseModel = new ProductResponseModel();
                    responseModel.setProductId(product.getProductId());
                    responseModel.setProductName(product.getProductName());
                    responseModel.setProductDescription(product.getProductDescription());
                    responseModel.setProductSalePrice(product.getProductSalePrice());
                    responseModel.setProductType(product.getProductType());
                    responseModel.setImageId(product.getImageId());
                    return responseModel;
                });
    }


    @Override
    public Mono<Void> DecreaseProductCount(String productId) {
        return productRepository.findProductByProductId(productId)
                .switchIfEmpty(Mono.defer(() -> Mono.error(new NotFoundException("Product id was not found: " + productId))))
                .flatMap(product -> {
                    product.setRequestCount(product.getProductQuantity() - 1);
                    return productRepository.save(product).then();
                });
    }

    @Override
    public Mono<Void> changeProductQuantity(String productId, Integer productQuantity) {
        return productRepository.findProductByProductId(productId)
                .switchIfEmpty(Mono.defer(() -> Mono.error(new NotFoundException("Product id was not found: " + productId))))
                .flatMap(product -> {
                    product.setProductQuantity(productQuantity);
                    return productRepository.save(product).then();
                });
    }


    @Override
    public List<Product> getProductsByType(ProductType productType) {
        return productRepository.findByProductType(productType);
    }


    @Scheduled(cron = "0 0 0 * * ?") // Runs daily at midnight
    public Mono<Void> patchProductStatus() {
        return productRepository.findAll()
                .flatMap(existingProduct -> {
                    LocalDate today = LocalDate.now();

                    if (existingProduct.getReleaseDate() != null && existingProduct.getReleaseDate().isAfter(today)) {
                        existingProduct.setProductStatus(ProductStatus.PRE_ORDER);
                    } else {
                        existingProduct.setProductStatus(ProductStatus.AVAILABLE);
                    }

                    return productRepository.save(existingProduct);
                })
                .then();
    }

    @Override
    public Mono<ProductEnumsResponseModel> getProductsEnumValues() {
        ProductEnumsResponseModel response = ProductEnumsResponseModel.builder()
                .productStatus(Arrays.asList(ProductStatus.values()))
                .productType(Arrays.asList(ProductType.values()))
                .deliveryType(Arrays.asList(DeliveryType.values()))
                .build();

        return Mono.just(response);
    }

    @Override
    public Flux<ProductTypeResponseModel> getAllProductTypes() {
        return productTypeRepository.findAll()
                .map(EntityModelUtil::toProductTypeResponseModel);
    }


    @Override
    public Mono<ProductTypeResponseModel> getProductTypeByProductTypeId(String productTypeId) {
        return productTypeRepository.findByProductTypeId(productTypeId)
                .switchIfEmpty(Mono.defer(() -> Mono.error(new NotFoundException("ProductType id was not found: " + productTypeId))))
                .map(EntityModelUtil::toProductTypeResponseModel);
    }

    @Override
    public Mono<ProductTypeResponseModel> addProductType(Mono<ProductTypeRequestModel> productTypeRequestModel) {
        return productTypeRequestModel
                .map(request -> {
                    request.setTypeName(request.getTypeName().toUpperCase());
                    return EntityModelUtil.toProductTypeEntity(request);
                })
                .flatMap(productTypeRepository::save)
                .map(EntityModelUtil::toProductTypeResponseModel);
    }

    @Override
    public Mono<ProductTypeResponseModel> updateProductTypeByProductTypeId(String productTypeId, Mono<ProductTypeRequestModel> productTypeRequestModel) {
        return productTypeRepository.findByProductTypeId(productTypeId)
                .switchIfEmpty(Mono.defer(() -> Mono.error(new NotFoundException("ProductType id was not found: " + productTypeId))))
                .flatMap(found -> productTypeRequestModel
                        .map(EntityModelUtil::toProductTypeEntity)
                        .map(entity -> {
                            entity.setId(found.getId());
                            entity.setProductTypeId(found.getProductTypeId());
                            entity.setTypeName(entity.getTypeName().toUpperCase());
                            return entity;
                        })
                        .flatMap(productTypeRepository::save))
                .map(EntityModelUtil::toProductTypeResponseModel);
    }

    @Override
    public Mono<ProductTypeResponseModel> deleteProductTypeByProductTypeId(String productTypeId) {
        return productTypeRepository.findByProductTypeId(productTypeId)
                .switchIfEmpty(Mono.error(new NotFoundException("ProductType id was not found: " + productTypeId)))
                .flatMap(existingProductType ->
                        productTypeRepository.delete(existingProductType)
                                .thenReturn(EntityModelUtil.toProductTypeResponseModel(existingProductType))
                );
    }

    // Allows an existing product to add or replace its image in the Files Service.
    @Override
    public Mono<ProductResponseModel> updateProductImage(
            String productId, FileRequestDTO image) {

        return productRepository.findProductByProductId(productId)
                .switchIfEmpty(Mono.error(
                        new NotFoundException(
                                "Product id was not found: " + productId)))
                .flatMap(product -> {
                    if (product.getImageId() == null
                            || product.getImageId().isBlank()
                            || isDefaultImage(product.getImageId())) {
                        return uploadProductImage(product, image);
                    }

                    return productRepository.existsByImageIdAndProductIdNot(
                                    product.getImageId(), productId)
                            .flatMap(shared -> {
                                if (shared) {
                                    return uploadProductImage(product, image);
                                }

                                return filesServiceClient
                                        .updateFile(product.getImageId(), image)
                                        .switchIfEmpty(Mono.error(
                                                new FailedDependencyException(
                                                        "Files Service returned no file")))
                                        .map(file -> toResponseWithImage(product, file))
                                        .onErrorResume(
                                                FileNotFoundInFilesServiceException.class,
                                                error -> uploadProductImage(product, image));
                            });
                });
    }

    @Override
    public Mono<ProductResponseModel> deleteProductImage(String productId) {
        return productRepository.findProductByProductId(productId)
                .switchIfEmpty(Mono.error(
                        new NotFoundException(
                                "Product id was not found: " + productId)))
                .flatMap(product -> deleteOwnedProductImage(product)
                        .then(Mono.defer(() -> {
                            product.setImageId(null);
                            return productRepository.save(product);
                        })))
                .map(EntityModelUtil::toProductResponseModel);
    }

    private Mono<ProductResponseModel> uploadProductImage(
            Product product, FileRequestDTO image) {

        // Upload a new file for a product with no image or a shared image.
        return filesServiceClient.addFile(image)
                .switchIfEmpty(Mono.error(
                        new FailedDependencyException(
                                "Files Service returned no file")))
                .flatMap(file -> {
                    // Store only the returned file ID on the product.
                    product.setImageId(file.getFileId());

                    return productRepository.save(product)
                            .onErrorResume(saveError ->
                                    // Remove the uploaded file if saving fails,
                                    // so it is not left without a product reference.
                                    filesServiceClient.deleteFile(file.getFileId())
                                            .onErrorResume(cleanupError -> {
                                                // Log cleanup failure while preserving
                                                // the original save error.
                                                log.error(
                                                        "Failed to clean up file {}",
                                                        file.getFileId(),
                                                        cleanupError);
                                                saveError.addSuppressed(cleanupError);
                                                return Mono.empty();
                                            })
                                            .then(Mono.<Product>error(saveError)))
                            .map(saved -> toResponseWithImage(saved, file));
                });
    }

    private ProductResponseModel toResponseWithImage(
            Product product, FileResponseDTO file) {

        // Map the stored product fields to the response.
        ProductResponseModel response =
                EntityModelUtil.toProductResponseModel(product);

        // Include file content in the response without storing it on the entity.
        response.setImage(file);
        return response;
    }

    private Mono<Void> deleteOwnedProductImage(Product product) {
        String imageId = product.getImageId();

        // Products without an image need no file cleanup.
        if (imageId == null || imageId.isBlank() || isDefaultImage(imageId)) {
            return Mono.empty();
        }

        return productRepository
                .existsByImageIdAndProductIdNot(
                        imageId, product.getProductId())
                .flatMap(shared -> {
                    // Keep files that another product still references.
                    if (shared) {
                        return Mono.empty();
                    }

                    return filesServiceClient.deleteFile(imageId);
                });
    }

    private boolean isDefaultImage(String imageId) {
        return Arrays.stream(defaultImageIds.split(","))
                .map(String::trim)
                .filter(id -> !id.isEmpty())
                .anyMatch(imageId::equals);
    }
}
