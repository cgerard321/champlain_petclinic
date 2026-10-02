package com.petclinic.cartsservice.utils;

import com.petclinic.cartsservice.dataaccesslayer.PromoCode;
import com.petclinic.cartsservice.dataaccesslayer.PromoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PromoCodeSeederTest {

    @Mock
    private PromoRepository promoRepository;

    @InjectMocks
    private PromoCodeSeeder promoCodeSeeder;

    @Test
    void seedPromos_shouldSaveDefaults_whenNoPromosExist() {
        // Arrange
        when(promoRepository.count()).thenReturn(Mono.just(0L));
        when(promoRepository.saveAll(any(Flux.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        promoCodeSeeder.seedPromos();

        // Assert
        verify(promoRepository, times(1)).saveAll(any(Flux.class));
    }

    @Test
    void seedPromos_shouldSkipSeeding_whenPromosAlreadyExist() {
        // Arrange
        when(promoRepository.count()).thenReturn(Mono.just(3L));

        // Act
        promoCodeSeeder.seedPromos();

        // Assert
        verify(promoRepository, never()).saveAll(any(Flux.class));
    }

    @Test
    void seedPromos_shouldNotThrow_whenCountFails() {
        // Arrange
        when(promoRepository.count()).thenReturn(Mono.error(new RuntimeException("db down")));

        // Act
        promoCodeSeeder.seedPromos();

        // Assert
        verify(promoRepository, never()).save(any(PromoCode.class));
    }
}
