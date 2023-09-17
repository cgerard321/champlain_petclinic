package com.petclinic.vet.servicelayer;

import com.petclinic.vet.dataaccesslayer.RatingRepository;
import com.petclinic.vet.util.EntityDtoUtil;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class RatingServiceImpl implements RatingService {

    private final RatingRepository ratingRepository;

    public RatingServiceImpl(RatingRepository ratingRepository) {
        this.ratingRepository = ratingRepository;
    }

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

    @Override
    public Mono<RatingResponseDTO> addRatingToVet(String vetId, Mono<RatingRequestDTO> ratingRequestDTO) {
        return ratingRequestDTO
                .map(EntityDtoUtil::toEntity)
                .doOnNext(r -> r.setRatingId(UUID.randomUUID().toString()))
                .flatMap(ratingRepository::insert)
                .map(EntityDtoUtil::toDTO);
    }

    @Override
    public Mono<Integer> getNumberOfRatingsByVetId(String vetId) {
        return  ratingRepository.countAllByVetId(vetId)
                .map(Long::intValue);
    }

}
