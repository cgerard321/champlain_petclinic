package com.petclinic.cartsservice.businesslayer;

import com.petclinic.cartsservice.dataaccesslayer.Cart;
import com.petclinic.cartsservice.dataaccesslayer.CartRepository;
import com.petclinic.cartsservice.dataaccesslayer.cartproduct.CartProduct;
import com.petclinic.cartsservice.domainclientlayer.ProductResponseModel;
import com.petclinic.cartsservice.presentationlayer.CartRequestModel;
import com.petclinic.cartsservice.presentationlayer.CartResponseModel;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface CartService {


    public Mono<CartResponseModel> getCartByCartId(String cartId);
    public Flux<CartResponseModel> getAllCarts();
//    Mono<CartResponseModel> updateCartByCartId(Mono<CartRequestModel> cartRequestModel, String cartId);
    // Combining both features: clearCart and updateCartByCartId
    Flux<CartResponseModel> clearCart(String cartId);  // From feat/CART-CPC-1144_clear_cart_feature

    //Mono<CartResponseModel> updateCartByCartId(Mono<CartRequestModel> cartRequestModel, String cartId);

    Mono<Integer> getCartItemCount(String cartId);
    Mono<CartResponseModel> deleteCartByCartId(String cartId);
    Mono<CartResponseModel> removeProductFromCart(String cartId, String productId);
    Mono<CartResponseModel> addProductToCart(String cartId, String productId, int quantity);
    Mono<CartResponseModel> updateProductQuantityInCart(String cartId, String productId, int quantity);

    Mono<CartResponseModel> checkoutCart(String cartId);

    public Mono<CartResponseModel> assignCartToCustomer(String customerId, List<CartProduct> products);

    public Mono<CartResponseModel>  findCartByCustomerId(String customerId);
    Mono<CartResponseModel> removeProductFromCart(String cartId, String productId);
}

