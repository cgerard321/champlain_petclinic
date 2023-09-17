package com.petclinic.vet.servicelayer;

import com.petclinic.vet.dataaccesslayer.RatingRepository;
import com.petclinic.vet.exceptions.ExistingVetNotFoundException;
import com.petclinic.vet.util.EntityDtoUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class RatingServiceImpl implements RatingService {
    @Autowired
    RatingRepository ratingRepository;

    @Override
    public Flux<RatingResponseDTO> getAllRatingsByVetId(String vetId) {
        return ratingRepository.findAllByVetId(vetId)
                .map(EntityDtoUtil::toDTO);
    }
    @Override
    public Mono<Void> deleteRatingByRatingId(String vetId, String ratingId) {
        return ratingRepository.findByVetIdAndRatingId(vetId, ratingId)
                //.switchIfEmpty(Mono.error(new Exception("Rating with id " + ratingId + " not found.")))
                .flatMap(ratingRepository::delete);
    }

}
