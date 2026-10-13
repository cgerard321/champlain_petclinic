package com.petclinic.products.presentationlayer.ratings;

import com.petclinic.products.datalayer.ratings.ReviewSettings;
import com.petclinic.products.datalayer.ratings.ReviewSettingsRepository;
import com.petclinic.products.utils.exceptions.InvalidInputException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/review-settings")
public class ReviewSettingsController {

    private ReviewSettingsRepository reviewSettingsRepository;

    @GetMapping
    public Mono<ReviewSettings> getSettings(){
        return reviewSettingsRepository.findById(1L);
    }

    @PutMapping
    public Mono<ReviewSettings> updateSettings(@RequestBody ReviewSettings request){
        if (request.getEditPeriodHours() == null || request.getEditPeriodHours() < 0){
            return Mono.error(new InvalidInputException("Edit period must be zero or greater"));
        }
        return reviewSettingsRepository.findById(1L)
                .switchIfEmpty(Mono.just(new ReviewSettings(1L, 48)))
                .flatMap(settings ->{
                    settings.setEditPeriodHours(request.getEditPeriodHours());
                    return reviewSettingsRepository.save(settings);
                });
    }
}
