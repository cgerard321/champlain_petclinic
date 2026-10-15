package com.petclinic.products.businesslayer;

import com.petclinic.products.businesslayer.products.ProductServiceImpl;
import com.petclinic.products.datalayer.products.*;
import com.petclinic.products.datalayer.ratings.RatingRepository;
import com.petclinic.products.presentationlayer.products.ProductRequestModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductLocalizationTest {
    @Mock ProductRepository products;
    @Mock ProductTypeRepository types;
    @Mock RatingRepository ratings;
    @InjectMocks ProductServiceImpl service;

    @BeforeEach
    void setup() {
        when(products.save(any(Product.class))).thenAnswer(call -> Mono.just(call.getArgument(0)));
        when(types.findByProductTypeId("food")).thenReturn(Mono.empty());
        when(ratings.findRatingsByProductId(anyString())).thenReturn(Flux.empty());
    }

    private ProductRequestModel request() {
        return ProductRequestModel.builder().productName("Cat Litter")
                .productDescription("Clumping litter").productSalePrice(12.99)
                .productTypeId("food").productQuantity(4).isUnlisted(false).build();
    }

    @Test
    void createPersistsAndReturnsBothLanguages() {
        ProductRequestModel request = request();
        request.setProductNameFr("Litière pour chats");
        request.setProductDescriptionFr("Litière agglomérante");
        StepVerifier.create(service.addProduct(Mono.just(request)))
                .assertNext(response -> {
                    assertEquals("Cat Litter", response.getProductName());
                    assertEquals("Clumping litter", response.getProductDescription());
                    assertEquals("Litière pour chats", response.getProductNameFr());
                    assertEquals("Litière agglomérante", response.getProductDescriptionFr());
                }).verifyComplete();
        verify(products).save(argThat(p -> "Litière pour chats".equals(p.getProductNameFr())
                && "Litière agglomérante".equals(p.getProductDescriptionFr())));
    }

    @Test
    void legacyCreateStillWorksWithoutTranslations() {
        StepVerifier.create(service.addProduct(Mono.just(request())))
                .assertNext(response -> {
                    assertEquals("Cat Litter", response.getProductName());
                    assertNull(response.getProductNameFr());
                    assertNull(response.getProductDescriptionFr());
                }).verifyComplete();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"Nouvelle traduction"})
    void updatePreservesOmittedTranslationsAndAllowsExplicitReplacementOrClearing(String value) {
        Product existing = Product.builder().id(42).productId("sample")
                .productNameFr("Ancien nom").productDescriptionFr("Ancienne description").build();
        when(products.findProductByProductId("sample")).thenReturn(Mono.just(existing));
        ProductRequestModel request = request();
        request.setProductNameFr(value);
        request.setProductDescriptionFr(value);
        StepVerifier.create(service.updateProductByProductId("sample", Mono.just(request)))
                .assertNext(response -> {
                    assertEquals("sample", response.getProductId());
                    assertEquals(value == null ? "Ancien nom" : value, response.getProductNameFr());
                    assertEquals(value == null ? "Ancienne description" : value, response.getProductDescriptionFr());
                }).verifyComplete();
        verify(products).save(argThat(p -> p.getId() == 42
                && (value == null ? "Ancien nom" : value).equals(p.getProductNameFr())));
    }
}
