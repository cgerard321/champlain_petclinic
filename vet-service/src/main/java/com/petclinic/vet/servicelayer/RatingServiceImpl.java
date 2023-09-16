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
    public Mono<Integer> getNumberOfRatingsByVetId(String vetId) {
        return  ratingRepository.countAllByVetId(vetId)
                .map(Long::intValue);
    }

}
