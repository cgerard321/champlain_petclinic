package com.petclinic.products.businesslayer;

import com.petclinic.products.businesslayer.products.ProductBundleServiceImpl;
import com.petclinic.products.datalayer.products.Product;
import com.petclinic.products.datalayer.products.ProductBundle;
import com.petclinic.products.datalayer.products.ProductBundleRepository;
import com.petclinic.products.datalayer.products.ProductRepository;
import com.petclinic.products.presentationlayer.products.ProductBundleRequestModel;
import com.petclinic.products.utils.exceptions.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductBundleServiceImplUnitTest {

    @Mock
    private ProductBundleRepository bundleRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductBundleServiceImpl productBundleService;

    private ProductBundle existingBundle;
    private ProductBundleRequestModel requestModel;
    private List<String> productIds;
    private List<Product> products;

    @BeforeEach
    public void setup() {

        productIds = Arrays.asList("product1", "product2");

        products = Arrays.asList(
                Product.builder()
                        .productId("product1")
                        .productName("Product 1")
                        .productSalePrice(50.0)
                        .build(),
                Product.builder()
                        .productId("product2")
                        .productName("Product 2")
                        .productSalePrice(100.0)
                        .build()
        );

        existingBundle = ProductBundle.builder()
                .bundleId("bundle1")
                .bundleName("Bundle 1")
                .bundleDescription("Description 1")
                .productIds(productIds)
                .originalTotalPrice(150.0)
                .bundlePrice(120.0)
                .build();

        requestModel = ProductBundleRequestModel.builder()
                .bundleName("Bundle 1")
                .bundleDescription("Description 1")
                .productIds(productIds)
                .bundlePrice(120.0)
                .build();
    }

    @Test
    public void testGetProductBundleById_Found() {
        when(bundleRepository.findByBundleId("bundle1")).thenReturn(Mono.just(existingBundle));

        StepVerifier.create(productBundleService.getProductBundleById("bundle1"))
                .expectNextMatches(bundle -> bundle.getBundleId().equals("bundle1"))
                .verifyComplete();

        verify(bundleRepository, times(1)).findByBundleId("bundle1");
    }

    @Test
    public void testGetProductBundleById_NotFound() {
        when(bundleRepository.findByBundleId("bundle1")).thenReturn(Mono.empty());

        StepVerifier.create(productBundleService.getProductBundleById("bundle1"))
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException &&
                        throwable.getMessage().equals("Bundle not found: bundle1"))
                .verify();

        verify(bundleRepository, times(1)).findByBundleId("bundle1");
    }

    @Test
    public void testCreateProductBundle_Success() {
        when(productRepository.findAllById(productIds)).thenReturn(Flux.fromIterable(products));

        when(bundleRepository.save(any(ProductBundle.class))).thenAnswer(invocation -> {
            ProductBundle bundle = invocation.getArgument(0);
            bundle.setBundleId(UUID.randomUUID().toString());
            return Mono.just(bundle);
        });

        StepVerifier.create(productBundleService.createProductBundle(Mono.just(requestModel)))
                .expectNextMatches(response -> response.getBundleName().equals("Bundle 1"))
                .verifyComplete();

        verify(productRepository, times(1)).findAllById(productIds);
        verify(bundleRepository, times(1)).save(any(ProductBundle.class));
    }

    @Test
    public void testCreateProductBundle_ProductNotFound() {
        when(productRepository.findAllById(productIds)).thenReturn(Flux.just(products.get(0)));

        StepVerifier.create(productBundleService.createProductBundle(Mono.just(requestModel)))
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException &&
                        throwable.getMessage().equals("One or more products not found"))
                .verify();

        verify(productRepository, times(1)).findAllById(productIds);
        verify(bundleRepository, times(0)).save(any(ProductBundle.class));
    }

    @Test
    public void testUpdateProductBundle_Success() {
        when(bundleRepository.findByBundleId("bundle1")).thenReturn(Mono.just(existingBundle));
        when(productRepository.findAllById(productIds)).thenReturn(Flux.fromIterable(products));
        when(bundleRepository.save(any(ProductBundle.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(productBundleService.updateProductBundle("bundle1", Mono.just(requestModel)))
                .expectNextMatches(response -> response.getBundleName().equals("Bundle 1"))
                .verifyComplete();

        verify(bundleRepository, times(1)).findByBundleId("bundle1");
        verify(productRepository, times(1)).findAllById(productIds);
        verify(bundleRepository, times(1)).save(any(ProductBundle.class));
    }

    @Test
    public void testUpdateProductBundle_BundleNotFound() {
        when(bundleRepository.findByBundleId("bundle1")).thenReturn(Mono.empty());

        StepVerifier.create(productBundleService.updateProductBundle("bundle1", Mono.just(requestModel)))
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException &&
                        throwable.getMessage().equals("Bundle not found: bundle1"))
                .verify();

        verify(bundleRepository, times(1)).findByBundleId("bundle1");
        verify(productRepository, times(0)).findAllById(anyList());
        verify(bundleRepository, times(0)).save(any(ProductBundle.class));
    }

    @Test
    public void testUpdateProductBundle_ProductNotFound() {
        when(bundleRepository.findByBundleId("bundle1")).thenReturn(Mono.just(existingBundle));
        when(productRepository.findAllById(productIds)).thenReturn(Flux.just(products.get(0)));

        StepVerifier.create(productBundleService.updateProductBundle("bundle1", Mono.just(requestModel)))
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException &&
                        throwable.getMessage().equals("One or more products not found"))
                .verify();

        verify(bundleRepository, times(1)).findByBundleId("bundle1");
        verify(productRepository, times(1)).findAllById(productIds);
        verify(bundleRepository, times(0)).save(any(ProductBundle.class));
    }

    @Test
    public void testDeleteProductBundle_Success() {
        when(bundleRepository.findByBundleId("bundle1")).thenReturn(Mono.just(existingBundle));
        when(bundleRepository.delete(existingBundle)).thenReturn(Mono.empty());

        StepVerifier.create(productBundleService.deleteProductBundle("bundle1"))
                .verifyComplete();

        verify(bundleRepository, times(1)).findByBundleId("bundle1");
        verify(bundleRepository, times(1)).delete(existingBundle);
    }

    @Test
    public void testDeleteProductBundle_NotFound() {
        when(bundleRepository.findByBundleId("bundle1")).thenReturn(Mono.empty());

        StepVerifier.create(productBundleService.deleteProductBundle("bundle1"))
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException &&
                        throwable.getMessage().equals("Bundle not found: bundle1"))
                .verify();

        verify(bundleRepository, times(1)).findByBundleId("bundle1");
        verify(bundleRepository, times(0)).delete(any(ProductBundle.class));
    }
}
