package com.petclinic.products.businesslayer;

import com.petclinic.products.businesslayer.products.ProductBundleService;
import com.petclinic.products.businesslayer.products.ProductServiceImpl;
import com.petclinic.products.datalayer.products.Product;
import com.petclinic.products.datalayer.products.ProductBundleRepository;
import com.petclinic.products.datalayer.products.ProductRepository;
import com.petclinic.products.datalayer.products.ProductTypeRepository;
import com.petclinic.products.datalayer.ratings.RatingRepository;
import com.petclinic.products.domainclientlayer.FileRequestDTO;
import com.petclinic.products.domainclientlayer.FileResponseDTO;
import com.petclinic.products.domainclientlayer.FilesServiceClient;
import com.petclinic.products.presentationlayer.products.ProductRequestModel;
import com.petclinic.products.presentationlayer.products.ProductResponseModel;
import com.petclinic.products.utils.exceptions.FailedDependencyException;
import com.petclinic.products.utils.exceptions.FileNotFoundInFilesServiceException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductImageServiceUnitTest {

    private static final String PRODUCT_ID =
            "06a7d573-bcab-4db3-956f-773324b92a80";
    private static final String FILE_ID =
            "1501f30e-1db1-44b2-a555-bca6f64450e4";
    private static final String NEW_FILE_ID =
            "ae2d3af7-f2a2-407f-ad31-ca7d8220cb77";

    @Mock
    private ProductRepository productRepository;
    @Mock
    private RatingRepository ratingRepository;
    @Mock
    private ProductBundleRepository productBundleRepository;
    @Mock
    private ProductBundleService productBundleService;
    @Mock
    private ProductTypeRepository productTypeRepository;
    @Mock
    private FilesServiceClient filesServiceClient;

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    void addProductWithImageUploadsFileAndStoresReturnedFileId() {
        FileRequestDTO image = imageRequest();
        FileResponseDTO uploadedFile = fileResponse(FILE_ID);
        ProductRequestModel request = ProductRequestModel.builder()
                .productName("Dog Food")
                .productSalePrice(12.99)
                .image(image)
                .build();

        when(filesServiceClient.addFile(image)).thenReturn(Mono.just(uploadedFile));
        when(ratingRepository.findRatingsByProductId(any())).thenReturn(Flux.empty());
        when(productRepository.save(any(Product.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(productService.addProduct(Mono.just(request)))
                .assertNext(response -> {
                    assertEquals(FILE_ID, response.getImageId());
                    assertEquals(FILE_ID, response.getImage().getFileId());
                })
                .verifyComplete();

        verify(filesServiceClient).addFile(image);
        verify(productRepository).save(
                org.mockito.ArgumentMatchers.argThat(
                        product -> FILE_ID.equals(product.getImageId())));
    }

    @Test
    void getProductWithIncludeImageReturnsFileData() {
        Product product = productWithImage(FILE_ID);
        FileResponseDTO file = fileResponse(FILE_ID);

        when(productRepository.findProductByProductId(PRODUCT_ID))
                .thenReturn(Mono.just(product));
        when(ratingRepository.findRatingsByProductId(PRODUCT_ID))
                .thenReturn(Flux.empty());
        when(filesServiceClient.getFile(FILE_ID)).thenReturn(Mono.just(file));

        StepVerifier.create(productService.getProductByProductId(PRODUCT_ID, true))
                .assertNext(response -> {
                    assertEquals(FILE_ID, response.getImage().getFileId());
                    assertArrayEquals(file.getFileData(), response.getImage().getFileData());
                })
                .verifyComplete();
    }

    @Test
    void getLegacyProductKeepsImageIdWhenFileIsMissing() {
        Product product = productWithImage(FILE_ID);

        when(productRepository.findProductByProductId(PRODUCT_ID))
                .thenReturn(Mono.just(product));
        when(ratingRepository.findRatingsByProductId(PRODUCT_ID))
                .thenReturn(Flux.empty());
        when(filesServiceClient.getFile(FILE_ID))
                .thenReturn(Mono.error(new FileNotFoundInFilesServiceException(
                        "File was not found in Files Service")));

        StepVerifier.create(productService.getProductByProductId(PRODUCT_ID, true))
                .assertNext(response -> {
                    assertEquals(FILE_ID, response.getImageId());
                    assertNull(response.getImage());
                })
                .verifyComplete();
    }

    @Test
    void includeImageKeepsLegacyProductWhenFileIsMissing() {
        ProductResponseModel response = ProductResponseModel.builder()
                .productId(PRODUCT_ID)
                .imageId(FILE_ID)
                .build();

        when(filesServiceClient.getFile(FILE_ID))
                .thenReturn(Mono.error(new FileNotFoundInFilesServiceException(
                        "File was not found in Files Service")));

        StepVerifier.create(productService.includeImage(response))
                .assertNext(result -> {
                    assertEquals(FILE_ID, result.getImageId());
                    assertNull(result.getImage());
                })
                .verifyComplete();
    }

    @Test
    void getProductReturnsWithoutImageWhenFilesServiceIsUnavailable() {
        Product product = productWithImage(FILE_ID);

        when(productRepository.findProductByProductId(PRODUCT_ID))
                .thenReturn(Mono.just(product));
        when(ratingRepository.findRatingsByProductId(PRODUCT_ID))
                .thenReturn(Flux.empty());
        when(filesServiceClient.getFile(FILE_ID))
                .thenReturn(Mono.error(
                        new FailedDependencyException("Files Service unavailable")));

        StepVerifier.create(productService.getProductByProductId(PRODUCT_ID, true))
                .assertNext(response -> {
                    assertEquals(FILE_ID, response.getImageId());
                    assertNull(response.getImage());
                })
                .verifyComplete();
    }

    @Test
    void getProductReturnsWithoutImageWhenFilesServiceReturnsEmpty() {
        Product product = productWithImage(FILE_ID);

        when(productRepository.findProductByProductId(PRODUCT_ID))
                .thenReturn(Mono.just(product));
        when(ratingRepository.findRatingsByProductId(PRODUCT_ID))
                .thenReturn(Flux.empty());
        when(filesServiceClient.getFile(FILE_ID)).thenReturn(Mono.empty());

        StepVerifier.create(productService.getProductByProductId(PRODUCT_ID, true))
                .assertNext(response -> {
                    assertEquals(FILE_ID, response.getImageId());
                    assertNull(response.getImage());
                })
                .verifyComplete();
    }

    @Test
    void includeImageReturnsProductWhenFilesServiceIsUnavailable() {
        ProductResponseModel response = ProductResponseModel.builder()
                .productId(PRODUCT_ID)
                .imageId(FILE_ID)
                .build();

        when(filesServiceClient.getFile(FILE_ID))
                .thenReturn(Mono.error(
                        new FailedDependencyException("Files Service unavailable")));

        StepVerifier.create(productService.includeImage(response))
                .assertNext(result -> {
                    assertEquals(PRODUCT_ID, result.getProductId());
                    assertEquals(FILE_ID, result.getImageId());
                    assertNull(result.getImage());
                })
                .verifyComplete();
    }

    @Test
    void updateCurrentImageUpdatesExistingFile() {
        Product product = productWithImage(FILE_ID);
        FileRequestDTO image = imageRequest();

        when(productRepository.findProductByProductId(PRODUCT_ID))
                .thenReturn(Mono.just(product));
        when(productRepository.existsByImageIdAndProductIdNot(FILE_ID, PRODUCT_ID))
                .thenReturn(Mono.just(false));
        when(filesServiceClient.updateFile(FILE_ID, image))
                .thenReturn(Mono.just(fileResponse(FILE_ID)));

        StepVerifier.create(productService.updateProductImage(PRODUCT_ID, image))
                .assertNext(response -> assertEquals(FILE_ID, response.getImageId()))
                .verifyComplete();

        verify(filesServiceClient).updateFile(FILE_ID, image);
        verify(filesServiceClient, never()).addFile(any());
    }

    @Test
    void updateMissingLegacyImageUploadsNewFileAndStoresItsId() {
        Product product = productWithImage(FILE_ID);
        FileRequestDTO image = imageRequest();

        when(productRepository.findProductByProductId(PRODUCT_ID))
                .thenReturn(Mono.just(product));
        when(productRepository.existsByImageIdAndProductIdNot(FILE_ID, PRODUCT_ID))
                .thenReturn(Mono.just(false));
        when(filesServiceClient.updateFile(FILE_ID, image))
                .thenReturn(Mono.error(new FileNotFoundInFilesServiceException(
                        "File was not found in Files Service")));
        when(filesServiceClient.addFile(image))
                .thenReturn(Mono.just(fileResponse(NEW_FILE_ID)));
        when(productRepository.save(product)).thenReturn(Mono.just(product));

        StepVerifier.create(productService.updateProductImage(PRODUCT_ID, image))
                .assertNext(response -> assertEquals(NEW_FILE_ID, response.getImageId()))
                .verifyComplete();

        verify(filesServiceClient).addFile(image);
        verify(productRepository).save(product);
        assertEquals(NEW_FILE_ID, product.getImageId());
    }

    @Test
    void deleteImageDeletesOwnedFileAndClearsImageId() {
        Product product = productWithImage(FILE_ID);

        when(productRepository.findProductByProductId(PRODUCT_ID))
                .thenReturn(Mono.just(product));
        when(productRepository.existsByImageIdAndProductIdNot(FILE_ID, PRODUCT_ID))
                .thenReturn(Mono.just(false));
        when(filesServiceClient.deleteFile(FILE_ID)).thenReturn(Mono.empty());
        when(productRepository.save(product)).thenReturn(Mono.just(product));

        StepVerifier.create(productService.deleteProductImage(PRODUCT_ID))
                .assertNext(response -> assertNull(response.getImageId()))
                .verifyComplete();

        verify(filesServiceClient).deleteFile(FILE_ID);
        verify(productRepository).save(product);
        assertNull(product.getImageId());
    }

    @Test
    void updateImagePropagatesFilesServiceFailureWithoutUploadingReplacement() {
        Product product = productWithImage(FILE_ID);
        FileRequestDTO image = imageRequest();

        when(productRepository.findProductByProductId(PRODUCT_ID))
                .thenReturn(Mono.just(product));
        when(productRepository.existsByImageIdAndProductIdNot(FILE_ID, PRODUCT_ID))
                .thenReturn(Mono.just(false));
        when(filesServiceClient.updateFile(FILE_ID, image))
                .thenReturn(Mono.error(
                        new FailedDependencyException("Files Service unavailable")));

        StepVerifier.create(productService.updateProductImage(PRODUCT_ID, image))
                .expectErrorMatches(error ->
                        error instanceof FailedDependencyException
                                && error.getMessage().equals("Files Service unavailable"))
                .verify();

        verify(filesServiceClient, never()).addFile(any());
        verify(productRepository, never()).save(any());
    }

    private FileRequestDTO imageRequest() {
        return FileRequestDTO.builder()
                .fileName("product.png")
                .fileType("image/png")
                .fileData(new byte[]{1, 2, 3})
                .build();
    }

    private FileResponseDTO fileResponse(String fileId) {
        return FileResponseDTO.builder()
                .fileId(fileId)
                .fileName("product.png")
                .fileType("image/png")
                .fileData(new byte[]{1, 2, 3})
                .build();
    }

    private Product productWithImage(String fileId) {
        return Product.builder()
                .productId(PRODUCT_ID)
                .productName("Dog Food")
                .productSalePrice(12.99)
                .imageId(fileId)
                .build();
    }
}
