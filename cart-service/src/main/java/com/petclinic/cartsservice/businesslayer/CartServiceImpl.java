package com.petclinic.cartsservice.businesslayer;

import com.petclinic.cartsservice.dataaccesslayer.Cart;
import com.petclinic.cartsservice.dataaccesslayer.CartRepository;
import com.petclinic.cartsservice.dataaccesslayer.cartproduct.CartProduct;
import com.petclinic.cartsservice.domainclientlayer.ProductClient;
import com.petclinic.cartsservice.presentationlayer.CartResponseModel;
import com.petclinic.cartsservice.utils.EntityModelUtil;
import com.petclinic.cartsservice.utils.exceptions.InvalidInputException;
import com.petclinic.cartsservice.utils.exceptions.NotFoundException;
import com.petclinic.cartsservice.utils.exceptions.OutOfStockException;
import com.petclinic.cartsservice.domainclientlayer.CustomerClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Collections;
import java.util.UUID;


@Service
@Slf4j
public class CartServiceImpl implements CartService {
    private final CartRepository cartRepository;
    private final ProductClient productClient;
    private final CustomerClient customerClient;


    public CartServiceImpl(CartRepository cartRepository, ProductClient productClient, CustomerClient customerClient ) {
        this.cartRepository = cartRepository;
        this.productClient = productClient;
        this.customerClient = customerClient;
    }
    @Override
    public Flux<CartResponseModel> getAllCarts() {
        return cartRepository.findAll()
                .flatMap(cart -> {
                    String cid = cart.getCustomerId();
                    if (cid == null || cid.isBlank()) {
                        return Mono.just(EntityModelUtil.toCartResponseModel(cart, cart.getProducts()));
                    }
                    return customerClient.getCustomerById(cid)
                            .map(c -> EntityModelUtil.toCartResponseModel(cart, cart.getProducts(), c.getFullName()))
                            .onErrorResume(e -> Mono.just(EntityModelUtil.toCartResponseModel(cart, cart.getProducts())));
                });
    }

    @Override
    public Mono<CartResponseModel> getCartByCartId(String cartId) {
        return cartRepository.findCartByCartId(cartId)
                .switchIfEmpty(Mono.defer(() -> Mono.error(new NotFoundException("Cart id was not found: " + cartId))))
                .flatMap(cart -> {
                    String cid = cart.getCustomerId();
                    if (cid == null || cid.isBlank()) {
                        return Mono.just(EntityModelUtil.toCartResponseModel(cart, cart.getProducts()));
                    }
                    return customerClient.getCustomerById(cid)
                            .map(c -> EntityModelUtil.toCartResponseModel(cart, cart.getProducts(), c.getFullName()))
                            .onErrorResume(e -> Mono.just(EntityModelUtil.toCartResponseModel(cart, cart.getProducts())));
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
    public Mono<CartResponseModel> removeProductFromWishlist(String cartId, String productId) {
        return cartRepository.findCartByCartId(cartId)
                .switchIfEmpty(Mono.error(new NotFoundException("Cart not found: " + cartId)))
                .flatMap(cart -> {
                    List<CartProduct> wish = cart.getWishListProducts();

                    if (wish == null || wish.isEmpty()) {
                        return Mono.error(new NotFoundException("No wishlist for cart: " + cartId));
                    }

                    boolean removed = wish.removeIf(p -> p.getProductId().equals(productId));
                    if (!removed) {
                        return Mono.error(new NotFoundException("Product not found in wishlist: " + productId));
                    }

                    cart.setWishListProducts(wish);

                    return cartRepository.save(cart)
                            .map(saved -> EntityModelUtil.toCartResponseModel(saved, saved.getWishListProducts()));
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

                    String invoiceId = UUID.randomUUID().toString();
                    List<CartProduct> products = cart.getProducts();
                    double total = calculateTotal(products);

                    // --- Recent Purchases Logic ---
                    List<CartProduct> updatedRecentPurchases = cart.getRecentPurchases() != null
                            ? new ArrayList<>(cart.getRecentPurchases())
                            : new ArrayList<>();

                    for (CartProduct purchasedProduct : products) {
                        boolean found = false;
                        for (CartProduct recent : updatedRecentPurchases) {
                            if (recent.getProductId().equals(purchasedProduct.getProductId())) {
                                recent.setQuantityInCart(
                                        recent.getQuantityInCart() + purchasedProduct.getQuantityInCart()
                                );
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            updatedRecentPurchases.add(purchasedProduct);
                        }
                    }
                    cart.setRecentPurchases(updatedRecentPurchases);

                    // --- Recommendation Purchases Logic ---
                    // Only recommend products bought 3+ times
                    Map<String, Integer> purchaseCounts = new HashMap<>();
                    for (CartProduct p : updatedRecentPurchases) {
                        purchaseCounts.put(
                                p.getProductId(),
                                purchaseCounts.getOrDefault(p.getProductId(), 0) + p.getQuantityInCart()
                        );
                    }
                    List<CartProduct> recommended = new ArrayList<>();
                    for (CartProduct p : updatedRecentPurchases) {
                        if (purchaseCounts.get(p.getProductId()) >= 3) {
                            // Deduplicate by productId
                            if (recommended.stream().noneMatch(r -> r.getProductId().equals(p.getProductId()))) {
                                recommended.add(p);
                            }
                        }
                    }
                    cart.setRecommendationPurchase(recommended);

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

    private Mono<Cart> createNewCartForCustomer(String customerId) {
        Cart newCart = new Cart();
        newCart.setCustomerId(customerId);
        newCart.setCartId(UUID.randomUUID().toString());
        newCart.setProducts(new ArrayList<>());
        return cartRepository.save(newCart);
    }

    @Override
    public Mono<CartResponseModel> findCartByCustomerId(String customerId) {
        if (customerId == null || customerId.trim().isEmpty()) {
            return Mono.error(new InvalidInputException("customerId must not be null or empty"));
        }
        return cartRepository.findCartByCustomerId(customerId)
                .switchIfEmpty(Mono.defer(() -> createNewCartForCustomer(customerId)))
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

    // move whole wishlist into cart
    @Override
    public Mono<CartResponseModel> moveAllWishlistToCart(String cartId) {
        return cartRepository.findCartByCartId(cartId)
                .switchIfEmpty(Mono.error(new NotFoundException("Cart not found: " + cartId)))
                .flatMap(cart -> {
                    List<CartProduct> wishlist = cart.getWishListProducts();
                    if (wishlist == null || wishlist.isEmpty()) {
                        CartResponseModel resp = EntityModelUtil.toCartResponseModel(cart, cart.getProducts());
                        resp.setMessage("No items in wishlist to move.");
                        return Mono.just(resp);
                    }

                    final List<CartProduct> wl = new ArrayList<>(wishlist);
                    final List<CartProduct> cartLines =
                            new ArrayList<>(cart.getProducts() != null ? cart.getProducts() : new ArrayList<>());

                    Map<String, Integer> wishQtyById = new HashMap<>();
                    Map<String, CartProduct> exemplarById = new HashMap<>();

                    for (CartProduct w : wl) {
                        String pid = w.getProductId();
                        if (pid == null || pid.isBlank()) continue;

                        int qty = (w.getQuantityInCart() == null || w.getQuantityInCart() <= 0) ? 1 : w.getQuantityInCart();
                        wishQtyById.merge(pid, Integer.valueOf(qty), (a, b) -> a + b);
                        exemplarById.putIfAbsent(pid, w);
                    }

                    int moved = 0;
                    for (Map.Entry<String, Integer> e : wishQtyById.entrySet()) {
                        String pid = e.getKey();
                        int qtyToMove = e.getValue();
                        CartProduct sample = exemplarById.get(pid);
                        if (sample == null) continue;

                        Optional<CartProduct> existingOpt = cartLines.stream()
                                .filter(p -> Objects.equals(p.getProductId(), pid))
                                .findFirst();

                        if (existingOpt.isPresent()) {
                            CartProduct existing = existingOpt.get();
                            int base = existing.getQuantityInCart() == null ? 0 : existing.getQuantityInCart();
                            existing.setQuantityInCart(base + qtyToMove);
                            if (sample.getProductQuantity() != null) {
                                existing.setProductQuantity(sample.getProductQuantity());
                            }
                        } else {
                            cartLines.add(CartProduct.builder()
                                    .productId(sample.getProductId())
                                    .imageId(sample.getImageId())
                                    .productName(sample.getProductName())
                                    .productDescription(sample.getProductDescription())
                                    .productSalePrice(sample.getProductSalePrice())
                                    .averageRating(sample.getAverageRating())
                                    .quantityInCart(qtyToMove)
                                    .productQuantity(sample.getProductQuantity())
                                    .build());
                        }
                        moved += qtyToMove;
                    }

                    cart.setProducts(cartLines);
                    cart.setWishListProducts(new ArrayList<>());

                    final int movedFinal = moved;
                    return cartRepository.save(cart)
                            .map(saved -> {
                                CartResponseModel resp = EntityModelUtil.toCartResponseModel(saved, saved.getProducts());
                                resp.setMessage("Moved " + movedFinal + " item(s) to cart.");
                                return resp;
                            });
                });
    }

    @Override
    public Mono<List<CartProduct>> getRecentPurchases(String cartId) {
        return cartRepository.findCartByCartId(cartId)
                .map(cart -> cart.getRecentPurchases() != null ? cart.getRecentPurchases() : List.of());
    }

    @Override
    public Mono<List<CartProduct>> getRecommendationPurchases(String cartId) {
        return cartRepository.findCartByCartId(cartId)
                .map(cart -> cart.getRecommendationPurchase() != null ? cart.getRecommendationPurchase() : List.of());
    }

    @Override
    public Mono<CartResponseModel> applyPromoToCart(String cartId, Double promoPercent) {
        return cartRepository.findCartByCartId(cartId)
                .switchIfEmpty(Mono.error(new NotFoundException("Cart not found: " + cartId)))
                .flatMap(cart -> {
                    if (promoPercent == null || promoPercent <= 0) {
                        cart.setPromoPercent(null);
                        return cartRepository.save(cart);
                    }
                    if (promoPercent > 100) {
                        return Mono.error(new InvalidInputException("promoPercent must be 1..100"));
                    }
                    cart.setPromoPercent(promoPercent);
                    return cartRepository.save(cart);
                })
                .map(saved -> EntityModelUtil.toCartResponseModel(saved, saved.getProducts()));
    }

    @Override
    public Mono<Integer> removeProductFromAllCarts(String productId) {
        return cartRepository.findAll()
                .flatMap(cart -> {
                    boolean changed = false;

                    if (cart.getProducts() != null) {
                        int before = cart.getProducts().size();
                        cart.getProducts().removeIf(p -> productId.equals(p.getProductId()));
                        changed |= (cart.getProducts().size() != before);
                    }

                    if (cart.getWishListProducts() != null) {
                        int beforeW = cart.getWishListProducts().size();
                        cart.getWishListProducts().removeIf(p -> productId.equals(p.getProductId()));
                        changed |= (cart.getWishListProducts().size() != beforeW);
                    }

                    return changed ? cartRepository.save(cart).thenReturn(1) : Mono.just(0);
                })
                .reduce(0, Integer::sum);
    }


}
