package com.petclinic.cartsservice.businesslayer;

import com.petclinic.cartsservice.dataaccesslayer.Cart;
import com.petclinic.cartsservice.dataaccesslayer.CartRepository;
import com.petclinic.cartsservice.dataaccesslayer.cartproduct.CartProduct;
import com.petclinic.cartsservice.domainclientlayer.CartItemRequestModel;
import com.petclinic.cartsservice.domainclientlayer.ProductClient;
import com.petclinic.cartsservice.domainclientlayer.ProductResponseModel;
import com.petclinic.cartsservice.presentationlayer.CartResponseModel;
import com.petclinic.cartsservice.presentationlayer.WishlistItemRequestModel;
import com.petclinic.cartsservice.presentationlayer.WishlistTransferDirection;
import com.petclinic.cartsservice.utils.EntityModelUtil;
import com.petclinic.cartsservice.utils.exceptions.InvalidInputException;
import com.petclinic.cartsservice.utils.exceptions.NotFoundException;
import com.petclinic.cartsservice.utils.exceptions.OutOfStockException;
import com.petclinic.cartsservice.domainclientlayer.CustomerClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;


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
    private static final int DEFAULT_PAGE_SIZE = 50;
    private static final int MAX_PAGE_SIZE = 200;

    @Override
    public Flux<CartResponseModel> getAllCarts(CartQueryCriteria criteria) {
        CartQueryCriteria effectiveCriteria = criteria == null ? CartQueryCriteria.builder().build() : criteria;

        int page = effectiveCriteria.resolvedPage();
        int size = effectiveCriteria.resolvedSize(DEFAULT_PAGE_SIZE, MAX_PAGE_SIZE);
        String normalizedCustomerId = effectiveCriteria.normalizedCustomerId();
        String normalizedCustomerName = effectiveCriteria.normalizedCustomerName();
        Boolean assigned = effectiveCriteria.getAssigned();

        Flux<Cart> cartsFlux = cartRepository.findAll();

        if (normalizedCustomerId != null && !normalizedCustomerId.isBlank()) {
            cartsFlux = cartsFlux.filter(cart -> normalizedCustomerId.equalsIgnoreCase(valueOrEmpty(cart.getCustomerId())));
        }

        if (assigned != null) {
            if (assigned) {
                cartsFlux = cartsFlux.filter(cart -> !valueOrEmpty(cart.getCustomerId()).isBlank());
            } else {
                cartsFlux = cartsFlux.filter(cart -> valueOrEmpty(cart.getCustomerId()).isBlank());
            }
        }

        Flux<CartResponseModel> responseFlux = cartsFlux.flatMap(this::toCartResponseModelWithCustomer);

        if (normalizedCustomerName != null && !normalizedCustomerName.isBlank()) {
            String lowered = normalizedCustomerName.toLowerCase();
            responseFlux = responseFlux.filter(model -> {
                String candidate = valueOrEmpty(model.getCustomerName()).toLowerCase();
                return !candidate.isBlank() && candidate.contains(lowered);
            });
        }

        long itemsToSkip = (long) page * size;

        return responseFlux
                .skip(itemsToSkip)
                .take(size);
    }

    private Mono<CartResponseModel> toCartResponseModelWithCustomer(Cart cart) {
        List<CartProduct> safeProducts = cart.getProducts() == null ? Collections.emptyList() : cart.getProducts();
        String cid = cart.getCustomerId();
        if (cid == null || cid.isBlank()) {
            return Mono.just(EntityModelUtil.toCartResponseModel(cart, safeProducts));
        }
        return customerClient.getCustomerById(cid)
                .map(c -> EntityModelUtil.toCartResponseModel(cart, safeProducts, c.getFullName()))
                .onErrorResume(e -> Mono.just(EntityModelUtil.toCartResponseModel(cart, safeProducts)));
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }

    private int resolveAvailableQuantity(ProductResponseModel product) {
        if (product == null) {
            return 0;
        }
        Integer quantity = product.getProductQuantity();
        if (quantity == null) {
            Integer fallback = product.getProductStock();
            if (fallback == null) {
                fallback = product.getQuantity();
            }
            quantity = fallback;
        }
        if (quantity == null) {
            return 0;
        }
        return Math.max(quantity, 0);
    }

    private int safeQuantity(Integer quantity) {
        return quantity == null ? 0 : Math.max(quantity, 0);
    }

    private int safeQuantity(Integer quantity, int fallback) {
        if (quantity == null || quantity <= 0) {
            return Math.max(fallback, 0);
        }
        return quantity;
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

    public Mono<Void> deleteAllItemsInCart(String cartId) {
        return cartRepository.findCartByCartId(cartId)
                .switchIfEmpty(Mono.error(new NotFoundException("Cart not found: " + cartId)))
                .flatMap(cart -> {
                    cart.setProducts(Collections.emptyList());
                    return cartRepository.save(cart).then();
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
                                .map(updatedCart -> EntityModelUtil.toCartResponseModel(updatedCart, products));
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
    public Mono<CartResponseModel> assignCartToCustomer(String customerId) {
        final String normalizedCustomerId = customerId == null ? null : customerId.trim();
        if (normalizedCustomerId == null || normalizedCustomerId.isBlank()) {
            return Mono.error(new InvalidInputException("Customer ID must be provided."));
        }

        return cartRepository.findCartByCustomerId(normalizedCustomerId)
                .flatMap(this::toCartResponseModelWithCustomer)
                .switchIfEmpty(Mono.defer(() -> {
                    Cart newCart = Cart.builder()
                            .cartId(UUID.randomUUID().toString())
                            .customerId(normalizedCustomerId)
                            .products(new ArrayList<>())
                            .wishListProducts(new ArrayList<>())
                            .recentPurchases(new ArrayList<>())
                            .recommendationPurchase(new ArrayList<>())
                            .subtotal(0.0)
                            .tvq(0.0)
                            .tvc(0.0)
                            .total(0.0)
                            .build();

                    return cartRepository.save(newCart)
                            .flatMap(this::toCartResponseModelWithCustomer);
                }));
    }

    @Override
    public Mono<CartResponseModel> addProductToCart(String cartId, CartItemRequestModel cartItemRequestModel) {
        if (cartItemRequestModel == null || cartItemRequestModel.getProductId() == null || cartItemRequestModel.getProductId().trim().isEmpty()) {
            return Mono.error(new InvalidInputException("Product ID must be provided."));
        }

        final String productId = cartItemRequestModel.getProductId().trim();
        final int quantity = cartItemRequestModel.resolveQuantity();

        // Fetch the latest cart and product information
        return cartRepository.findCartByCartId(cartId)
                .switchIfEmpty(Mono.error(new NotFoundException("Cart not found: " + cartId)))
                .flatMap(cart -> {
                    if (cart.getProducts() == null) {
                        cart.setProducts(new ArrayList<>());
                    }

                    return productClient.getProductByProductId(productId)
                            .flatMap(product -> {
                                if (quantity <= 0) {
                                    return Mono.error(new InvalidInputException("Quantity must be greater than zero."));
                                }

                                int availableStock = resolveAvailableQuantity(product);
                                String productName = product.getProductName();
                                String readableName = (productName == null || productName.isBlank())
                                        ? "Product"
                                        : productName;

                                if (availableStock <= 0) {
                                    return Mono.error(new OutOfStockException(readableName
                                            + " is out of stock and cannot be added to the cart."));
                                }

                                if (availableStock < quantity) {
                                    return Mono.error(new OutOfStockException("You cannot add more than "
                                            + availableStock + " item(s). Only " + availableStock + " items left in stock."));
                                }

                                Optional<CartProduct> existingProductOpt = cart.getProducts().stream()
                                        .filter(p -> p.getProductId().equals(productId))
                                        .findFirst();

                                if (existingProductOpt.isPresent()) {
                                    CartProduct existingProduct = existingProductOpt.get();
                                    int currentQuantity = safeQuantity(existingProduct.getQuantityInCart());
                                    int newQuantity = currentQuantity + quantity;
                                    if (newQuantity > availableStock) {
                                        return Mono.error(new OutOfStockException("You cannot add more than "
                                                + availableStock + " item(s). Only " + availableStock + " items left in stock."));
                                    }
                                    existingProduct.setQuantityInCart(newQuantity);
                                    existingProduct.setProductQuantity(availableStock);
                                } else {
                                    CartProduct cartProduct = CartProduct.builder()
                                            .productId(product.getProductId())
                                            .imageId(product.getImageId())
                                            .productName(product.getProductName())
                                            .productDescription(product.getProductDescription())
                                            .productSalePrice(product.getProductSalePrice())
                                            .averageRating(product.getAverageRating())
                                            .quantityInCart(quantity)
                                            .productQuantity(availableStock)
                                            .build();
                                    cart.getProducts().add(cartProduct);
                                }

                                List<CartProduct> wishlist = cart.getWishListProducts();
                                if (wishlist != null && !wishlist.isEmpty()) {
                                    List<CartProduct> updatedWishlist = new ArrayList<>(wishlist);
                                    if (updatedWishlist.removeIf(p -> productId.equals(p.getProductId()))) {
                                        cart.setWishListProducts(updatedWishlist);
                                    }
                                }

                                return cartRepository.save(cart)
                                        .map(savedCart -> EntityModelUtil.toCartResponseModel(savedCart, savedCart.getProducts()));
                            });
        });
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
                            int availableStock = resolveAvailableQuantity(product);
                            if (availableStock <= 0) {
                                return Mono.error(new OutOfStockException("Product is out of stock and cannot be updated in the cart."));
                            }
                            if (availableStock < quantity) {
                                return Mono.error(new OutOfStockException("You cannot set quantity more than "
                                        + availableStock + " item(s). Only " + availableStock + " items left in stock."));
                            }

                            Optional<CartProduct> existingProductOpt = cart.getProducts().stream()
                                    .filter(p -> p.getProductId().equals(productId))
                                    .findFirst();

                            if (existingProductOpt.isPresent()) {
                                CartProduct existingProduct = existingProductOpt.get();
                                existingProduct.setQuantityInCart(quantity);
                                existingProduct.setProductQuantity(availableStock);
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
    public Mono<CartResponseModel> addProductToWishlist(String cartId, WishlistItemRequestModel requestModel) {
    if (requestModel == null || requestModel.getProductId() == null || requestModel.getProductId().trim().isEmpty()) {
        return Mono.error(new InvalidInputException("Product ID must be provided."));
    }

        final String productId = requestModel.getProductId().trim();
        if (productId.length() != 36) {
            return Mono.error(new InvalidInputException("Provided product id is invalid: " + productId));
        }
    final int quantity = requestModel.resolveQuantity();

    return cartRepository.findCartByCartId(cartId)
        .switchIfEmpty(Mono.error(new NotFoundException("Cart not found: " + cartId)))
        .flatMap(cart -> productClient.getProductByProductId(productId)
            .switchIfEmpty(Mono.error(new NotFoundException("Product not found: " + productId)))
            .flatMap(product -> {
                if (quantity <= 0) {
                return Mono.error(new InvalidInputException("Quantity must be greater than zero"));
                }

                int availableStock = resolveAvailableQuantity(product);

                List<CartProduct> wishListProducts = cart.getWishListProducts() != null
                    ? new ArrayList<>(cart.getWishListProducts())
                    : new ArrayList<>();

                Optional<CartProduct> existingProductOpt = wishListProducts.stream()
                    .filter(p -> p.getProductId().equals(productId))
                    .findFirst();

                if (existingProductOpt.isPresent()) {
                CartProduct existingProduct = existingProductOpt.get();
                int currentQty = existingProduct.getQuantityInCart() == null ? 0 : existingProduct.getQuantityInCart();
                existingProduct.setQuantityInCart(currentQty + quantity);
                existingProduct.setProductQuantity(availableStock);
                } else {
                CartProduct cartProduct = CartProduct.builder()
                    .productId(product.getProductId())
                    .productName(product.getProductName())
                    .imageId(product.getImageId())
                    .productDescription(product.getProductDescription())
                    .productSalePrice(product.getProductSalePrice())
                    .averageRating(product.getAverageRating())
                    .quantityInCart(quantity)
                    .productQuantity(availableStock)
                    .build();
                wishListProducts.add(cartProduct);
                }

                cart.setWishListProducts(wishListProducts);

                return cartRepository.save(cart)
                    .map(savedCart -> EntityModelUtil.toCartResponseModel(savedCart, savedCart.getProducts() != null ? savedCart.getProducts() : Collections.emptyList()));
            })
        );
    }

    // transfer wishlist/cart items based on direction
    @Override
    public Mono<CartResponseModel> transferWishlist(String cartId, List<String> productIds, WishlistTransferDirection direction) {
        final List<String> normalizedIds = productIds == null ? List.of() : productIds.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(id -> !id.isEmpty())
                .distinct()
                .collect(Collectors.toList());

        final WishlistTransferDirection effectiveDirection = direction == null
                ? WishlistTransferDirection.defaultDirection()
                : direction;

        return cartRepository.findCartByCartId(cartId)
                .switchIfEmpty(Mono.error(new NotFoundException("Cart not found: " + cartId)))
                .flatMap(cart -> {
                    if (effectiveDirection == WishlistTransferDirection.TO_CART) {
                        return moveWishlistToCart(cart, normalizedIds);
                    }
                    return moveCartToWishlist(cart, normalizedIds);
                });
    }

    private Mono<CartResponseModel> moveWishlistToCart(Cart cart, List<String> normalizedIds) {
        List<CartProduct> wishlist = cart.getWishListProducts();
        if (wishlist == null || wishlist.isEmpty()) {
            CartResponseModel resp = EntityModelUtil.toCartResponseModel(cart, cart.getProducts());
            resp.setMessage("No items in wishlist to move.");
            return Mono.just(resp);
        }

        final boolean moveAll = normalizedIds.isEmpty();
        final Set<String> targetIds = moveAll
                ? Collections.emptySet()
                : normalizedIds.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(id -> !id.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        List<CartProduct> selectedItems = wishlist.stream()
                .filter(item -> moveAll || (item.getProductId() != null && targetIds.contains(item.getProductId())))
                .collect(Collectors.toList());

        if (!moveAll && selectedItems.isEmpty()) {
            return Mono.error(new NotFoundException("No wishlist items matched the requested product IDs."));
        }

        Set<String> distinctProductIds = selectedItems.stream()
                .map(CartProduct::getProductId)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(id -> !id.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (distinctProductIds.isEmpty()) {
            if (moveAll) {
                CartResponseModel resp = EntityModelUtil.toCartResponseModel(cart, cart.getProducts());
                resp.setMessage("No wishlist items were eligible to move.");
                return Mono.just(resp);
            }
            return Mono.error(new NotFoundException("No wishlist items matched the requested product IDs."));
        }

    return Flux.fromIterable(distinctProductIds)
        .concatMap(productId -> {
            Mono<ProductResponseModel> productMono = productClient.getProductByProductId(productId);
            if (productMono == null) {
            return Mono.just(WishlistProductAvailability.missing(productId));
            }
            return productMono
                .map(product -> WishlistProductAvailability.fromProduct(productId, product, resolveAvailableQuantity(product)))
                .switchIfEmpty(Mono.just(WishlistProductAvailability.missing(productId)))
                .onErrorResume(NotFoundException.class, e -> Mono.just(WishlistProductAvailability.missing(productId)))
                .onErrorResume(InvalidInputException.class, e -> Mono.just(WishlistProductAvailability.missing(productId)));
        })
                .collectMap(WishlistProductAvailability::getProductId, availability -> availability, LinkedHashMap::new)
                .flatMap(availabilityMap -> processWishlistMovement(cart, wishlist, availabilityMap, moveAll, targetIds));
    }

    private Mono<CartResponseModel> processWishlistMovement(
            Cart cart,
            List<CartProduct> wishlist,
            Map<String, WishlistProductAvailability> availabilityMap,
            boolean moveAll,
            Set<String> targetIds) {

        List<CartProduct> cartLines = cart.getProducts() != null ? new ArrayList<>(cart.getProducts()) : new ArrayList<>();
        Map<String, CartProduct> cartById = cartLines.stream()
                .filter(p -> p.getProductId() != null && !p.getProductId().isBlank())
                .collect(Collectors.toMap(CartProduct::getProductId, p -> p, (existing, replacement) -> existing, LinkedHashMap::new));

        availabilityMap.values().forEach(info -> {
            CartProduct existingLine = cartById.get(info.getProductId());
            if (existingLine != null) {
                info.setExistingInCart(safeQuantity(existingLine.getQuantityInCart()));
            }
        });

        List<CartProduct> updatedWishlist = new ArrayList<>();
        int movedCount = 0;
        int skippedCount = 0;
        Set<String> skippedNames = new LinkedHashSet<>();

        for (CartProduct item : wishlist) {
            String productId = item.getProductId();
            boolean selected = moveAll || (productId != null && targetIds.contains(productId));
            WishlistProductAvailability availability = productId == null ? null : availabilityMap.get(productId);

            if (availability != null) {
                item.setProductQuantity(availability.getAvailableStock());
            }

            if (!selected) {
                updatedWishlist.add(item);
                continue;
            }

            if (availability == null) {
                int requestedQty = safeQuantity(item.getQuantityInCart(), 1);
                skippedCount += requestedQty;
                skippedNames.add(valueOrEmpty(item.getProductName()));
                updatedWishlist.add(item);
                continue;
            }

            int requestedQty = safeQuantity(item.getQuantityInCart(), 1);
            int allocated = availability.allocate(requestedQty);

            if (allocated <= 0) {
                skippedCount += requestedQty;
                skippedNames.add(availability.resolveNameOrDefault(item.getProductName()));
                updatedWishlist.add(item);
                continue;
            }

            CartProduct cartLine = cartById.get(productId);
            if (cartLine != null) {
                int currentQty = safeQuantity(cartLine.getQuantityInCart());
                cartLine.setQuantityInCart(currentQty + allocated);
                cartLine.setProductQuantity(availability.getAvailableStock());
            } else {
                CartProduct newLine = CartProduct.builder()
                        .productId(item.getProductId())
                        .imageId(item.getImageId())
                        .productName(item.getProductName())
                        .productDescription(item.getProductDescription())
                        .productSalePrice(item.getProductSalePrice())
                        .averageRating(item.getAverageRating())
                        .quantityInCart(allocated)
                        .productQuantity(availability.getAvailableStock())
                        .build();
                cartLines.add(newLine);
                cartById.put(productId, newLine);
            }

            movedCount += allocated;

            int remainder = requestedQty - allocated;
            if (remainder > 0) {
                CartProduct remainderItem = CartProduct.builder()
                        .productId(item.getProductId())
                        .imageId(item.getImageId())
                        .productName(item.getProductName())
                        .productDescription(item.getProductDescription())
                        .productSalePrice(item.getProductSalePrice())
                        .averageRating(item.getAverageRating())
                        .quantityInCart(remainder)
                        .productQuantity(availability.getAvailableStock())
                        .build();
                updatedWishlist.add(remainderItem);
                skippedCount += remainder;
                skippedNames.add(availability.resolveNameOrDefault(item.getProductName()));
            }
        }

        cart.setProducts(cartLines);
        cart.setWishListProducts(updatedWishlist);

        int finalMoved = movedCount;
        int finalSkipped = skippedCount;
        List<String> skippedSnapshot = skippedNames.stream()
                .map(name -> name == null ? "" : name.trim())
                .filter(name -> !name.isEmpty())
                .collect(Collectors.toList());

        return cartRepository.save(cart)
                .map(saved -> {
                    CartResponseModel resp = EntityModelUtil.toCartResponseModel(saved, saved.getProducts());
                    StringBuilder message = new StringBuilder();
                    if (finalMoved > 0) {
                        message.append("Moved ").append(finalMoved).append(" item(s) from wishlist to cart.");
                    }
                    if (finalSkipped > 0) {
                        if (!message.isEmpty()) {
                            message.append(' ');
                        }
                        message.append("Skipped ").append(finalSkipped)
                                .append(" item(s) because they are out of stock or unavailable.");
                        if (!skippedSnapshot.isEmpty()) {
                            message.append(" (").append(String.join(", ", skippedSnapshot)).append(')');
                        }
                    }
                    if (message.isEmpty()) {
                        message.append("No wishlist items were moved.");
                    }
                    resp.setMessage(message.toString());
                    return resp;
                });
    }

    private static final class WishlistProductAvailability {
        private final String productId;
        private final String productName;
        private final int availableStock;
        private final boolean missing;
        private int existingInCart;
        private int allocated;

        private WishlistProductAvailability(String productId, String productName, int availableStock, boolean missing) {
            this.productId = productId;
            this.productName = productName;
            this.availableStock = Math.max(availableStock, 0);
            this.missing = missing;
        }

        static WishlistProductAvailability fromProduct(String productId, ProductResponseModel product, int availableStock) {
            String resolvedName = product != null ? product.getProductName() : null;
            return new WishlistProductAvailability(productId, resolvedName, availableStock, false);
        }

        static WishlistProductAvailability missing(String productId) {
            return new WishlistProductAvailability(productId, null, 0, true);
        }

        String getProductId() {
            return productId;
        }

        int getAvailableStock() {
            return availableStock;
        }

        void setExistingInCart(int existingInCart) {
            this.existingInCart = Math.max(existingInCart, 0);
        }

        int allocate(int requested) {
            if (missing) {
                return 0;
            }
            int remaining = availableStock - existingInCart - allocated;
            if (remaining <= 0) {
                return 0;
            }
            int demand = Math.max(requested, 0);
            int granted = Math.min(remaining, demand);
            allocated += granted;
            return granted;
        }

        String resolveNameOrDefault(String fallback) {
            if (productName != null && !productName.isBlank()) {
                return productName;
            }
            if (fallback != null && !fallback.isBlank()) {
                return fallback;
            }
            return "Product";
        }
    }

    private Mono<CartResponseModel> moveCartToWishlist(Cart cart, List<String> normalizedIds) {
        List<CartProduct> cartLines = cart.getProducts() != null ? new ArrayList<>(cart.getProducts()) : new ArrayList<>();
        if (cartLines.isEmpty()) {
            CartResponseModel resp = EntityModelUtil.toCartResponseModel(cart, cartLines);
            resp.setMessage("No items in cart to move.");
            return Mono.just(resp);
        }

        final boolean moveAll = normalizedIds.isEmpty();
        final List<CartProduct> remainingCart = new ArrayList<>();
        final List<CartProduct> selectedForWishlist = new ArrayList<>();

        final Set<String> targetIds = moveAll ? Collections.emptySet() : new LinkedHashSet<>(normalizedIds);

        for (CartProduct item : cartLines) {
            String pid = item.getProductId();
            boolean shouldMove = moveAll || (pid != null && targetIds.contains(pid));
            if (shouldMove) {
                selectedForWishlist.add(item);
            } else {
                remainingCart.add(item);
            }
        }

        if (selectedForWishlist.isEmpty()) {
            return Mono.error(new NotFoundException("No cart items matched the requested product IDs."));
        }

        List<CartProduct> wishlist = cart.getWishListProducts() != null ? new ArrayList<>(cart.getWishListProducts()) : new ArrayList<>();
        Map<String, CartProduct> wishlistById = new HashMap<>();
        for (CartProduct w : wishlist) {
            if (w.getProductId() != null) {
                wishlistById.put(w.getProductId(), w);
            }
        }

        int moved = 0;
        for (CartProduct selected : selectedForWishlist) {
            String pid = selected.getProductId();
            if (pid == null || pid.isBlank()) {
                continue;
            }

            CartProduct existing = wishlistById.get(pid);
            int qty = selected.getQuantityInCart() == null || selected.getQuantityInCart() <= 0 ? 1 : selected.getQuantityInCart();
            if (existing != null) {
                int base = existing.getQuantityInCart() == null ? 0 : existing.getQuantityInCart();
                existing.setQuantityInCart(base + qty);
                existing.setProductQuantity(selected.getProductQuantity());
            } else {
                CartProduct copy = CartProduct.builder()
                        .productId(selected.getProductId())
                        .imageId(selected.getImageId())
                        .productName(selected.getProductName())
                        .productDescription(selected.getProductDescription())
                        .productSalePrice(selected.getProductSalePrice())
                        .averageRating(selected.getAverageRating())
                        .quantityInCart(qty)
                        .productQuantity(selected.getProductQuantity())
                        .build();
                wishlist.add(copy);
                wishlistById.put(pid, copy);
            }
            moved += qty;
        }

        cart.setProducts(remainingCart);
        cart.setWishListProducts(wishlist);

        final int movedFinal = moved;
        return cartRepository.save(cart)
                .map(saved -> {
                    CartResponseModel resp = EntityModelUtil.toCartResponseModel(saved, saved.getProducts());
                    resp.setMessage("Moved " + movedFinal + " item(s) from cart to wishlist.");
                    return resp;
                });
    }

    @Override
    public Mono<List<CartProduct>> getRecentPurchasesByCustomerId(String customerId) {
        final String normalizedCustomerId = customerId == null ? null : customerId.trim();
        if (normalizedCustomerId == null || normalizedCustomerId.isEmpty()) {
            return Mono.error(new InvalidInputException("customerId must not be null or empty"));
        }

    return cartRepository.findCartByCustomerId(normalizedCustomerId)
        .map(cart -> cart.getRecentPurchases() != null ? cart.getRecentPurchases() : List.<CartProduct>of())
        .switchIfEmpty(Mono.just(List.of()));
    }

    @Override
    public Mono<List<CartProduct>> getRecommendationPurchasesByCustomerId(String customerId) {
        final String normalizedCustomerId = customerId == null ? null : customerId.trim();
        if (normalizedCustomerId == null || normalizedCustomerId.isEmpty()) {
            return Mono.error(new InvalidInputException("customerId must not be null or empty"));
        }

    return cartRepository.findCartByCustomerId(normalizedCustomerId)
        .map(cart -> cart.getRecommendationPurchase() != null ? cart.getRecommendationPurchase() : List.<CartProduct>of())
        .switchIfEmpty(Mono.just(List.of()));
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

}
