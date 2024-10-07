package com.petclinic.cartsservice.businesslayer;

import com.petclinic.cartsservice.dataaccesslayer.Cart;
import com.petclinic.cartsservice.dataaccesslayer.CartRepository;
import com.petclinic.cartsservice.dataaccesslayer.cartproduct.CartProduct;
import com.petclinic.cartsservice.domainclientlayer.ProductClient;
import com.petclinic.cartsservice.domainclientlayer.ProductResponseModel;
import com.petclinic.cartsservice.presentationlayer.CartRequestModel;
import com.petclinic.cartsservice.presentationlayer.CartResponseModel;
import com.petclinic.cartsservice.utils.EntityModelUtil;
import com.petclinic.cartsservice.utils.exceptions.InvalidInputException;
import com.petclinic.cartsservice.utils.exceptions.NotFoundException;
import com.petclinic.cartsservice.utils.exceptions.OutOfStockException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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


//instead lets create a removeProductFromCart, UpdateQuantityOfProductInCart, and AddProductInCart methods
    @Override
    public Mono<CartResponseModel> removeProductFromCart(String cartId, String productId){
        return cartRepository.findCartByCartId(cartId)
                .switchIfEmpty(Mono.defer(() -> Mono.error(new NotFoundException("Cart id was not found:" + cartId))))
                .flatMap(found -> {
                    List<CartProduct> products = found.getProducts();

                    Optional<CartProduct> productToRemove = products.stream()
                            .filter(product -> product.getProductId().equals(productId)).findFirst();

                    if (productToRemove.isPresent()) {
                        products.remove(productToRemove.get());

                        found.setProducts(products);

                        return cartRepository.save(found)
                                .map(updatedCart -> {
                                    return EntityModelUtil.toCartResponseModel(updatedCart, products);
                                });
                    } else {
                        return Mono.error(new NotFoundException("Product id was not found: " + productId));
                    }
                });
    }


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

                    // Clear the products list
                    cart.setProducts(new ArrayList<>());

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
    public Mono<CartResponseModel> assignCartToCustomer(String customerId, List<CartProduct> products) {
        //check if the customer already has a cart
        return cartRepository.findCartByCustomerId(customerId)
                .defaultIfEmpty(new Cart())
                .flatMap(cart -> {
                    //set the customerId if it's a new cart
                    if (cart.getCustomerId() == null) {
                        cart.setCustomerId(customerId);
                        cart.setCartId(UUID.randomUUID().toString()); // Generate a new cart ID
                    }

                    //add or update products in the cart
                    List<CartProduct> updatedProducts = cart.getProducts() != null ? cart.getProducts() : new ArrayList<>();

                    for (CartProduct newProduct : products) {
                        boolean productExists = false;

                        //update quantity if product already exists in the cart
                        for (CartProduct existingProduct : updatedProducts) {
                            if (existingProduct.getProductId().equals(newProduct.getProductId())) {
                                existingProduct.setQuantityInCart(existingProduct.getQuantityInCart() + newProduct.getQuantityInCart());
                                productExists = true;
                                break;
                            }
                        }

                        //if the product is not in the cart, add it
                        if (!productExists) {
                            updatedProducts.add(newProduct);
                        }
                    }

                    cart.setProducts(updatedProducts);

                    //save the cart to the repository
                    return cartRepository.save(cart)
                            .map(savedCart -> EntityModelUtil.toCartResponseModel(savedCart, savedCart.getProducts()));
                });
    }

    @Override
    public Mono<CartResponseModel> addProductToCart(String cartId, String productId, int quantity) {
        // Fetch the latest cart and product information
        return cartRepository.findCartByCartId(cartId)
                .switchIfEmpty(Mono.error(new NotFoundException("Cart not found: " + cartId)))
                .flatMap(cart -> productClient.getProductByProductId(productId)
                        .flatMap(product -> {
                            // Validate if the requested quantity is greater than zero
                            if (quantity <= 0) {
                                return Mono.error(new InvalidInputException("Quantity must be greater than zero."));
                            }
                            // Check if the available stock is sufficient
                            if (product.getProductQuantity() < quantity) {
                                return Mono.error(new OutOfStockException("You cannot add more than "
                                        + product.getProductQuantity() + " items. Only "
                                        + product.getProductQuantity() + " items left in stock."));
                            }

                            // Check if the product already exists in the cart
                            Optional<CartProduct> existingProductOpt = cart.getProducts().stream()
                                    .filter(p -> p.getProductId().equals(productId))
                                    .findFirst();

                            if (existingProductOpt.isPresent()) {
                                // If product is already in the cart, update the quantity
                                CartProduct existingProduct = existingProductOpt.get();
                                int newQuantity = existingProduct.getQuantityInCart() + quantity;
                                if (newQuantity > product.getProductQuantity()) {
                                    return Mono.error(new OutOfStockException("You cannot add more than "
                                            + product.getProductQuantity() + " items. Only "
                                            + product.getProductQuantity() + " items left in stock."));
                                }
                                existingProduct.setQuantityInCart(newQuantity);
                                existingProduct.setProductQuantity(product.getProductQuantity()); // Update stock information
                            } else {
                                // If product is not in the cart, create a new entry
                                CartProduct cartProduct = CartProduct.builder()
                                        .productId(product.getProductId())
                                        .productName(product.getProductName())
                                        .productDescription(product.getProductDescription())
                                        .productSalePrice(product.getProductSalePrice())
                                        .averageRating(product.getAverageRating())
                                        .quantityInCart(quantity)
                                        .productQuantity(product.getProductQuantity()) // Set stock information
                                        .build();
                                cart.getProducts().add(cartProduct);
                            }

                            // Save the updated cart
                            return cartRepository.save(cart)
                                    .map(savedCart -> EntityModelUtil.toCartResponseModel(savedCart, savedCart.getProducts()));
                        })
                );
    }


    @Override
    public Mono<CartResponseModel> updateProductQuantityInCart(String cartId, String productId, int quantity) {
        return cartRepository.findCartByCartId(cartId)
                .switchIfEmpty(Mono.error(new NotFoundException("Cart not found: " + cartId)))
                .flatMap(cart -> productClient.getProductByProductId(productId)
                        .flatMap(product -> {
                            if (quantity <= 0) {
                                return Mono.error(new InvalidInputException("Quantity must be greater than zero."));
                            }
                            if (product.getProductQuantity() < quantity) {
                                return Mono.error(new OutOfStockException("You cannot set quantity more than " + product.getProductQuantity() + " items. Only " + product.getProductQuantity() + " items left in stock."));
                            }

                            Optional<CartProduct> existingProductOpt = cart.getProducts().stream()
                                    .filter(p -> p.getProductId().equals(productId))
                                    .findFirst();

                            if (existingProductOpt.isPresent()) {
                                CartProduct existingProduct = existingProductOpt.get();
                                existingProduct.setQuantityInCart(quantity);
                                existingProduct.setProductQuantity(product.getProductQuantity());
                            } else {
                                return Mono.error(new NotFoundException("Product not found in cart: " + productId));
                            }

                            return cartRepository.save(cart)
                                    .map(savedCart -> EntityModelUtil.toCartResponseModel(savedCart, savedCart.getProducts()));
                        })
                );
    }

    @Override
    public Mono<CartResponseModel> findCartByCustomerId(String customerId) {
        return cartRepository.findCartByCustomerId(customerId)
                .switchIfEmpty(Mono.defer(() -> Mono.error(new NotFoundException("Cart for customer id was not found: " + customerId))))
                .doOnNext(cart -> log.debug("The cart for customer id {} is: {}", customerId, cart.toString()))
                .flatMap(cart -> {
                    List<CartProduct> products = cart.getProducts();
                    return Mono.just(EntityModelUtil.toCartResponseModel(cart, products));
                });
    }

    @Override
    public Mono<CartResponseModel> moveProductFromCartToWishlist(String cartId, String productId) {
        return cartRepository.findCartByCartId(cartId)
                .switchIfEmpty(Mono.error(new NotFoundException("Cart not found: " + cartId)))
                .flatMap(cart -> {
                    return Mono.justOrEmpty(cart.getProducts().stream()
                                    .filter(p -> p.getProductId().equals(productId))
                                    .findFirst())
                            .switchIfEmpty(Mono.error(new NotFoundException("Product not found in cart: " + productId)))
                            .flatMap(cartProduct -> {
                                // Create a new mutable list for cart's wishlist products
                                List<CartProduct> wishListProducts = cart.getWishListProducts() != null
                                        ? new ArrayList<>(cart.getWishListProducts())
                                        : new ArrayList<>();

                                // Add the product to the wishlist
                                wishListProducts.add(cartProduct);
                                cart.setWishListProducts(wishListProducts);

                                // Remove the product from the main cart products list
                                List<CartProduct> updatedProducts = new ArrayList<>(cart.getProducts());
                                updatedProducts.remove(cartProduct);
                                cart.setProducts(updatedProducts);

                                // Save the updated cart and return the response
                                return cartRepository.save(cart)
                                        .map(savedCart -> EntityModelUtil.toCartResponseModel(savedCart, savedCart.getProducts()));
                            });
                });
    }

    @Override
    public Mono<CartResponseModel> moveProductFromWishListToCart(String cartId, String productId) {
        return cartRepository.findCartByCartId(cartId)
                .switchIfEmpty(Mono.error(new NotFoundException("Cart not found: " + cartId)))
                .flatMap(cart -> {
                    return Mono.justOrEmpty(cart.getWishListProducts().stream()
                                    .filter(p -> p.getProductId().equals(productId))
                                    .findFirst())
                            .switchIfEmpty(Mono.error(new NotFoundException("Product: " + productId + " not found in wishlist of cart: " + cartId)))
                            .flatMap(wishListProduct -> {
                                // Create a new list for cart products and wishlist products
                                List<CartProduct> updatedCartProducts = new ArrayList<>(cart.getProducts() != null ? cart.getProducts() : new ArrayList<>());
                                List<CartProduct> updatedWishListProducts = new ArrayList<>(cart.getWishListProducts());

                                // Add the product to the cart and remove it from the wishlist
                                updatedCartProducts.add(wishListProduct);
                                updatedWishListProducts.remove(wishListProduct);

                                // Update the cart with the new lists
                                cart.setProducts(updatedCartProducts);
                                cart.setWishListProducts(updatedWishListProducts);

                                // Save the updated cart and map to CartResponseModel
                                return cartRepository.save(cart)
                                        .map(savedCart -> EntityModelUtil.toCartResponseModel(savedCart, savedCart.getProducts()));
                            });
                });
    }




}
