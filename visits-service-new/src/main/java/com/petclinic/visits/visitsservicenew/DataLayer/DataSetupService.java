package com.petclinic.visits.visitsservicenew.DataLayer;



import com.petclinic.visits.visitsservicenew.DataLayer.Review.Review;
import com.petclinic.visits.visitsservicenew.DataLayer.Review.ReviewRepository;
import com.petclinic.visits.visitsservicenew.DataLayer.Status;
import com.petclinic.visits.visitsservicenew.DataLayer.Visit;
import com.petclinic.visits.visitsservicenew.DataLayer.VisitRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class DataSetupService implements CommandLineRunner {
    private final VisitRepo visitRepo;
    private final ReviewRepository reviewRepository;

    /**
     * This is an event function ran at the start to populate visitRepo. Sort of like a main
     * @throws Exception
     */
    @Override
    public void run(String... args) throws Exception {
       setupVisits();
       setupReviews();

    }

    private void setupReviews() {
        Review review1 = buildReview("reviewId1", 5, "zako", "very good", "2022-11-24 13:00");
        // Add more reviews...
        Flux.just(review1)
                .flatMap(reviewRepository::insert)
                .subscribe();
    }

    private void setupVisits(){
        Visit visit1 = buildVisit("visitId1", "2022-11-24 13:00", "this is a dummy description", "ecb109cd-57ea-4b85-b51e-99751fd1c349", "69f852ca-625b-11ee-8c99-0242ac120002", Status.COMPLETED, LocalDateTime.parse("2022-11-24 13:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")).plusHours(1));
        Visit visit2 = buildVisit("visitId2", "2022-03-01 13:00", "Dog Needs Meds", "0e4d8481-b611-4e52-baed-af16caa8bf8a", "69f85766-625b-11ee-8c99-0242ac120002", Status.COMPLETED, LocalDateTime.parse("2022-03-01 13:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")).plusHours(1));
        Visit visit3 = buildVisit("visitId3", "2020-07-19 13:00","Dog Needs Surgery After Meds", "0e4d8481-b611-4e52-baed-af16caa8bf8a", "69f85bda-625b-11ee-8c99-0242ac120002", Status.COMPLETED, LocalDateTime.parse("2020-07-19 13:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")).plusHours(1));
        Visit visit4 = buildVisit("visitId4", "2022-12-24 13:00", "Dog Needs Physio-Therapy", "0e4d8481-b611-4e52-baed-af16caa8bf8a", "69f85d2e-625b-11ee-8c99-0242ac120002", Status.UPCOMING, LocalDateTime.parse("2022-12-24 13:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")).plusHours(1));
        Visit visit5 = buildVisit("visitId5", "2023-12-24 13:00", "Cat Needs Check-Up", "53163352-8398-4513-bdff-b7715c056d1d", "ac9adeb8-625b-11ee-8c99-0242ac120002", Status.UPCOMING, LocalDateTime.parse("2023-12-24 13:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")).plusHours(1));
        Visit visit6 = buildVisit("visitId6", "2023-12-05 15:00", "Animal Needs Operation", "53163352-8398-4513-bdff-b7715c056d1d", "ac9adeb8-625b-11ee-8c99-0242ac120002", Status.UPCOMING, LocalDateTime.parse("2023-12-05 15:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")).plusHours(1));
        Visit visit7 = buildVisit("visitId7", "2022-05-20 09:00", "Cat Needs Check-Up", "7056652d-f2fd-4873-a480-5d2e86bed641", "ac9adeb8-625b-11ee-8c99-0242ac120002", Status.CONFIRMED, LocalDateTime.parse("2022-05-20 09:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")).plusHours(1));

        Flux.just(visit1, visit2, visit3, visit4, visit5, visit6, visit7).flatMap(x -> visitRepo.insert(Mono.just(x)).log(x.toString())).subscribe();

    }

    private Visit buildVisit(String visitId, String visitStartDate, String description, String petId, String practitionerId, Status status, LocalDateTime visitEndDate){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime parsedVisitDate = LocalDateTime.parse(visitStartDate, formatter);

        return Visit.builder()
                .visitId(visitId)
                .visitStartDate(parsedVisitDate)
                .description(description)
                .petId(petId)
                .practitionerId(practitionerId)
                .status(status)
                .visitEndDate(visitEndDate)
                .build();
    }

    private Review buildReview(String reviewId, int rating, String reviewerName, String review, String dateSub) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime parsedReviewDate = LocalDateTime.parse(dateSub, formatter);
        return Review.builder()
                .reviewId(reviewId)
                .rating(rating)
                .reviewerName(reviewerName)
                .review(review)
                .dateSubmitted(parsedReviewDate)
                .build();
    }
}
//VLAD