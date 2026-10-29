package com.petclinic.cartsservice.businesslayer;

import com.petclinic.cartsservice.dataaccesslayer.PromoCode;
import com.petclinic.cartsservice.dataaccesslayer.PromoRepository;
import com.petclinic.cartsservice.domainclientlayer.PromoCodeRequestModel;
import com.petclinic.cartsservice.domainclientlayer.PromoCodeResponseModel;
import com.petclinic.cartsservice.utils.EntityModelUtil;
import com.petclinic.cartsservice.utils.PromoEligibility;
import com.petclinic.cartsservice.utils.PromoImage;
import com.petclinic.cartsservice.domainclientlayer.FilesServiceClient;
import com.petclinic.cartsservice.utils.exceptions.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
public class PromoCodeServiceImpl implements PromoCodeService {
    private final PromoRepository promoRepository;

    private final FilesServiceClient filesServiceClient;

    public PromoCodeServiceImpl(PromoRepository promoRepository, FilesServiceClient filesServiceClient) {
        this.promoRepository = promoRepository;
        this.filesServiceClient = filesServiceClient;
    }
    @Override
    public Flux<PromoCodeResponseModel> getAllPromoCodes() {
        return promoRepository.findAll()
                .flatMapSequential(this::includeImage);
    }
    @Override
    public Flux<PromoCodeResponseModel> getActivePromos() {
        LocalDateTime currentDateTime = LocalDateTime.now();
        return promoRepository.findAllByExpirationDateGreaterThanEqual(currentDateTime)
                .filter(PromoCode::isActive)
                .flatMapSequential(this::includeImage);
    }

    @Override
    public Mono<PromoCodeResponseModel> getPromoCodeByCode(String promoCode) {
        return promoRepository.findPromoCodeByCode(promoCode)
                .flatMap(this::includeImage);
    }

    @Override
    public Mono<PromoCodeResponseModel> getPromoCodeById(String promoCodeId) {
        return promoRepository.findById(promoCodeId)
                .flatMap(this::includeImage)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Promo code not found")));
    }

    @Override
    public Mono<PromoCodeResponseModel> updatePromoCodeById(PromoCodeRequestModel promoCodeRequestModel, String promoCodeId) {
        return promoRepository.findById(promoCodeId)
                .switchIfEmpty(Mono.defer(() -> Mono.error(new NotFoundException("Promo code ID was not found: " + promoCodeId))))
                .map(foundPromoCode -> EntityModelUtil.mapPromoCode(foundPromoCode, promoCodeRequestModel))
                .flatMap(promo -> saveWithImage(promo, promoCodeRequestModel));
    }

    @Override
    public Mono<PromoCodeResponseModel> deletePromoCode(String promoCodeId) {
        return promoRepository.findById(promoCodeId)
                .switchIfEmpty(Mono.defer(() -> Mono.error(new NotFoundException("Promo code ID was not found: " + promoCodeId))))
                .flatMap(found -> promoRepository.delete(found)
                        .then(deleteImage(found.getImageId()))
                        .thenReturn(EntityModelUtil.toPromoCodeResponseModel(found)));
    }


    @Override
    public Mono<PromoCodeResponseModel> createPromo(PromoCodeRequestModel promoCodeRequestModel) {

        PromoCode promoCode = new PromoCode();
        promoCode.setCode(promoCodeRequestModel.getCode());
        promoCode.setId(UUID.randomUUID().toString());
        promoCode.setName(promoCodeRequestModel.getName());
        promoCode.setDiscount(promoCodeRequestModel.getDiscount());
        promoCode.setActive(!Boolean.FALSE.equals(promoCodeRequestModel.getActive()));
        promoCode.setExpirationDate(EntityModelUtil.validateExpirationDate(promoCodeRequestModel.getExpirationDate()));

        PromoEligibility.setScope(promoCode, promoCodeRequestModel);
        return saveWithImage(promoCode, promoCodeRequestModel);
    }

    private Mono<PromoCodeResponseModel> includeImage(PromoCode promo) {
        PromoCodeResponseModel response = EntityModelUtil.toPromoCodeResponseModel(promo);
        if (promo.getImageId() == null || promo.getImageId().isBlank()) return Mono.just(response);
        return filesServiceClient.getFile(promo.getImageId())
                .map(image -> { response.setImage(image); return response; })
                .onErrorResume(error -> {
                    log.warn("Unable to load image for promotion {}", promo.getId());
                    return Mono.empty();
                })
                .defaultIfEmpty(response);
    }

    private Mono<PromoCodeResponseModel> saveWithImage(PromoCode promo, PromoCodeRequestModel request) {
        return Mono.defer(() -> {
            if (request.isRemoveImage() && request.getImage() != null) {
                return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Choose either a replacement image or image removal."));
            }
            PromoImage.validate(request.getImage());
            String previousId = promo.getImageId();
            if (request.getImage() != null) {
                return filesServiceClient.addFile(request.getImage()).flatMap(image -> {
                    promo.setImageId(image.getFileId());
                    return promoRepository.save(promo)
                            .onErrorResume(error -> deleteImage(image.getFileId()).then(Mono.error(error)))
                            .flatMap(saved -> deleteImage(previousId).thenReturn(saved))
                            .map(saved -> {
                                PromoCodeResponseModel response = EntityModelUtil.toPromoCodeResponseModel(saved);
                                response.setImage(image);
                                return response;
                            });
                });
            }
            if (request.isRemoveImage()) promo.setImageId(null);
            return promoRepository.save(promo)
                    .flatMap(saved -> (Objects.equals(previousId, saved.getImageId())
                            ? Mono.<Void>empty() : deleteImage(previousId)).then(includeImage(saved)));
        });
    }

    private Mono<Void> deleteImage(String imageId) {
        if (imageId == null || imageId.isBlank()) return Mono.empty();
        return filesServiceClient.deleteFile(imageId).onErrorResume(error -> {
            log.warn("Unable to delete promotion image {}", imageId);
            return Mono.empty();
        });
    }

}
