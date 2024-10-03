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
import java.util.UUID;

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
    public Flux<CartResponseModel> getAllCarts() {
        return cartRepository.findAll()
                .map(cart -> {
                    List<CartProduct> products = cart.getProducts();
                    return EntityModelUtil.toCartResponseModel(cart, products);
                });
    }

    @Override
    public Mono<CartResponseModel> getCartByCartId(String cartId) {
        return cartRepository.findCartByCartId(cartId)
                .switchIfEmpty(Mono.defer(() -> Mono.error(new NotFoundException("Cart id was not found: " + cartId))))
                .doOnNext(e -> log.debug("The cart response entity is: " + e.toString()))
                .flatMap(cart -> {
                    List<CartProduct> products = cart.getProducts();
                    return Mono.just(EntityModelUtil.toCartResponseModel(cart, products));
                });
    }


    public Flux<CartResponseModel> clearCart(String cartId) {
        return cartRepository.findCartByCartId(cartId)
                .switchIfEmpty(Mono.error(new NotFoundException("Cart not found: " + cartId)))
                .flatMapMany(cart -> {
                    List<CartProduct> products = cart.getProducts();
                    cart.setProducts(Collections.emptyList());
                    return cartRepository.save(cart)
                            .thenMany(Flux.fromIterable(products))
                            .map(product -> EntityModelUtil.toCartResponseModel(cart, List.of(product)));
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
     public Mono<CartResponseModel> deleteCartByCartId(String cartId) {
         return cartRepository.findCartByCartId(cartId)
                 .switchIfEmpty(Mono.defer(() -> Mono.error(new NotFoundException("Cart id was not found: " + cartId))))
                 .flatMap(found -> {
                     List<CartProduct> products = found.getProducts();
                     return cartRepository.delete(found)
                             .then(Mono.just(EntityModelUtil.toCartResponseModel(found, products)));
                 });
     }

    @Override
    public Mono<CartResponseModel> checkoutCart(String cartId) {
        return cartRepository.findCartByCartId(cartId)
                .flatMap(cart -> {

                    double subtotal = cart.getProducts().stream()
                            .mapToDouble(product -> product.getProductSalePrice() * product.getQuantityInCart())
                            .sum();


                    double tvq = subtotal * 0.09975;
                    double tvc = subtotal * 0.05;
                    double total = subtotal + tvq + tvc;

                    // Update the cart model
                    cart.setSubtotal(subtotal);
                    cart.setTvq(tvq);
                    cart.setTvc(tvc);
                    cart.setTotal(total);


                    return cartRepository.save(cart)
                            .map(savedCart -> {
                                // Create a response model to send back to the client
                                CartResponseModel responseModel = new CartResponseModel();
                                responseModel.setCartId(savedCart.getCartId());
                                responseModel.setSubtotal(subtotal);
                                responseModel.setTvq(tvq);
                                responseModel.setTvc(tvc);
                                responseModel.setTotal(total);
                                responseModel.setPaymentStatus("Payment Processed");  // Simulated payment status
                                return responseModel;
                            });
                });
    }


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

    @Override
    public Mono<CartResponseModel> createNewCart(CartRequestModel cartRequestModel) {

        Cart cart = new Cart();
        cart.setCustomerId(cartRequestModel.getCustomerId());
        cart.setCartId(UUID.randomUUID().toString());
        Mono<CartResponseModel> cartRequestModelMono = cartRepository.save(cart)
                .map(savedCart -> {
                    CartResponseModel cartResponseModel = new CartResponseModel();
                    cartResponseModel.setCustomerId(savedCart.getCustomerId());
                    cartResponseModel.setCartId(savedCart.getCartId());
                    return cartResponseModel;
                });
        return cartRequestModelMono;
    }



}
