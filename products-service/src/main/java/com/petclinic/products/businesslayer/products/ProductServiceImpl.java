package com.petclinic.products.businesslayer.products;

import com.petclinic.products.datalayer.products.*;
import com.petclinic.products.domainclientlayer.CartClient;
import com.petclinic.products.presentationlayer.products.*;
import com.petclinic.products.utils.exceptions.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.petclinic.products.datalayer.ratings.Rating;
import com.petclinic.products.datalayer.ratings.RatingRepository;
import com.petclinic.products.utils.EntityModelUtil;
import com.petclinic.products.utils.exceptions.InvalidInputException;
import lombok.extern.slf4j.Slf4j;
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
    private final CartClient cartClient;

    public ProductServiceImpl(ProductRepository productRepository, RatingRepository ratingRepository
    , ProductBundleRepository productBundleRepository, ProductBundleService productBundleService, CartClient cartClient) {
    private final ProductTypeRepository productTypeRepository;

    public ProductServiceImpl(ProductRepository productRepository, RatingRepository ratingRepository
    , ProductBundleRepository productBundleRepository, ProductBundleService productBundleService, ProductTypeRepository productTypeRepository) {
        this.productRepository = productRepository;
        this.ratingRepository = ratingRepository;
        this.productBundleRepository = productBundleRepository;
        this.productBundleService = productBundleService;
        this.cartClient = cartClient;
        this.productTypeRepository = productTypeRepository;
    }

    private Mono<Product> getAverageRating(Product product) {
        return ratingRepository.findRatingsByProductId(product.getProductId())
                .map(Rating::getRating)
                .collectList()
                .flatMap(ratings -> {
                    if(ratings.isEmpty()){
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
    public Flux<ProductResponseModel> getAllProducts(Double minPrice, Double maxPrice, Double minRating, Double maxRating, String sort,String deliveryType, String productType) {
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
                .filter(product->{
                    if(productType == null || productType.trim().isEmpty()){
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
    public Mono<ProductResponseModel> getProductByProductId(String productId) {
        return productRepository.findProductByProductId(productId)
                .switchIfEmpty(Mono.defer(() -> Mono.error(new NotFoundException("Product id was not found: " + productId))))
                .flatMap(this::getAverageRating)
                .map(EntityModelUtil::toProductResponseModel);
    }

    @Override
    public Mono<ProductResponseModel> addProduct(Mono<ProductRequestModel> productRequestModel) {
        return productRequestModel
                .filter(product -> product.getProductSalePrice() > 0)
                .switchIfEmpty(Mono.error(new InvalidAmountException("Product sale price must be greater than 0")))
                .map(request -> {

                    Product product = EntityModelUtil.toProductEntity(request);


                    LocalDate today = LocalDate.now();
                    if (product.getReleaseDate() != null && product.getReleaseDate().isAfter(today)) {
                        product.setProductStatus(ProductStatus.PRE_ORDER);
                    } else {
                        product.setProductStatus(ProductStatus.AVAILABLE);
                    }

                    return product;
                })
                .flatMap(this::getAverageRating)
                .flatMap(productRepository::save)
                .map(EntityModelUtil::toProductResponseModel);
    }

    @Override
    public Mono<ProductResponseModel> updateProductByProductId(String productId, Mono<ProductRequestModel> productRequestModel) {
        return productRepository.findProductByProductId(productId)
                .switchIfEmpty(Mono.defer(() -> Mono.error(new NotFoundException("Product id was not found: " + productId))))
                .flatMap(found -> productRequestModel
                        .map(EntityModelUtil::toProductEntity)
                        .doOnNext(entity -> entity.setId(found.getId()))
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
                                            .then(ratingRepository.deleteRatingsByProductId(found.getProductId()).then())
                                            .then(productRepository.delete(found))
                                            .thenReturn(found);
                                })
                )
                .map(EntityModelUtil::toProductResponseModel)
                .flatMap(resp ->
                        cartClient.purgeProductFromAllCarts(resp.getProductId())
                                .doOnSubscribe(sub -> log.info("[ProductService] Notifying cart-service for product deletion {}", resp.getProductId()))
                                .doOnSuccess(v -> log.info("[ProductService] Product {} purged from all carts", resp.getProductId()))
                                .doOnError(e -> log.warn("[ProductService] Failed to purge product {} from carts: {}", resp.getProductId(), e.getMessage()))
                                .onErrorResume(e -> Mono.empty())
                                .thenReturn(resp)
                );
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
    public Mono<ProductEnumsResponseModel> getProductsEnumValues(){
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

    }
