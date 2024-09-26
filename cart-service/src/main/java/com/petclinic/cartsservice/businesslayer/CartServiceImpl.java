package com.petclinic.cartsservice.businesslayer;

import com.petclinic.cartsservice.dataaccesslayer.Cart;
import com.petclinic.cartsservice.dataaccesslayer.CartRepository;
import com.petclinic.cartsservice.dataaccesslayer.cartproduct.CartProduct;
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
                .map(EntityModelUtil::toCartResponseModel);
    }

    @Override
    public Flux<CartResponseModel> getAllCarts() {
        return cartRepository.findAll()
                .map(EntityModelUtil::toCartResponseModel)
                .doOnNext(e -> log.debug("The cart response entity is: " + e.toString()));
    }


    public Flux<CartResponseModel> clearCart(String cartId) {
        return cartRepository.findCartByCartId(cartId)
                .switchIfEmpty(Mono.error(new NotFoundException("Cart not found: " + cartId)))
                .flatMapMany(cart -> {
                    // Retrieve products based on productIds and clear the cart simultaneously
                    Flux<CartProduct> productsFlux = Flux.fromIterable(cart.getProducts());
                    // Clear cart and save it
                    cart.setProducts(Collections.emptyList());
                    return cartRepository.save(cart)
                            .map(EntityModelUtil::toCartResponseModel);  // Ensure cart is saved before returning the cart
                });
    }

//    @Override
//    public Mono<CartResponseModel> updateCartByCartId(Mono<CartRequestModel> cartRequestModel, String cartId) {
//        return cartRepository.findCartByCartId(cartId)
//                .switchIfEmpty(Mono.defer(() -> Mono.error(new NotFoundException("Cart id was not found: " + cartId))))
//                .flatMap(foundCart -> cartRequestModel
//                        .flatMap(request -> {
//                            List<String> productIds = request.getProductIds();
//
//                            Cart cartEntity = EntityModelUtil.toCartEntity(request);
//                            cartEntity.setProductIds(productIds);
//                            cartEntity.setId(foundCart.getId());
//                            cartEntity.setCartId(foundCart.getCartId());
//                            return cartRepository.save(cartEntity);
//
//                        })
//                )
//                .flatMap(cart -> {
//                    List<String> productIds = cart.getProductIds();
//                    return productIds
//                            .stream().map(productId -> productClient.getProductByProductId(productId).flux())
//                            .reduce(Flux.empty(), Flux::merge)
//                            .collectList()
//                            .map(products -> EntityModelUtil.toCartResponseModel(cart, products));
//                });
//    }


//instead lets create a removeProductFromCart, UpdateQuantityOfProductInCart, and AddProductInCart methods


@Override
    public Mono<Integer> getCartItemCount(String cartId) {
        return cartRepository.findCartByCartId(cartId)
                .switchIfEmpty(Mono.error(new NotFoundException("Cart not found: " + cartId)))
                .map(cart -> {
                    int count = 0;
                    for (CartProduct product : cart.getProducts()) {
                        count += product.getQuantityInCart();
                    }
                    return count;
                });
    }

}
