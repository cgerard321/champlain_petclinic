package com.petclinic.cartsservice.businesslayer.Payment;

import com.petclinic.cartsservice.dataaccesslayer.CartRepository;
import com.petclinic.cartsservice.dataaccesslayer.OrderRepository;
import com.petclinic.cartsservice.dataaccesslayer.cartproduct.CartProduct;
import com.petclinic.cartsservice.presentationlayer.OrderRequestModel;
import com.petclinic.cartsservice.presentationlayer.OrderResponseModel;
import com.petclinic.cartsservice.presentationlayer.Payment.StripeProductRequestModel;
import com.petclinic.cartsservice.presentationlayer.Payment.StripeResponseModel;
import com.petclinic.cartsservice.utils.exceptions.InvalidInputException;
import com.petclinic.cartsservice.utils.exceptions.NotFoundException;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class PaymentServiceImpl implements PaymentService {

    private final CartRepository cartRepository;

    public PaymentServiceImpl(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }


    @Override
    public Mono<StripeResponseModel> createStripeBill(StripeProductRequestModel sessionRequestModel, String cartId) {

        return Mono.just(sessionRequestModel)
                .filter(request -> cartId.equals(request.getCartId()))
                .switchIfEmpty(Mono.error(
                        new InvalidInputException("Cart ids does not match.")
                ))
                .flatMap(request -> cartRepository.findCartByCartId(cartId))
                .switchIfEmpty(Mono.error(new NotFoundException("Cart not found.")))
                .flatMap(cart -> {

                    List<CartProduct> cartProducts = cart.getProducts();

                    if(cartProducts == null || cartProducts.isEmpty()){
                        return Mono.error(new InvalidInputException("No products found in cart."));
                    }

                    List<SessionCreateParams.LineItem> lineItems = new ArrayList<>();

                    for (CartProduct cartProduct : cartProducts) {

                        long priceInCents = Math.round(cartProduct.getProductSalePrice() * 100); //stripe takes the prices in cents

                        if (priceInCents <= 0 || cartProduct.getQuantityInCart() <= 0) {
                            throw new InvalidInputException("Invalid price amount.");
                        }

                        SessionCreateParams.LineItem.PriceData.ProductData productData = SessionCreateParams.LineItem.PriceData.ProductData
                                .builder()
                                .setName(cartProduct.getProductName())
                                .build();

                        SessionCreateParams.LineItem.PriceData priceData = SessionCreateParams.LineItem.PriceData
                                .builder()
                                .setCurrency("CAD")
                                .setUnitAmount(priceInCents)
                                .setProductData(productData)
                                .build();

                        SessionCreateParams.LineItem lineItem = SessionCreateParams.LineItem
                                .builder()
                                .setQuantity((long) cartProduct.getQuantityInCart())
                                .setPriceData(priceData)
                                .build();

                        lineItems.add(lineItem);
                    }

                    SessionCreateParams params =
                            SessionCreateParams.builder()
                                    .setMode(SessionCreateParams.Mode.PAYMENT)
                                    .setSuccessUrl("http://localhost:3000/transaction")
                                    .setCancelUrl("http://localhost:3000/checkout")
                                    .addAllLineItem(lineItems)
                                    .putMetadata("cartId", cart.getCartId())
                                    .build();

                    return Mono.fromCallable(() -> Session.create(params))//.fromCallable executes the blocking method of stripe in a more reactive way
                            .subscribeOn(Schedulers.boundedElastic())
                            .map(session -> new StripeResponseModel(
                                    session.getId(),
                                    session.getUrl()
                            ));

                });
    }
}
