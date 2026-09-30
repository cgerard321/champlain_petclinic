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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
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
        dataLoaderService = new DataLoaderService();
        dataLoaderService.productRepository = productRepository;
        dataLoaderService.productBundleRepository = productBundleRepository;
        dataLoaderService.imageRepository = imageRepository;
        dataLoaderService.ratingRepository = ratingRepository;
        dataLoaderService.productTypeRepository = productTypeRepository;

        product = Product.builder()
                .productId(PRODUCT_ID)
                .imageId(IMAGE_ID)
                .build();
    }

    @Test
    void restoresMissingBuiltInImageReferencedByExistingProduct() throws Exception {
        when(productRepository.findAll())
                .thenReturn(Flux.just(product), Flux.just(product));
        when(imageRepository.findImageByImageId(IMAGE_ID)).thenReturn(Mono.empty());
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
        when(imageRepository.findImageByImageId(IMAGE_ID))
                .thenReturn(Mono.just(existing));

        dataLoaderService.run();

        verify(imageRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }
}
