package com.petclinic.cartsservice.businesslayer;

import com.petclinic.cartsservice.dataaccesslayer.PromoCode;
import com.petclinic.cartsservice.dataaccesslayer.PromoRepository;
import com.petclinic.cartsservice.domainclientlayer.PromoCodeRequestModel;
import com.petclinic.cartsservice.domainclientlayer.PromoCodeResponseModel;
import com.petclinic.cartsservice.utils.EntityModelUtil;
import com.petclinic.cartsservice.utils.exceptions.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PromoCodeServiceUnitTest {

    @Mock
    private PromoRepository promoRepository;

    @InjectMocks
    private PromoCodeServiceImpl promoCodeService;

    private PromoCode promoCode;
    private PromoCodeRequestModel promoCodeRequestModel;

    @BeforeEach
    public void setup() {
        promoCodeRequestModel = new PromoCodeRequestModel();
        promoCodeRequestModel.setCode("SUMMER2024");
        promoCodeRequestModel.setName("Summer Promo");
        promoCodeRequestModel.setExpirationDate("2024-12-31T23:59:59");

        promoCode = new PromoCode();
        promoCode.setId(UUID.randomUUID().toString());
        promoCode.setCode("SUMMER2024");
        promoCode.setName("Summer Promo");
        promoCode.setActive(true);
        promoCode.setExpirationDate(EntityModelUtil.validateExpirationDate("2024-12-31T23:59:59"));
    }

    @Test
    void getAllPromoCodes_shouldReturnAllPromoCodes() {
        // Arrange
        when(promoRepository.findAll()).thenReturn(Flux.just(promoCode));

        // Act
        Flux<PromoCodeResponseModel> promoCodeFlux = promoCodeService.getAllPromoCodes();

        // Assert
        StepVerifier.create(promoCodeFlux)
                .assertNext(promoCodeResponseModel -> {
                    assertNotNull(promoCodeResponseModel);
                    assertEquals(promoCodeResponseModel.getCode(), "SUMMER2024");
                    assertEquals(promoCodeResponseModel.getName(), "Summer Promo");
                    assertEquals(promoCodeResponseModel.getExpirationDate().toString(), "2024-12-31T23:59:59");
                })
                .verifyComplete();

        verify(promoRepository, times(1)).findAll();
    }

    @Test
    void getPromoCodeById_shouldReturnPromoCode_whenIdExists() {
        // Arrange
        when(promoRepository.findById(eq(promoCode.getId()))).thenReturn(Mono.just(promoCode));

        // Act
        Mono<PromoCodeResponseModel> promoCodeMono = promoCodeService.getPromoCodeById(promoCode.getId());

        // Assert
        StepVerifier.create(promoCodeMono)
                .assertNext(promoCodeResponseModel -> {
                    assertNotNull(promoCodeResponseModel);
                    assertEquals(promoCode.getCode(), promoCodeResponseModel.getCode());
                    assertEquals(promoCode.getName(), promoCodeResponseModel.getName());
                    String expectedExpirationDate = promoCode.getExpirationDate().toString();
                    assertEquals(expectedExpirationDate, promoCodeResponseModel.getExpirationDate().toString());
                })
                .verifyComplete();

        verify(promoRepository, times(1)).findById(promoCode.getId());
    }


    @Test
    void getPromoCodeById_shouldReturnError_whenIdNotFound() {
        // Arrange
        when(promoRepository.findById(eq(promoCode.getId()))).thenReturn(Mono.empty());

        // Act
        Mono<PromoCodeResponseModel> promoCodeMono = promoCodeService.getPromoCodeById(promoCode.getId());

        // Assert
        StepVerifier.create(promoCodeMono)
                .expectErrorMatches(throwable -> throwable instanceof ResponseStatusException
                        && throwable.getMessage().contains("Promo code not found"))
                .verify();
        verify(promoRepository, times(1)).findById(promoCode.getId());
    }


    @Test
    void updatePromoCodeById_shouldUpdateAndReturnUpdatedPromoCode() {
        // Arrange
        when(promoRepository.findById(eq(promoCode.getId()))).thenReturn(Mono.just(promoCode));
        when(promoRepository.save(any())).thenReturn(Mono.just(promoCode));

        // Act
        Mono<PromoCodeResponseModel> updatedPromoCode = promoCodeService.updatePromoCodeById(promoCodeRequestModel, promoCode.getId());

        // Assert
        StepVerifier.create(updatedPromoCode)
                .expectNextMatches(Objects::nonNull)
                .verifyComplete();

        verify(promoRepository, times(1)).findById(promoCode.getId());
        verify(promoRepository, times(1)).save(any(PromoCode.class));
    }

    @Test
    void deletePromoCode_shouldDeleteAndReturnDeletedPromoCode() {
        // Arrange
        when(promoRepository.findById(eq(promoCode.getId()))).thenReturn(Mono.just(promoCode));
        when(promoRepository.delete(any())).thenReturn(Mono.empty());

        // Act
        Mono<PromoCodeResponseModel> deletedPromoCode = promoCodeService.deletePromoCode(promoCode.getId());

        // Assert
        StepVerifier.create(deletedPromoCode)
                .expectNextMatches(Objects::nonNull)
                .verifyComplete();

        verify(promoRepository, times(1)).findById(promoCode.getId());
        verify(promoRepository, times(1)).delete(promoCode);
    }

    @Test
    void createPromo_shouldCreateAndReturnNewPromoCode() {
        // Arrange
        when(promoRepository.save(any())).thenReturn(Mono.just(promoCode));

        // Act
        Mono<PromoCodeResponseModel> createdPromoCode = promoCodeService.createPromo(promoCodeRequestModel);

        // Assert
        StepVerifier.create(createdPromoCode)
                .expectNextMatches(Objects::nonNull)
                .verifyComplete();

        verify(promoRepository, times(1)).save(any(PromoCode.class));
    }
}
