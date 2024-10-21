package com.petclinic.products.businesslayer.products;

import com.petclinic.products.datalayer.notifications.Notification;
import com.petclinic.products.datalayer.notifications.NotificationRepository;
import com.petclinic.products.datalayer.notifications.NotificationType;
import com.petclinic.products.domainclientlayer.EmailRequestModel;
import com.petclinic.products.domainclientlayer.EmailingServiceClient;
import com.petclinic.products.domainclientlayer.UserDetails;
import com.petclinic.products.domainclientlayer.UserServiceClient;
import com.petclinic.products.presentationlayer.notifications.NotificationRequestModel;
import com.petclinic.products.datalayer.products.ProductStatus;
import com.petclinic.products.datalayer.products.ProductType;
import com.petclinic.products.utils.exceptions.InvalidInputException;
import org.springframework.http.HttpStatusCode;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.petclinic.products.datalayer.products.Product;
import com.petclinic.products.datalayer.products.ProductRepository;
import com.petclinic.products.datalayer.ratings.Rating;
import com.petclinic.products.datalayer.ratings.RatingRepository;
import com.petclinic.products.presentationlayer.products.ProductRequestModel;
import com.petclinic.products.presentationlayer.products.ProductResponseModel;
import com.petclinic.products.utils.EntityModelUtil;
import com.petclinic.products.utils.exceptions.InvalidAmountException;
import com.petclinic.products.utils.exceptions.InvalidInputException;
import com.petclinic.products.utils.exceptions.NotFoundException;
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
    private final NotificationRepository notificationRepository;

    private final EmailingServiceClient emailClient;

    public ProductServiceImpl(ProductRepository productRepository, RatingRepository ratingRepository, NotificationRepository notificationRepository, EmailingServiceClient emailClient) {
        this.productRepository = productRepository;
        this.ratingRepository = ratingRepository;
        this.notificationRepository = notificationRepository;

        this.emailClient = emailClient;
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

    private void onQuantityChange(Product product, Integer newQuantity){
        notificationRepository.findNotificationsByProductIdAndNotificationTypeContains(product.getProductId(), NotificationType.QUANTITY)
                .flatMap(notification ->
                    emailClient.sendEmail(EmailRequestModel.builder()
                            .EmailToSendTo(notification.getEmail())
                            .EmailTitle("PetClinic - Product \"" + product.getProductName() + "\" Restocked")
                            .TemplateName("Default")
                            .Header("PetClinic - Product Restocked")
                            .SenderName("PetClinicBackInStock")
                            .Body("Hi!\n" + product.getProductName() + " has been restocked!\n" + "New quantity: " + newQuantity + "\n\n")
                            .Footer("Thank you for shopping with PetClinic!")
                            .build())
                )
                .subscribe();
    }

    private void onPriceChange(Product product, Double newPrice){
        notificationRepository.findNotificationsByProductIdAndNotificationTypeContains(product.getProductId(), NotificationType.PRICE)
                .flatMap(notification ->
                        emailClient.sendEmail(EmailRequestModel.builder()
                                .EmailToSendTo(notification.getEmail())
                                .EmailTitle("PetClinic - Product \"" + product.getProductName() + "\" Price Change")
                                .TemplateName("Default")
                                .Header("PetClinic - Price Change")
                                .SenderName("PetClinicPriceChange")
                                .Body("Hi!\n" + product.getProductName() + " has had a price change!\n" + "New price: " + newPrice + "\n\n")
                                .Footer("Thank you for shopping with PetClinic!")
                                .build())
                )
                .subscribe();
    }

    @Override
    public Flux<ProductResponseModel> getAllProducts(Double minPrice, Double maxPrice, Double minRating, Double maxRating, String sort,String deliveryType) {
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
                        .doOnNext(entity -> {
                            if(entity.getProductSalePrice() < found.getProductSalePrice()){
                                log.debug("Sending onPriceChange");
                                onPriceChange(found, entity.getProductSalePrice());
                            }
                            if(entity.getProductQuantity() > found.getProductQuantity()){
                                log.debug("Sending onQuantityChange");
                                onQuantityChange(found, entity.getProductQuantity());
                            }
                        })
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
    public Mono<ProductResponseModel> deleteProductByProductId(String productId) {
        return productRepository.findProductByProductId(productId)
                .switchIfEmpty(Mono.defer(() -> Mono.error(new NotFoundException("Product id was not found: " + productId))))
                .flatMap(found -> {
                    notificationRepository.deleteNotificationsByProductId(found.getProductId());
                    ratingRepository.deleteRatingsByProductId(found.getProductId());
                    return productRepository.delete(found)
                            .then(Mono.just(found));
                    }
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
                .doOnNext(product -> {
                    if (productQuantity > product.getProductQuantity()){
                        log.debug("Sending onQuantityChange");
                        onQuantityChange(product, productQuantity);
                    }
                })
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



    }
