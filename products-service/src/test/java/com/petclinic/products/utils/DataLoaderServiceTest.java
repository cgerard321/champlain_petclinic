package com.petclinic.products.utils;

import com.petclinic.products.datalayer.images.Image;
import com.petclinic.products.migration.LegacyImageMigration;
import com.petclinic.products.datalayer.products.Product;
import com.petclinic.products.datalayer.products.ProductBundleRepository;
import com.petclinic.products.datalayer.products.ProductRepository;
import com.petclinic.products.datalayer.products.ProductTypeRepository;
import com.petclinic.products.datalayer.ratings.Rating;
import com.petclinic.products.datalayer.ratings.RatingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataLoaderServiceTest {

    private static final String PRODUCT_ID =
            "06a7d573-bcab-4db3-956f-773324b92a80";
    private static final String IMAGE_ID =
            "08a5af6b-3501-4157-9a99-1aa82387b9e4";

    @Mock
    private ProductRepository productRepository;
    @Mock
    private ProductBundleRepository productBundleRepository;
    @Mock
    private LegacyImageMigration imagePublisher;
    @Mock
    private RatingRepository ratingRepository;
    @Mock
    private ProductTypeRepository productTypeRepository;

    private DataLoaderService dataLoaderService;
    private Product product;

    @BeforeEach
    void setUp() {
        dataLoaderService = spy(new DataLoaderService());
        dataLoaderService.productRepository = productRepository;
        dataLoaderService.productBundleRepository = productBundleRepository;
        dataLoaderService.imagePublisher = imagePublisher;
        lenient().when(imagePublisher.publishSeed(any())).thenReturn(Mono.just("files-service-id"));
        dataLoaderService.ratingRepository = ratingRepository;
        dataLoaderService.productTypeRepository = productTypeRepository;

        lenient().when(productRepository.backfillFrenchName(org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(Mono.just(0));
        lenient().when(productRepository.backfillFrenchDescription(org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(Mono.just(0));

        product = Product.builder()
                .productId(PRODUCT_ID)
                .imageId(IMAGE_ID)
                .build();
    }

    @Test
    void seedUploadFailureStopsBeforeSavingProduct() {
        when(imagePublisher.publishSeed(any())).thenReturn(Mono.error(new IllegalStateException("Upload failed")));
        org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class, () -> dataLoaderService.run());
        verify(productRepository, never()).save(any());
    }

    @Test
    void deletedSeedImagesSkipProductRecreationWithoutBlockingStartup() throws Exception {
        when(imagePublisher.publishSeed(any())).thenReturn(Mono.empty());
        dataLoaderService.run();
        verify(productRepository, never()).save(any());
    }

    @Test
    void existingProductsDoNotUploadOrOverwriteImages() throws Exception {
        stubAllProductsExist();
        dataLoaderService.run();
        verify(imagePublisher, never()).publishSeed(any());
        verify(productRepository, never()).save(any());
    }

    @Test
    void freshCatalogIncludesFrenchNamesAndDescriptionsForEverySeed() throws Exception {
        when(productRepository.save(org.mockito.ArgumentMatchers.any(Product.class)))
                .thenAnswer(call -> Mono.just(call.getArgument(0)));
        when(productTypeRepository.save(org.mockito.ArgumentMatchers.any()))
                .thenAnswer(call -> Mono.just(call.getArgument(0)));

        dataLoaderService.run();

        var saved = org.mockito.ArgumentCaptor.forClass(Product.class);
        verify(productRepository, org.mockito.Mockito.times(40)).save(saved.capture());
        for (var seeded : saved.getAllValues()) {
            org.junit.jupiter.api.Assertions.assertEquals("files-service-id", seeded.getImageId());
            assertTrue(seeded.getProductNameFr() != null && !seeded.getProductNameFr().isBlank(),
                    "Missing French name: " + seeded.getProductName());
            assertTrue(seeded.getProductDescriptionFr() != null && !seeded.getProductDescriptionFr().isBlank(),
                    "Missing French description: " + seeded.getProductName());
        }
    }

    @Test
    void emptyDatabaseInsertsAllSeededProducts() throws Exception {
        stubMissingProducts();

        dataLoaderService.run();

        verify(productRepository, times(40)).save(any(Product.class));
    }

    @Test
    void existingProductsReceiveOnlyMissingProducts() throws Exception {
        when(productRepository.findProductByProductId(anyString()))
                .thenAnswer(invocation -> PRODUCT_ID.equals(invocation.getArgument(0))
                        ? Mono.just(product)
                        : Mono.empty());
        when(productRepository.findByProductNameContainingIgnoreCase(anyString()))
                .thenReturn(Flux.empty());
        when(productRepository.save(any(Product.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(productRepository.findAllByProductIdIn(anyList())).thenReturn(Flux.empty());
        when(ratingRepository.findRatingByCustomerIdAndProductId(anyString(), anyString()))
                .thenReturn(Mono.just(com.petclinic.products.datalayer.ratings.Rating.builder().build()));

        dataLoaderService.run();

        verify(productRepository, never()).save(argThat(saved ->
                PRODUCT_ID.equals(saved.getProductId())));
        verify(productRepository, times(39)).save(argThat(saved ->
                !PRODUCT_ID.equals(saved.getProductId())));
    }

    @Test
    void runningLoaderTwiceDoesNotCreateDuplicateProducts() throws Exception {
        Set<String> savedProductIds = new HashSet<>();
        when(productRepository.findProductByProductId(anyString()))
                .thenAnswer(invocation -> {
                    String productId = invocation.getArgument(0);
                    return savedProductIds.contains(productId)
                            ? Mono.just(Product.builder().productId(productId).build())
                            : Mono.empty();
                });
        when(productRepository.findByProductNameContainingIgnoreCase(anyString()))
                .thenReturn(Flux.empty());
        when(productRepository.save(any(Product.class)))
                .thenAnswer(invocation -> {
                    Product saved = invocation.getArgument(0);
                    savedProductIds.add(saved.getProductId());
                    return Mono.just(saved);
                });
        when(productRepository.findAllByProductIdIn(anyList())).thenReturn(Flux.empty());

        dataLoaderService.run();
        dataLoaderService.run();

        verify(productRepository, times(40)).save(any(Product.class));
    }

    @Test
    void emptyDatabaseSavesEverySeededRatingOnce() throws Exception {
        Set<String> savedProductIds = new HashSet<>();
        stubStatefulProductRepository(savedProductIds);
        stubRatingRepository(new HashSet<>());

        dataLoaderService.run();

        var saved = org.mockito.ArgumentCaptor.forClass(Rating.class);
        verify(ratingRepository, times(220)).save(saved.capture());
        Set<String> productsWithCategoryRatings = new HashSet<>();
        Set<Byte> qualityScores = new HashSet<>();
        for (Rating seed : saved.getAllValues()) {
            for (Byte score : java.util.List.of(seed.getQualityRating(), seed.getValueRating(),
                    seed.getPetSatisfactionRating(), seed.getIngredientsRating())) {
                assertTrue(score >= 1 && score <= 5);
            }
            productsWithCategoryRatings.add(seed.getProductId());
            qualityScores.add(seed.getQualityRating());
        }
        org.junit.jupiter.api.Assertions.assertEquals(40, productsWithCategoryRatings.size());
        assertTrue(qualityScores.size() > 1);
    }

    @Test
    void runningLoaderTwiceDoesNotCreateDuplicateRatings() throws Exception {
        Set<String> savedProductIds = new HashSet<>();
        Set<String> savedRatingKeys = new LinkedHashSet<>();
        stubStatefulProductRepository(savedProductIds);
        stubRatingRepository(savedRatingKeys);

        dataLoaderService.run();
        dataLoaderService.run();

        verify(ratingRepository, times(220)).save(any(Rating.class));
    }

    @Test
    void existingRatingForSameCustomerAndProductIsNotOverwritten() throws Exception {
        String existingProductId = "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7001";
        String existingCustomerId = "810440e8-cae0-48f7-aa4f-830239b82b78";
        Rating existingRating = Rating.builder()
                .productId(existingProductId)
                .customerId(existingCustomerId)
                .rating((byte) 2)
                .review("Existing customer review must remain unchanged.")
                .build();
        stubAllProductsExist();
        when(ratingRepository.findRatingByCustomerIdAndProductId(anyString(), anyString()))
                .thenAnswer(invocation -> existingProductId.equals(invocation.getArgument(1))
                        && existingCustomerId.equals(invocation.getArgument(0))
                        ? Mono.just(existingRating)
                        : Mono.empty());
        when(ratingRepository.save(any(Rating.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        dataLoaderService.run();

        verify(ratingRepository, times(219)).save(any(Rating.class));
        verify(ratingRepository, never()).save(argThat(rating ->
                existingProductId.equals(rating.getProductId())
                        && existingCustomerId.equals(rating.getCustomerId())));
    }

    private void stubMissingProducts() {
        when(productRepository.findProductByProductId(anyString())).thenReturn(Mono.empty());
        when(productRepository.findByProductNameContainingIgnoreCase(anyString()))
                .thenReturn(Flux.empty());
        when(productRepository.save(any(Product.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(productRepository.findAllByProductIdIn(anyList())).thenReturn(Flux.empty());
    }


    private void stubStatefulProductRepository(Set<String> savedProductIds) {
        when(productRepository.findProductByProductId(anyString()))
                .thenAnswer(invocation -> {
                    String productId = invocation.getArgument(0);
                    return savedProductIds.contains(productId)
                            ? Mono.just(Product.builder().productId(productId).build())
                            : Mono.empty();
                });
        when(productRepository.findByProductNameContainingIgnoreCase(anyString()))
                .thenReturn(Flux.empty());
        when(productRepository.save(any(Product.class)))
                .thenAnswer(invocation -> {
                    Product saved = invocation.getArgument(0);
                    savedProductIds.add(saved.getProductId());
                    return Mono.just(saved);
                });
        when(productRepository.findAllByProductIdIn(anyList())).thenReturn(Flux.empty());
    }

    private void stubAllProductsExist() {
        when(productRepository.findProductByProductId(anyString()))
                .thenReturn(Mono.just(product));
        when(productRepository.findAllByProductIdIn(anyList())).thenReturn(Flux.empty());
    }

    private void stubRatingRepository(Set<String> savedRatingKeys) {
        when(ratingRepository.findRatingByCustomerIdAndProductId(anyString(), anyString()))
                .thenAnswer(invocation -> {
                    String customerId = invocation.getArgument(0);
                    String productId = invocation.getArgument(1);
                    String key = customerId + ":" + productId;
                    return savedRatingKeys.contains(key)
                            ? Mono.just(Rating.builder()
                            .customerId(customerId)
                            .productId(productId)
                            .build())
                            : Mono.empty();
                });
        when(ratingRepository.save(any(Rating.class)))
                .thenAnswer(invocation -> {
                    Rating saved = invocation.getArgument(0);
                    savedRatingKeys.add(saved.getCustomerId() + ":" + saved.getProductId());
                    return Mono.just(saved);
                });
    }
}
