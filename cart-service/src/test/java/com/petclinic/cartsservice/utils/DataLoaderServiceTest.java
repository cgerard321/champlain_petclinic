package com.petclinic.cartsservice.utils;

import com.petclinic.cartsservice.dataaccesslayer.Cart;
import com.petclinic.cartsservice.dataaccesslayer.CartRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class DataLoaderServiceTest {

    @Mock
    private CartRepository cartRepository;

    @InjectMocks
    private DataLoaderService dataLoaderService;

    @Test
    void run_whenCollectionEmpty_insertsTenDemoCarts() {
        when(cartRepository.findAll()).thenReturn(Flux.empty());

        when(cartRepository.insert(any(Cart.class)))
                .thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        dataLoaderService.run();

        ArgumentCaptor<Cart> cartCaptor = ArgumentCaptor.forClass(Cart.class);
        verify(cartRepository, times(10)).insert(cartCaptor.capture());

        List<Cart> inserted = cartCaptor.getAllValues();
        assertEquals(10, inserted.size(), "Expected 10 demo carts to be inserted");

        for (Cart c : inserted) {
            assertNotNull(c.getCartId(), "cartId should be set");
            assertNotNull(c.getCustomerId(), "customerId should be set");
            assertNotNull(c.getProducts(), "products list should be set");
            assertNotNull(c.getWishListProducts(), "wishlist should be set");
            assertTrue(c.getProducts().size() >= 2, "each demo cart should have at least two products");
        }

        verify(cartRepository, times(1)).findAll();
        verifyNoMoreInteractions(cartRepository);
    }

    @Test
    void run_whenCollectionNotEmpty_insertsNothing() {
        when(cartRepository.findAll()).thenReturn(Flux.just(Cart.builder().cartId("existing").build()));

        dataLoaderService.run();

        verify(cartRepository, times(1)).findAll();
        verify(cartRepository, never()).insert(any(Cart.class));
        verifyNoMoreInteractions(cartRepository);
    }
}
