package com.petclinic.vet.servicelayer.ratings;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petclinic.vet.dataaccesslayer.ratings.Rating;
import com.petclinic.vet.dataaccesslayer.ratings.RatingRepository;
import com.petclinic.vet.dataaccesslayer.VetRepository;
import com.petclinic.vet.exceptions.NotFoundException;

import com.petclinic.vet.exceptions.InvalidInputException;

import com.petclinic.vet.servicelayer.VetAverageRatingDTO;
import com.petclinic.vet.servicelayer.VetDTO;
import com.petclinic.vet.util.EntityDtoUtil;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuples;

import java.util.*;

@Service
public class RatingServiceImpl implements RatingService {
    private final VetRepository vetRepository;
    private final RatingRepository ratingRepository;
    private final ObjectMapper objectMapper;



    public RatingServiceImpl(RatingRepository ratingRepository, ObjectMapper objectMapper, VetRepository vetRepository) {
        this.ratingRepository = ratingRepository;
        this.objectMapper = objectMapper;
        this.vetRepository=vetRepository;
    }

    @Override
    public Flux<RatingResponseDTO> getAllRatingsByVetId(String vetId) {
        return vetRepository.findVetByVetId(vetId)
                .switchIfEmpty(Mono.error(new NotFoundException("vetId not found: " + vetId)))
                .flatMapMany(vet -> ratingRepository.findAllByVetId(vetId)
                        .map(EntityDtoUtil::toDTO)
                );
    }

    @Override
    public Mono<Void> deleteRatingByRatingId(String vetId, String ratingId) {
        return vetRepository.findVetByVetId(vetId)
                .switchIfEmpty(Mono.error(new NotFoundException("vetId not found: " + vetId)))
                .then(ratingRepository.findByVetIdAndRatingId(vetId, ratingId)
                        .switchIfEmpty(Mono.error(new NotFoundException("ratingId not found: "+ratingId)))
                        .flatMap(ratingRepository::delete)
                );
    }

    @Override
    public Mono<RatingResponseDTO> addRatingToVet(String vetId, Mono<RatingRequestDTO> ratingRequestDTO) {
        return vetRepository.findVetByVetId(vetId)
                .switchIfEmpty(Mono.error(new NotFoundException("vetId not found: " + vetId)))
                .flatMap(r -> ratingRequestDTO.flatMap(dto -> {
                    if (dto.getRateScore() < 1 || dto.getRateScore() > 5) {
                        return Mono.error(new InvalidInputException("rateScore should be between 1 and 5: " + dto.getRateScore()));
                    } else {
                        return Mono.just(dto);
                    }
                }))
                .map(requestDto -> {
                    if (requestDto.getPredefinedDescription() != null){
                        requestDto.setRateDescription(requestDto.getPredefinedDescription().name());
                    }
                    return requestDto;
                })
                .map(EntityDtoUtil::toEntity)
                .doOnNext(r -> r.setRatingId(UUID.randomUUID().toString()))
                .flatMap(ratingRepository::insert)
                .map(EntityDtoUtil::toDTO);
    }


    @Override
    public Mono<Integer> getNumberOfRatingsByVetId(String vetId) {
        return vetRepository.findVetByVetId(vetId)
                .switchIfEmpty(Mono.error(new NotFoundException("vetId not found: " + vetId)))
                .then(ratingRepository.countAllByVetId(vetId)
                        .map(Long::intValue)
                );
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
    public Flux<VetAverageRatingDTO> getTopThreeVetsWithHighestAverageRating() {


        return ratingRepository.findAll()
                .groupBy(Rating::getVetId)
                .flatMap(group -> {
                    String vetId = group.key();
                    return getAverageRatingByVetId(vetId)
                            .map(averageRating -> Tuples.of(vetId,averageRating));
                })
                .sort((t1, t2) -> Double.compare(t2.getT2(), t1.getT2()))
                .take(3)
                .publishOn(Schedulers.boundedElastic())
                .flatMap(tuple -> {
                    Mono<VetDTO> vetMono = vetRepository.findVetByVetId(tuple.getT1())
                            .map(EntityDtoUtil::toDTO);

                    return vetMono.map(vetDTO ->
                            new VetAverageRatingDTO(vetDTO, tuple.getT1(), tuple.getT2()));
                });
    }



    @Override
    public Mono<RatingResponseDTO> updateRatingByVetIdAndRatingId(String vetId, String ratingId, Mono<RatingRequestDTO> ratingRequestDTOMono) {
        return vetRepository.findVetByVetId(vetId)
                .switchIfEmpty(Mono.error(new NotFoundException("vetId not found: " + vetId)))
                .then(ratingRepository.findByVetIdAndRatingId(vetId, ratingId)
                        .switchIfEmpty(Mono.error(new NotFoundException("ratingId not found: " + ratingId)))
                        .flatMap(rating -> ratingRequestDTOMono
                                .flatMap(r -> {
                                    if (r.getRateScore() < 1 || r.getRateScore() > 5)
                                        return Mono.error(new InvalidInputException("rateScore should be between 1 and 5" + r.getRateScore()));
                                    if (r.getPredefinedDescription() != null){
                                        r.setRateDescription(r.getPredefinedDescription().name());
                                    }
                                    return Mono.just(r);
                                })
                                .map(EntityDtoUtil::toEntity)
                                .doOnNext(e -> e.setId(rating.getId()))
                                .doOnNext(e -> e.setRatingId(rating.getRatingId()))
                                .flatMap(ratingRepository::save)
                                .map(EntityDtoUtil::toDTO))
                );
    }



    @Override
    public Mono<String> getRatingPercentagesByVetId(String vetId) {
        return vetRepository.findVetByVetId(vetId)
                .switchIfEmpty(Mono.error(new NotFoundException("vetId not found: " + vetId)))
                .then(Mono.defer(()->{
                    Flux<Rating> ratingFlux = ratingRepository.findAllByVetId(vetId);
                    Map<Double, Integer> ratingCount = new HashMap<>();
                    for(double i = 1.0; i <= 5.0; i += 1.0) {
                        ratingCount.put(i, 0);
                    }
                    return ratingFlux
                            .map(EntityDtoUtil::toDTO)
                            .map(RatingResponseDTO::getRateScore)
                            .doOnNext(rating -> ratingCount.put(rating, ratingCount.get(rating) + 1))
                            .then(Mono.just(ratingCount))
                            .map(ratingCountMap -> {
                                Map<Double, Double> ratingPercentages = new LinkedHashMap<>();
                                for(double i = 1.0; i <= 5.0; i += 1.0) {
                                    ratingPercentages.put(i, ratingCountMap.get(i) / (double) ratingCountMap.values().stream().mapToInt(Integer::intValue).sum());
                                }
                                try {
                                    String ratingPercentageJson = objectMapper.writeValueAsString(ratingPercentages);
                                    return ratingPercentageJson;
                                } catch (JsonProcessingException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                }));

    }
}
