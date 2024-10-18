package com.petclinic.cartsservice.businesslayer;

import com.petclinic.cartsservice.domainclientlayer.PromoCodeRequestModel;
import com.petclinic.cartsservice.domainclientlayer.PromoCodeResponseModel;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PromoCodeService {
    Mono<PromoCodeResponseModel> createPromo (PromoCodeRequestModel promoCodeRequestModel);
    Flux<PromoCodeResponseModel> getAllPromoCodes() ;

    Mono<PromoCodeResponseModel> getPromoCodeById(String promoCodeId);

    Mono<PromoCodeResponseModel> updatePromoCodeById (PromoCodeRequestModel promoCodeRequestModel, String promoCodeId);

    Mono<PromoCodeResponseModel> deletePromoCode(String promoCodeId);

    Flux<PromoCodeResponseModel> getActivePromos();
}
