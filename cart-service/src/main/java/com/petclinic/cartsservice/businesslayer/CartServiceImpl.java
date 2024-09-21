package com.petclinic.cartsservice.businesslayer;

import com.petclinic.cartsservice.dataaccesslayer.Cart;
import com.petclinic.cartsservice.dataaccesslayer.CartRepository;
import com.petclinic.cartsservice.domainclientlayer.ProductClient;
import com.petclinic.cartsservice.domainclientlayer.ProductResponseModel;
import com.petclinic.cartsservice.presentationlayer.CartRequestModel;
import com.petclinic.cartsservice.presentationlayer.CartResponseModel;
import com.petclinic.cartsservice.utils.EntityModelUtil;
import com.petclinic.cartsservice.utils.exceptions.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
                .doOnNext(cart -> log.debug("The cart response entity is: " + cart.toString()))
                .flatMap(cart -> {
                    List<String> productIds = cart.getProductIds();

                    // Fetch all products by their IDs and reduce them into a Flux
                    return productIds.stream()
                            .map(productId -> productClient.getProductByProductId(productId).flux())
                            .reduce(Flux.empty(), Flux::merge)
                            .collectList()
                            .map(products -> EntityModelUtil.toCartResponseModel(cart, products));
                });
    }

    @Override
    public Mono<Integer> getCartItemCount(String cartId) {
        return cartRepository.findCartByCartId(cartId)
                .map(cart -> cart.getProductIds().size())  // Count the number of product IDs in the cart
                .switchIfEmpty(Mono.empty());
    }

    @Override
    public Mono<CartResponseModel> updateCartByCartId(Mono<CartRequestModel> cartRequestModel, String cartId) {
        return cartRepository.findCartByCartId(cartId)
                .switchIfEmpty(Mono.defer(() -> Mono.error(new NotFoundException("Cart id was not found: " + cartId))))
                .flatMap(foundCart -> cartRequestModel.flatMap(request -> {
                    List<String> productIds = request.getProductIds();

                    // Map the request to a Cart entity and update fields
                    Cart cartEntity = EntityModelUtil.toCartEntity(request);
                    cartEntity.setProductIds(productIds);
                    cartEntity.setId(foundCart.getId());
                    cartEntity.setCartId(foundCart.getCartId());

                    // Save the updated cart entity
                    return cartRepository.save(cartEntity);
                }))
                .flatMap(cart -> {
                    List<String> productIds = cart.getProductIds();

                    // Fetch all products by their IDs and reduce them into a Flux
                    return productIds.stream()
                            .map(productId -> productClient.getProductByProductId(productId).flux())
                            .reduce(Flux.empty(), Flux::merge)
                            .collectList()
                            .map(products -> EntityModelUtil.toCartResponseModel(cart, products));
                });
    }
}