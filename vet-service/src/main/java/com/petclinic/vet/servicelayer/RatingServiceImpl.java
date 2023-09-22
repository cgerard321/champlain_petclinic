package com.petclinic.vet.servicelayer;

import com.petclinic.vet.dataaccesslayer.RatingRepository;
import com.petclinic.vet.exceptions.NotFoundException;

import com.petclinic.vet.exceptions.InvalidInputException;

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

    @Override
    public Mono<Double> getAverageRatingByVetId(String vetId) {
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
                });
    }

    @Override
    public Mono<RatingResponseDTO> updateRatingByVetIdAndRatingId(String vetId, String ratingId, Mono<RatingRequestDTO> ratingRequestDTOMono) {
        return this.ratingRepository.findByRatingId(ratingId)
                .switchIfEmpty(Mono.error(new NotFoundException("Rating with id " + ratingId + " not found.")))
                .flatMap(rating -> ratingRequestDTOMono
                        .flatMap(r->{
                            if (r.getRateScore()<1||r.getRateScore()>5)
                                return Mono.error(new InvalidInputException("rateScore should be between 1 and 5" + r.getRateScore()));
                            return Mono.just(r);
                        })
                        .map(EntityDtoUtil::toEntity)
                        .doOnNext(e -> e.setId(rating.getId()))
                        .doOnNext(e -> e.setRatingId(rating.getRatingId()))
                        .flatMap(ratingRepository::save)
                        .map(EntityDtoUtil::toDTO));
    }
}
