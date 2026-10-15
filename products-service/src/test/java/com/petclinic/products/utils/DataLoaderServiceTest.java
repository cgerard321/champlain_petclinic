package com.petclinic.products.utils;

import com.petclinic.products.datalayer.images.Image;
import com.petclinic.products.datalayer.images.ImageRepository;
import com.petclinic.products.datalayer.products.Product;
import com.petclinic.products.datalayer.products.ProductBundleRepository;
import com.petclinic.products.datalayer.products.ProductRepository;
import com.petclinic.products.datalayer.products.ProductTypeRepository;
import com.petclinic.products.datalayer.ratings.RatingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashSet;
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
    private ImageRepository imageRepository;
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
        dataLoaderService.imageRepository = imageRepository;
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
    void restoresMissingBuiltInImageReferencedByExistingProduct() throws Exception {
        doReturn(new byte[]{1, 2, 3}).when(dataLoaderService).loadImageData(anyString());
        when(productRepository.findAll())
                .thenReturn(Flux.just(product), Flux.just(product));
        when(imageRepository.findImageByImageId(anyString()))
                .thenAnswer(invocation -> IMAGE_ID.equals(invocation.getArgument(0))
                        ? Mono.empty()
                        : Mono.just(Image.builder()
                        .imageId(invocation.getArgument(0))
                        .build()));
        when(imageRepository.save(org.mockito.ArgumentMatchers.any(Image.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        dataLoaderService.run();

        verify(imageRepository).save(argThat(image -> {
            assertTrue(image.getImageData().length > 0);
            return IMAGE_ID.equals(image.getImageId())
                    && "dog_food.jpg".equals(image.getImageName())
                    && "image/jpeg".equals(image.getImageType());
        }));
    }

    @Test
    void doesNotOverwriteExistingLegacyImage() throws Exception {
        Image existing = Image.builder().imageId(IMAGE_ID).build();
        when(productRepository.findAll())
                .thenReturn(Flux.just(product), Flux.just(product));
        when(imageRepository.findImageByImageId(anyString()))
                .thenReturn(Mono.just(existing));

        dataLoaderService.run();

        verify(imageRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }
    @Test
    void freshCatalogIncludesFrenchNamesAndDescriptionsForEverySeed() throws Exception {
        when(productRepository.findAll()).thenReturn(Flux.empty());
        when(productRepository.save(org.mockito.ArgumentMatchers.any(Product.class)))
                .thenAnswer(call -> Mono.just(call.getArgument(0)));
        when(imageRepository.save(org.mockito.ArgumentMatchers.any()))
                .thenAnswer(call -> Mono.just(call.getArgument(0)));
        when(productTypeRepository.save(org.mockito.ArgumentMatchers.any()))
                .thenAnswer(call -> Mono.just(call.getArgument(0)));

        dataLoaderService.run();

        var saved = org.mockito.ArgumentCaptor.forClass(Product.class);
        verify(productRepository, org.mockito.Mockito.times(40)).save(saved.capture());
        for (var seeded : saved.getAllValues()) {
            assertTrue(seeded.getProductNameFr() != null && !seeded.getProductNameFr().isBlank(),
                    "Missing French name: " + seeded.getProductName());
            assertTrue(seeded.getProductDescriptionFr() != null && !seeded.getProductDescriptionFr().isBlank(),
                    "Missing French description: " + seeded.getProductName());
        }
    }

    @Test
    void emptyDatabaseInsertsAllSeededProducts() throws Exception {
        stubMissingProducts();
        when(productRepository.findAll()).thenReturn(Flux.empty());
        stubExistingImages();

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
        when(productRepository.findAll()).thenReturn(Flux.just(product));
        when(imageRepository.findImageByImageId(anyString()))
                .thenReturn(Mono.just(Image.builder().build()));
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
        when(productRepository.findAll()).thenReturn(Flux.empty());
        stubExistingImages();

        dataLoaderService.run();
        dataLoaderService.run();

        verify(productRepository, times(40)).save(any(Product.class));
    }

    private void stubMissingProducts() {
        when(productRepository.findProductByProductId(anyString())).thenReturn(Mono.empty());
        when(productRepository.findByProductNameContainingIgnoreCase(anyString()))
                .thenReturn(Flux.empty());
        when(productRepository.save(any(Product.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(productRepository.findAllByProductIdIn(anyList())).thenReturn(Flux.empty());
    }

    private void stubExistingImages() {
        when(imageRepository.findImageByImageId(anyString()))
                .thenReturn(Mono.just(Image.builder().build()));
    }
}
