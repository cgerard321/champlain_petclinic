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
    public Mono<CartResponseModel> checkoutCart(final String cartId) {
        return cartRepository.findCartByCartId(cartId)
                .switchIfEmpty(Mono.error(new NotFoundException("Cart not found: " + cartId)))
                .flatMap(cart -> {
                    if (cart.getProducts().isEmpty()) {
                        return Mono.error(new InvalidInputException("Cart is empty"));
                    }

                    // Create the invoice directly without a separate class
                    String invoiceId = UUID.randomUUID().toString();
                    List<CartProduct> products = cart.getProducts();
                    double total = calculateTotal(products);

                    // Log the invoice data (optional)
                    log.info("Generated Invoice: ID: {}, Cart ID: {}, Total: {}", invoiceId, cartId, total);

                    // Clear the cart after checkout
                    cart.setProducts(Collections.emptyList());
                    return cartRepository.save(cart)
                            .then(Mono.just(new CartResponseModel(invoiceId, cartId, products, total)));
                });
    }
    private double calculateTotal(List<CartProduct> products) {
        return products.stream()
                .mapToDouble(product -> product.getProductSalePrice() * product.getQuantityInCart())
                .sum();
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
                            // Check if the product is out of stock
                            if (product.getProductQuantity() == 0) {
                                // The product is out of stock, move it to wishlist
                                // Create a CartProduct for the wishlist
                                CartProduct wishListProduct = CartProduct.builder()
                                        .productId(product.getProductId())
                                        .imageId(product.getImageId())
                                        .productName(product.getProductName())
                                        .productDescription(product.getProductDescription())
                                        .productSalePrice(product.getProductSalePrice())
                                        .averageRating(product.getAverageRating())
                                        .quantityInCart(0)
                                        .productQuantity(product.getProductQuantity())
                                        .build();
                                // Add the product to the wishlist
                                if (cart.getWishListProducts() == null) {
                                    cart.setWishListProducts(new ArrayList<>());
                                }
                                // Check if the product already exists in the wishlist
                                Optional<CartProduct> existingWishlistProductOpt = cart.getWishListProducts().stream()
                                        .filter(p -> p.getProductId().equals(productId))
                                        .findFirst();
                                if (existingWishlistProductOpt.isEmpty()) {
                                    cart.getWishListProducts().add(wishListProduct);
                                }
                                // Save the cart
                                return cartRepository.save(cart)
                                        .map(savedCart -> {
                                            CartResponseModel responseModel = EntityModelUtil.toCartResponseModel(savedCart, savedCart.getProducts());
                                            responseModel.setMessage("Product is out of stock and has been moved to your wishlist.");
                                            return responseModel;
                                        });
                            }
                            // Check if the available stock is sufficient
                            else if (product.getProductQuantity() < quantity) {
                                return Mono.error(new OutOfStockException("You cannot add more than "
                                        + product.getProductQuantity() + " items. Only "
                                        + product.getProductQuantity() + " items left in stock."));
                            } else {
                                // Proceed to add product to cart as before
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
                                            .imageId(product.getImageId())
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
                            }
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
                    // Find the product in the main cart
                    CartProduct cartProduct = cart.getProducts().stream()
                            .filter(p -> p.getProductId().equals(productId))
                            .findFirst()
                            .orElseThrow(() -> new NotFoundException("Product not found in cart: " + productId));

                    // Create new mutable lists for products and wishlist
                    List<CartProduct> updatedProducts = new ArrayList<>(cart.getProducts());
                    List<CartProduct> updatedWishListProducts = cart.getWishListProducts() != null
                            ? new ArrayList<>(cart.getWishListProducts())
                            : new ArrayList<>();

                    // Add the product to the wishlist and remove it from the cart
                    updatedWishListProducts.add(cartProduct);
                    updatedProducts.remove(cartProduct);

                    // Update the cart with the new lists
                    cart.setProducts(updatedProducts);
                    cart.setWishListProducts(updatedWishListProducts);

                    // Save the updated cart and map to CartResponseModel
                    return cartRepository.save(cart)
                            .map(savedCart -> EntityModelUtil.toCartResponseModel(savedCart, savedCart.getProducts()));
                });
    }



    public Mono<CartResponseModel> moveProductFromWishListToCart(String cartId, String productId) {
        return cartRepository.findCartByCartId(cartId)
                .switchIfEmpty(Mono.error(new NotFoundException("Cart not found: " + cartId)))
                .flatMap(cart -> {
                    // Find the product in the wishlist
                    CartProduct wishListProduct = cart.getWishListProducts().stream()
                            .filter(p -> p.getProductId().equals(productId))
                            .findFirst()
                            .orElseThrow(() -> new NotFoundException("Product not found in wishlist: " + productId));

                    // Check if the product is out of stock
                    if (wishListProduct.getProductQuantity() == 0) {
                        return Mono.error(new OutOfStockException("Product is out of stock and cannot be added to the cart."));
                    }

                    // Create new mutable lists for products and wishlist
                    List<CartProduct> updatedProducts = new ArrayList<>(cart.getProducts());
                    List<CartProduct> updatedWishListProducts = new ArrayList<>(cart.getWishListProducts());

                    // Add the product to the cart's products list and remove it from the wishlist
                    updatedProducts.add(wishListProduct);
                    updatedWishListProducts.remove(wishListProduct);

                    // Update the cart with the new lists
                    cart.setProducts(updatedProducts);
                    cart.setWishListProducts(updatedWishListProducts);

                    // Save the updated cart and map to CartResponseModel
                    return cartRepository.save(cart)
                            .map(savedCart -> EntityModelUtil.toCartResponseModel(savedCart, savedCart.getProducts()));
                });
    }



    @Override
    public Mono<CartResponseModel> addProductToCartFromProducts(String cartId, String productId) {
        //fetch the latest cart and product information
        return cartRepository.findCartByCartId(cartId)
                .switchIfEmpty(Mono.error(new NotFoundException("Cart not found: " + cartId)))
                .flatMap(cart -> productClient.getProductByProductId(productId)
                        .flatMap(product -> {
                            //validate if the product is out of stock
                            if (product.getProductQuantity() == 0) {
                                //the product is out of stock, move it to wishlist
                                CartProduct wishListProduct = CartProduct.builder()
                                        .productId(product.getProductId())
                                        .imageId(product.getImageId())
                                        .productName(product.getProductName())
                                        .productDescription(product.getProductDescription())
                                        .productSalePrice(product.getProductSalePrice())
                                        .averageRating(product.getAverageRating())
                                        .quantityInCart(0)
                                        .productQuantity(product.getProductQuantity())
                                        .build();

                                if (cart.getWishListProducts() == null) {
                                    cart.setWishListProducts(new ArrayList<>());
                                }

                                //check if the product already exists in the wishlist
                                Optional<CartProduct> existingWishlistProductOpt = cart.getWishListProducts().stream()
                                        .filter(p -> p.getProductId().equals(productId))
                                        .findFirst();

                                if (existingWishlistProductOpt.isEmpty()) {
                                    cart.getWishListProducts().add(wishListProduct);
                                }

                                return cartRepository.save(cart)
                                        .map(savedCart -> {
                                            CartResponseModel responseModel = EntityModelUtil.toCartResponseModel(savedCart, savedCart.getProducts());
                                            responseModel.setMessage("Product is out of stock and has been moved to your wishlist.");
                                            return responseModel;
                                        });
                            } else {
                                //if product is available in stock
                                Optional<CartProduct> existingProductOpt = cart.getProducts().stream()
                                        .filter(p -> p.getProductId().equals(productId))
                                        .findFirst();

                                if (existingProductOpt.isPresent()) {
                                    //if product already exists in cart, increment quantity by 1
                                    CartProduct existingProduct = existingProductOpt.get();
                                    int newQuantity = existingProduct.getQuantityInCart() + 1;

                                    if (newQuantity > product.getProductQuantity()) {
                                        return Mono.error(new OutOfStockException("Cannot add more than "
                                                + product.getProductQuantity() + " items. Only "
                                                + product.getProductQuantity() + " items left in stock."));
                                    }

                                    existingProduct.setQuantityInCart(newQuantity);
                                    existingProduct.setProductQuantity(product.getProductQuantity());
                                } else {
                                    //add new product to the cart
                                    CartProduct cartProduct = CartProduct.builder()
                                            .productId(product.getProductId())
                                            .imageId(product.getImageId())
                                            .productName(product.getProductName())
                                            .productDescription(product.getProductDescription())
                                            .productSalePrice(product.getProductSalePrice())
                                            .averageRating(product.getAverageRating())
                                            .quantityInCart(1)
                                            .productQuantity(product.getProductQuantity())
                                            .build();

                                    cart.getProducts().add(cartProduct);
                                }

                                return cartRepository.save(cart)
                                        .map(savedCart -> EntityModelUtil.toCartResponseModel(savedCart, savedCart.getProducts()));
                            }
                        })
                );
    }

    @Override
    public Mono<CartResponseModel> addProductToWishList(String cartId, String productId, int quantity) {
        return cartRepository.findCartByCartId(cartId)
                .switchIfEmpty(Mono.error(new NotFoundException("Cart not found: " + cartId)))
                .flatMap(cart -> productClient.getProductByProductId(productId)
                        .switchIfEmpty(Mono.error(new NotFoundException("Product not found: " + productId)))
                        .flatMap(product -> {
                            if (quantity <= 0) {
                                return Mono.error(new InvalidInputException("Quantity must be greater than zero"));
                            }
                            if (product.getProductQuantity() < quantity) {
                                return Mono.error(new OutOfStockException("Only "  + product.getProductQuantity() + " items left in stock. You added: " + quantity));
                            }

                            // Create a mutable copy of the wishlist
                            List<CartProduct> wishListProducts = cart.getWishListProducts() != null
                                    ? new ArrayList<>(cart.getWishListProducts())
                                    : new ArrayList<>();

                            Optional<CartProduct> existingProductOpt = wishListProducts.stream()
                                    .filter(p -> p.getProductId().equals(productId))
                                    .findFirst();

                            if (existingProductOpt.isPresent()) {
                                CartProduct existingProduct = existingProductOpt.get();
                                existingProduct.setQuantityInCart(existingProduct.getQuantityInCart() + quantity);
                                existingProduct.setProductQuantity(product.getProductQuantity());
                            } else {
                                CartProduct cartProduct = CartProduct.builder()
                                        .productId(product.getProductId())
                                        .productName(product.getProductName())
                                        .imageId(product.getImageId())
                                        .productDescription(product.getProductDescription())
                                        .productSalePrice(product.getProductSalePrice())
                                        .averageRating(product.getAverageRating())
                                        .quantityInCart(quantity)
                                        .productQuantity(product.getProductQuantity())
                                        .build();
                                wishListProducts.add(cartProduct);
                            }

                            // Update the cart with the modified wishlist
                            cart.setWishListProducts(wishListProducts);

                            return cartRepository.save(cart)
                                    .map(savedCart -> EntityModelUtil.toCartResponseModel(savedCart, savedCart.getWishListProducts()));
                        })
                );
    }


}
