package com.petclinic.products.businesslayer.products;
import com.petclinic.products.datalayer.products.Product;
import com.petclinic.products.datalayer.products.ProductBundle;
import com.petclinic.products.datalayer.products.ProductBundleRepository;
import com.petclinic.products.datalayer.products.ProductRepository;
import com.petclinic.products.presentationlayer.products.ProductBundleRequestModel;
import com.petclinic.products.presentationlayer.products.ProductBundleResponseModel;
import com.petclinic.products.utils.EntityModelUtil;
import com.petclinic.products.utils.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
@Service
@RequiredArgsConstructor
public class ProductBundleServiceImpl implements ProductBundleService {
    private final ProductBundleRepository bundleRepository;
    private final ProductRepository productRepository;
    @Override
    public Flux<ProductBundleResponseModel> getAllProductBundles() {
        return bundleRepository.findAll()
                .map(EntityModelUtil::toProductBundleResponseModel);
    }
    @Override
    public Mono<ProductBundleResponseModel> getProductBundleById(String bundleId) {
        return bundleRepository.findByBundleId(bundleId)
                .switchIfEmpty(Mono.error(new NotFoundException("Bundle not found: " + bundleId)))
                .map(EntityModelUtil::toProductBundleResponseModel);
    }
    @Override
    public Mono<ProductBundleResponseModel> createProductBundle(Mono<ProductBundleRequestModel> requestModel) {
        return requestModel
                .flatMap(request -> {
                    // Calculate original total price
                    return productRepository.findAllById(request.getProductIds())
                            .collectList()
                            .flatMap(products -> {
                                if (products.size() != request.getProductIds().size()) {
                                    return Mono.error(new NotFoundException("One or more products not found"));
                                }
                                double originalTotal = products.stream()
                                        .mapToDouble(Product::getProductSalePrice)
                                        .sum();
                                ProductBundle bundle = ProductBundle.builder()
                                        .bundleId(java.util.UUID.randomUUID().toString())
                                        .bundleName(request.getBundleName())
                                        .bundleDescription(request.getBundleDescription())
                                        .productIds(request.getProductIds())
                                        .originalTotalPrice(originalTotal)
                                        .bundlePrice(request.getBundlePrice())
                                        .build();
                                return bundleRepository.save(bundle);
                            });
                })
                .map(EntityModelUtil::toProductBundleResponseModel);
    }
    @Override
    public Mono<ProductBundleResponseModel> updateProductBundle(String bundleId, Mono<ProductBundleRequestModel> requestModel) {
        return bundleRepository.findByBundleId(bundleId)
                .switchIfEmpty(Mono.error(new NotFoundException("Bundle not found: " + bundleId)))
                .flatMap(existingBundle -> requestModel
                        .flatMap(request -> {
                            existingBundle.setBundleName(request.getBundleName());
                            existingBundle.setBundleDescription(request.getBundleDescription());
                            existingBundle.setProductIds(request.getProductIds());
                            existingBundle.setBundlePrice(request.getBundlePrice());
                            // Recalculate original total price
                            return productRepository.findAllById(request.getProductIds())
                                    .collectList()
                                    .flatMap(products -> {
                                        if (products.size() != request.getProductIds().size()) {
                                            return Mono.error(new NotFoundException("One or more products not found"));
                                        }
                                        double originalTotal = products.stream()
                                                .mapToDouble(Product::getProductSalePrice)
                                                .sum();
                                        existingBundle.setOriginalTotalPrice(originalTotal);
                                        return bundleRepository.save(existingBundle);
                                    });
                        }))
                .map(EntityModelUtil::toProductBundleResponseModel);
    }
    @Override
    public Mono<Void> deleteProductBundle(String bundleId) {
        return bundleRepository.findByBundleId(bundleId)
                .switchIfEmpty(Mono.error(new NotFoundException("Bundle not found: " + bundleId)))
                .flatMap(bundleRepository::delete);
    }

    @Override
    public Flux<ProductBundleResponseModel> deleteAllProductBundlesByProductId(String productId) {
        return bundleRepository.findAllByProductIdsContaining(productId)
                .switchIfEmpty(Flux.error(new NotFoundException("No Bundles found with product: " + productId)))
                .collectList()
                .flatMapMany(bundles -> {
                    if (bundles.isEmpty()) { return Flux.empty(); }

                    var deletedBundles = bundles.stream()
                            .map(EntityModelUtil::toProductBundleResponseModel)
                            .toList();

                    return bundleRepository.deleteAll(bundles)
                            .thenMany(Flux.fromIterable(deletedBundles));
                });
    }
}
