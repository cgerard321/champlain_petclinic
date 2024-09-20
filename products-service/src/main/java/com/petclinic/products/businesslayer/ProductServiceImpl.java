package com.petclinic.products.businesslayer;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;


import com.petclinic.products.utils.EntityModelUtil;
import com.petclinic.products.datalayer.ProductRepository;
import com.petclinic.products.presentationlayer.ProductRequestModel;
import com.petclinic.products.presentationlayer.ProductResponseModel;
import com.petclinic.products.utils.exceptions.NotFoundException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ProductServiceImpl implements ProductService{



    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public Flux<ProductResponseModel> getAllProducts() {
        return productRepository.findAll()
                .map(EntityModelUtil::toProductResponseModel);
    }

    @Override
    public Mono<ProductResponseModel> getProductByProductId(String productId) {
        return productRepository.findProductByProductId(productId)
                .switchIfEmpty(Mono.defer(() -> Mono.error(new NotFoundException("Product id was not found: " + productId))))
                .map(EntityModelUtil::toProductResponseModel);
    }

    @Override
    public Mono<ProductResponseModel> addProduct(Mono<ProductRequestModel> productRequestModel) {
        return productRequestModel
                .map(EntityModelUtil::toProductEntity)
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
                .flatMap(productRepository::save)
                .map(EntityModelUtil::toProductResponseModel);
    }

    @Override
    public Mono<ProductResponseModel> deleteProductByProductId(String productId) {
        return productRepository.findProductByProductId(productId)
                .switchIfEmpty(Mono.defer(() -> Mono.error(new NotFoundException("Product id was not found: " + productId))))
                .flatMap(found -> productRepository.delete(found)
                        .then(Mono.just(found)))
                .map(EntityModelUtil::toProductResponseModel);
    }


    @Override
    public Mono<Void> requestCount(String productId) {
        return productRepository.findProductByProductId(productId)
                .switchIfEmpty(Mono.defer(() -> Mono.error(new NotFoundException("Product id was not found: " + productId))))
                .flatMap(product -> {
                    product.setRequestCount(product.getRequestCount() + 1);
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

}
