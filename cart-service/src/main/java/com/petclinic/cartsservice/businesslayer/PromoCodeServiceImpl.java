package com.petclinic.cartsservice.businesslayer;

import com.petclinic.cartsservice.dataaccesslayer.PromoCode;
import com.petclinic.cartsservice.dataaccesslayer.PromoRepository;
import com.petclinic.cartsservice.domainclientlayer.PromoCodeRequestModel;
import com.petclinic.cartsservice.domainclientlayer.PromoCodeResponseModel;
import com.petclinic.cartsservice.utils.EntityModelUtil;
import com.petclinic.cartsservice.utils.exceptions.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
public class PromoCodeServiceImpl implements PromoCodeService {
    private final PromoRepository promoRepository;

    public PromoCodeServiceImpl(PromoRepository promoRepository) {
        this.promoRepository = promoRepository;
    }
    @Override
    public Flux<PromoCodeResponseModel> getAllPromoCodes() {
        return promoRepository.findAll()
                .map(EntityModelUtil::toPromoCodeResponseModel);
    }
    @Override
    public Flux<PromoCodeResponseModel> getActivePromos() {
        LocalDateTime currentDateTime = LocalDateTime.now();
        return promoRepository.findAllByExpirationDateGreaterThanEqual(currentDateTime)
                .map(EntityModelUtil::toPromoCodeResponseModel);
    }

    @Override
    public Mono<PromoCodeResponseModel> getPromoCodeByCode(String promoCode) {
        return promoRepository.findPromoCodeByCode(promoCode)
                .map(EntityModelUtil::toPromoCodeResponseModel);
    }

    @Override
    public Mono<PromoCodeResponseModel> getPromoCodeById(String promoCodeId) {
        return promoRepository.findById(promoCodeId)
                .map(EntityModelUtil::toPromoCodeResponseModel)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Promo code not found")));
    }

    @Override
    public Mono<PromoCodeResponseModel> updatePromoCodeById(PromoCodeRequestModel promoCodeRequestModel, String promoCodeId) {
        return promoRepository.findById(promoCodeId)
                .switchIfEmpty(Mono.defer(() -> Mono.error(new NotFoundException("Promo code ID was not found: " + promoCodeId))))
                .map(foundPromoCode -> EntityModelUtil.mapPromoCode(foundPromoCode, promoCodeRequestModel))
                .flatMap(promoRepository::save)
                .map(EntityModelUtil::toPromoCodeResponseModel);
    }

    @Override
    public Mono<PromoCodeResponseModel> deletePromoCode(String promoCodeId) {
        return promoRepository.findById(promoCodeId)
                .switchIfEmpty(Mono.defer(() -> Mono.error(new NotFoundException("Promo code ID was not found: " + promoCodeId))))
                .flatMap(found -> promoRepository.delete(found)
                        .then(Mono.just(found)))
                .map(EntityModelUtil::toPromoCodeResponseModel);
    }


    @Override
    public Mono<PromoCodeResponseModel> createPromo(PromoCodeRequestModel promoCodeRequestModel) {

        PromoCode promoCode = new PromoCode();
        promoCode.setCode(promoCodeRequestModel.getCode());
        promoCode.setId(UUID.randomUUID().toString());
        promoCode.setName(promoCodeRequestModel.getName());
        promoCode.setDiscount(promoCodeRequestModel.getDiscount());
        promoCode.setActive(true);
        promoCode.setExpirationDate(EntityModelUtil.validateExpirationDate(promoCodeRequestModel.getExpirationDate()));

        return promoRepository.save(promoCode)
                .map(EntityModelUtil::toPromoCodeResponseModel);
    }

}
