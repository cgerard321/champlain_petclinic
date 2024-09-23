package com.petclinic.products.businesslayer.products;

import com.petclinic.products.datalayer.products.Product;
import com.petclinic.products.datalayer.ratings.Rating;
import com.petclinic.products.datalayer.ratings.RatingRepository;
import com.petclinic.products.utils.EntityModelUtil;
import com.petclinic.products.datalayer.products.ProductRepository;
import com.petclinic.products.presentationlayer.products.ProductRequestModel;
import com.petclinic.products.presentationlayer.products.ProductResponseModel;
import com.petclinic.products.utils.exceptions.InvalidAmountException;
import com.petclinic.products.utils.exceptions.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final RatingRepository ratingRepository;
    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository, RatingRepository ratingRepository) {
        this.productRepository = productRepository;
        this.ratingRepository = ratingRepository;
    }

    private Mono<Product> getAverageRating(Product product){
        return ratingRepository.findRatingsByProductId(product.getProductId())
                .map(Rating::getRating)
                .collectList()
                .flatMap(ratings -> {
                    if(ratings.isEmpty()){
                        return Mono.just(product);
                    }

                    Double sumOfRatings = ratings
                            .stream()
                            .mapToDouble(Byte::doubleValue)
                            .sum();
                    product.setAverageRating(sumOfRatings / ratings.size());
                    return Mono.just(product);
                });
    }

    @Override
    public Flux<ProductResponseModel> getAllProducts(Double minPrice, Double maxPrice) {
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
                .map(EntityModelUtil::toProductEntity)
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
    public Mono<ProductResponseModel> deleteProductByProductId(String productId) {
        return productRepository.findProductByProductId(productId)
                .switchIfEmpty(Mono.defer(() -> Mono.error(new NotFoundException("Product id was not found: " + productId))))
                .flatMap(found -> {
                    ratingRepository.deleteRatingsByProductId(found.getProductId());
                    return productRepository.delete(found)
                            .then(Mono.just(found));
                    }
      ull          )
                .map(EntityModelUtil::toProductResponseModel);
    }

}
