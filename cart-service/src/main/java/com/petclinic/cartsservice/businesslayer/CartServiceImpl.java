package com.petclinic.cartsservice.businesslayer;

import com.petclinic.cartsservice.dataaccesslayer.CartRepository;
import com.petclinic.cartsservice.domainclientlayer.ProductClient;
import com.petclinic.cartsservice.domainclientlayer.ProductResponseModel;
import com.petclinic.cartsservice.presentationlayer.CartResponseModel;
import com.petclinic.cartsservice.utils.EntityModelUtil;
import com.petclinic.cartsservice.utils.exceptions.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class CartServiceImpl implements CartService {
    private final CartRepository cartRepository;
    private final ProductClient productClient;

    public CartServiceImpl(CartRepository cartRepository, ProductClient productClient) {
        this.cartRepository = cartRepository;
        this.productClient = productClient;
    }

    @Override
    public Mono<CartResponseModel> getCartByCartId(String cartId) {
        return cartRepository.findCartByCartId(cartId)
                .switchIfEmpty(Mono.defer(() -> Mono.error(new NotFoundException("Cart id was not found: " + cartId))))
                .doOnNext(e -> log.debug("The cart response entity is: " + e.toString()))
                .flatMap(cart -> {
                    List<String> productIds = cart.getProductIds();
                    return productIds
                            .stream().map(productId -> productClient.getProductByProductId(productId).flux())
                            .reduce(Flux.empty(), Flux::merge)
                            .collectList()
                            .map(products -> EntityModelUtil.toCartResponseModel(cart, products));
                });
    }

    @Override
    public Flux<ProductResponseModel> clearCart(String cartId) {
        return cartRepository.findCartByCartId(cartId)
                .switchIfEmpty(Mono.error(new NotFoundException("Cart not found: " + cartId)))
                .flatMapMany(cart -> {
                    // Retrieve  products based on productIds and clear  cart simultaneously
                    Flux<ProductResponseModel> productsFlux = Flux.fromIterable(cart.getProductIds())
                            .flatMap(productClient::getProductByProductId);

                    // Clear cart and save it
                    cart.setProductIds(Collections.emptyList());
                    return cartRepository.save(cart)
                            .thenMany(productsFlux);  // Ensure cart is saved before returning the products
                });
    }

}
