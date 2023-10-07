package com.petclinic.vet.servicelayer.badges;

import com.petclinic.vet.dataaccesslayer.badges.BadgeRepository;
import com.petclinic.vet.dataaccesslayer.badges.BadgeTitle;
import com.petclinic.vet.dataaccesslayer.ratings.RatingRepository;
import com.petclinic.vet.exceptions.InvalidInputException;
import com.petclinic.vet.exceptions.NotFoundException;
import com.petclinic.vet.util.EntityDtoUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import reactor.core.publisher.Mono;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class BadgeServiceImpl implements BadgeService{
    private final BadgeRepository badgeRepository;
    private final RatingRepository ratingRepository;

    @Override
    public Mono<BadgeResponseDTO> getBadgeByVetId(String vetId) {
        return badgeRepository.findByVetId(vetId)
                .switchIfEmpty(Mono.error(new NotFoundException("vetId not found: " + vetId)))
                .flatMap(badge -> {
                    return ratingRepository.countAllByVetId(vetId)
                            .flatMap(count -> {
                                if (count == 0) {
                                    return Mono.just(0.0);
                                } else {
                                    return ratingRepository.findAllByVetId(vetId)
                                            .switchIfEmpty(Mono.error(new NotFoundException("vetId is Not Found" + vetId)))
                                            .map(EntityDtoUtil::toDTO)
                                            .reduce(0.0, (acc, rating) -> acc + rating.getRateScore())
                                            .map(sum -> sum / count);
                                }
                            })
                            .flatMap(avgRating -> {
                                if (avgRating <= 2.0) {
                                    return loadBadgeImage("images/empty_food_bowl.png")
                                            .map(imageData -> {
                                                badge.setData(imageData);
                                                badge.setBadgeTitle(BadgeTitle.VALUED);
                                                return badge;
                                            });
                                } else if (avgRating <= 4.0) {
                                    return loadBadgeImage("images/half-full_food_bowl.png")
                                            .map(imageData -> {
                                                badge.setData(imageData);
                                                badge.setBadgeTitle(BadgeTitle.MUCH_APPRECIATED);
                                                return badge;
                                            });
                                } else if (avgRating <= 5.0) {
                                    return loadBadgeImage("images/full_food_bowl.png")
                                            .map(imageData -> {
                                                badge.setData(imageData);
                                                badge.setBadgeTitle(BadgeTitle.HIGHLY_RESPECTED);
                                                return badge;
                                            });
                                } else {
                                    return Mono.just(badge);
                                }
                            });
                })
                .map(EntityDtoUtil::toBadgeResponseDTO);
    }

    private Mono<byte[]> loadBadgeImage(String imagePath) {
        try {
            ClassPathResource cpr = new ClassPathResource(imagePath);
            return Mono.just(StreamUtils.copyToByteArray(cpr.getInputStream()));
        } catch (IOException io) {
            return Mono.error(new InvalidInputException("Picture does not exist: " + io.getMessage()));
        }
    }
}
