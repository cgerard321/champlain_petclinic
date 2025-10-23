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

    @Override
    public void run(String... args) throws Exception {

        // If db contains anything, skip.
        try {
            if (Boolean.TRUE.equals(visitRepo.findAll().hasElements().block())) {
                return;
            }

            if (Boolean.TRUE.equals(reviewRepository.findAll().hasElements().block())) {
                return;
            }

        } catch (Exception e) {
            System.out.println("Error checking if visits/reviews exist: " + e.getMessage());
            return;
        }

        setupVisits();
        setupReviews();
    }

    private void setupVisits() {
        Visit visit1 = buildVisit("VIST-5248-4928", "2022-11-24 13:00", "this is a dummy description", "ecb109cd-57ea-4b85-b51e-99751fd1c349", "69f852ca-625b-11ee-8c99-0242ac120002", Status.UPCOMING, LocalDateTime.parse("2024-11-24 13:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")).plusHours(1), true);
        Visit visit2 = buildVisit("VIST-4928-5180", "2022-03-01 13:00", "Dog Needs Meds", "0e4d8481-b611-4e52-baed-af16caa8bf8a", "69f85766-625b-11ee-8c99-0242ac120002", Status.COMPLETED, LocalDateTime.parse("2022-03-01 13:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")).plusHours(1), true);
        Visit visit3 = buildVisit("VIST-2389-4820", "2020-07-19 13:00", "Dog Needs Surgery After Meds", "0e4d8481-b611-4e52-baed-af16caa8bf8a", "69f85bda-625b-11ee-8c99-0242ac120002", Status.COMPLETED, LocalDateTime.parse("2020-07-19 13:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")).plusHours(1), true);
        Visit visit4 = buildVisit("VIST-3482-2849", "2022-12-24 13:00", "Dog Needs Physio-Therapy", "0e4d8481-b611-4e52-baed-af16caa8bf8a", "69f85d2e-625b-11ee-8c99-0242ac120002", Status.ARCHIVED, LocalDateTime.parse("2022-12-24 13:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")).plusHours(1), true);
        Visit visit5 = buildVisit("VIST-4810-1048", "2023-12-24 13:00", "Cat Needs Check-Up", "53163352-8398-4513-bdff-b7715c056d1d", "ac9adeb8-625b-11ee-8c99-0242ac120002", Status.UPCOMING, LocalDateTime.parse("2023-12-24 13:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")).plusHours(1), false);
        Visit visit6 = buildVisit("VIST-4972-1804", "2023-12-05 15:00", "Animal Needs Operation", "53163352-8398-4513-bdff-b7715c056d1d", "ac9adeb8-625b-11ee-8c99-0242ac120002", Status.UPCOMING, LocalDateTime.parse("2023-12-05 15:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")).plusHours(1), false);
        Visit visit7 = buildVisit("VIST-6593-4824", "2022-05-20 09:00", "Cat Needs Check-Up", "7056652d-f2fd-4873-a480-5d2e86bed641", "ac9adeb8-625b-11ee-8c99-0242ac120002", Status.CONFIRMED, LocalDateTime.parse("2022-05-20 09:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")).plusHours(1), false);
        Visit visit8 = buildVisit("VIST-4835-4958", "2023-01-01 10:00", "Cat Needs Check-Up", "ecb109cd-57ea-4b85-b51e-99751fd1c349", "69f852ca-625b-11ee-8c99-0242ac120002", Status.CONFIRMED, LocalDateTime.parse("2023-01-01 10:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")).plusHours(1), false);
        Visit visit9 = buildVisit("VIST-6853-4039", "2023-02-14 14:00", "Valentine's Day Special Treatment", "0e4d8481-b611-4e52-baed-af16caa8bf8a", "69f85766-625b-11ee-8c99-0242ac120002", Status.CANCELLED, LocalDateTime.parse("2023-02-14 14:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")).plusHours(1), false);
        Visit visit10 = buildVisit("VIST-6549-4892", "2023-03-17 16:00", "Dog Massage", "0e4d8481-b611-4e52-baed-af16caa8bf8a", "69f85bda-625b-11ee-8c99-0242ac120002", Status.UPCOMING, LocalDateTime.parse("2023-03-17 16:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")).plusHours(1), false);
        Visit visit11 = buildVisit("VIST-6850-6072", "2023-04-01 08:00", "Nail Cutting", "0e4d8481-b611-4e52-baed-af16caa8bf8a", "69f85d2e-625b-11ee-8c99-0242ac120002", Status.CONFIRMED, LocalDateTime.parse("2023-04-01 08:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")).plusHours(1), false);
        Visit visit12 = buildVisit("VIST-9918-9457", "2023-05-05 12:00", "Dog rabies injection", "53163352-8398-4513-bdff-b7715c056d1d", "ac9adeb8-625b-11ee-8c99-0242ac120002", Status.ARCHIVED, LocalDateTime.parse("2023-05-05 12:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")).plusHours(1), false);
        Visit visit13 = buildVisit("VIST-6859-7695", "2023-06-21 09:00", "Cast removal", "53163352-8398-4513-bdff-b7715c056d1d", "ac9adeb8-625b-11ee-8c99-0242ac120002", Status.COMPLETED, LocalDateTime.parse("2023-06-21 09:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")).plusHours(1), false);
        Visit visit14 = buildVisit("VIST-1860-2008", "2023-07-04 15:00", "Regular check-up", "7056652d-f2fd-4873-a480-5d2e86bed641", "ac9adeb8-625b-11ee-8c99-0242ac120002", Status.CANCELLED, LocalDateTime.parse("2023-07-04 15:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")).plusHours(1), false);
        Visit visit15 = buildVisit("VIST-6058-0380", "2021-10-18 13:00", "this is a dummy description2", "ecb109cd-57ea-4b85-b51e-99751fd1c349", "69f852ca-625b-11ee-8c99-0242ac120002", Status.UPCOMING, LocalDateTime.parse("2024-11-24 13:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")).plusHours(1), false);
        Visit visit16 = buildVisit("VIST-9729-9753", "2024-01-24 19:00", "this is a dummy description3", "ecb109cd-57ea-4b85-b51e-99751fd1c349", "69f852ca-625b-11ee-8c99-0242ac120002", Status.UPCOMING, LocalDateTime.parse("2024-11-24 13:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")).plusHours(1), false);

        Flux.just(visit1, visit2, visit3, visit4, visit5, visit6, visit7, visit8, visit9, visit10, visit11, visit12, visit13, visit14, visit15, visit16)
                .flatMap(visitRepo::insert)
                .subscribe();
    }

    private void setupReviews() {
        Review review1 = buildReview("REVIEW-4800-1480", 5, "zako", "very good", "2022-11-24 13:00");
        Review review2 = buildReview("REVIEW-6418-8429", 5, "Regine", "very good", "2022-11-24 13:00");
        Review review3 = buildReview("REVIEW-9648-1840", 5, "zako2", "very good", "2022-11-24 13:00");
        Review review4 = buildReview("REVIEW-8496-6245", 5, "zako3", "very good", "2022-11-24 13:00");
        Review review5 = buildReview("REVIEW-4985-2479", 5, "zako4", "very good", "2022-11-24 13:00");
        // Add more reviews...
        Flux.just(review1, review2, review3, review4, review5)
                .flatMap(reviewRepository::insert)
                .subscribe();
    }


    private Visit buildVisit(String visitId, String visitDate, String description, String petId, String practitionerId, Status status, LocalDateTime visitEndDate, Boolean isEmergency) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime parsedVisitDate = LocalDateTime.parse(visitDate, formatter);
        return Visit.builder()
                .visitId(visitId)
                .visitDate(parsedVisitDate)
                .description(description)
                .petId(petId)
                .practitionerId(practitionerId)
                .status(status)
                .visitEndDate(visitEndDate)
                .isEmergency(isEmergency)
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
