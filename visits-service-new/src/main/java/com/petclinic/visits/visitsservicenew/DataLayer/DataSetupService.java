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
        Visit visit1 = buildVisit("4f204ef4-2375-49de-8825-906143239fb9", "2022-11-24 13:00", "this is a dummy description", "ecb109cd-57ea-4b85-b51e-99751fd1c349", "69f852ca-625b-11ee-8c99-0242ac120002", Status.UPCOMING, LocalDateTime.parse("2024-11-24 13:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")).plusHours(1), true);
        Visit visit2 = buildVisit("b38c5c35-483f-4017-bcda-8d5c53aed362", "2022-03-01 13:00", "Dog Needs Meds", "0e4d8481-b611-4e52-baed-af16caa8bf8a", "69f85766-625b-11ee-8c99-0242ac120002", Status.COMPLETED, LocalDateTime.parse("2022-03-01 13:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")).plusHours(1), true);
        Visit visit3 = buildVisit("c1d9ca65-a4f7-4cf7-a4c2-0964ecc050d4", "2020-07-19 13:00", "Dog Needs Surgery After Meds", "0e4d8481-b611-4e52-baed-af16caa8bf8a", "69f85bda-625b-11ee-8c99-0242ac120002", Status.COMPLETED, LocalDateTime.parse("2020-07-19 13:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")).plusHours(1), true);
        Visit visit4 = buildVisit("81331565-fe16-438a-8a8c-c926239fb955", "2022-12-24 13:00", "Dog Needs Physio-Therapy", "0e4d8481-b611-4e52-baed-af16caa8bf8a", "69f85d2e-625b-11ee-8c99-0242ac120002", Status.ARCHIVED, LocalDateTime.parse("2022-12-24 13:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")).plusHours(1), true);
        Visit visit5 = buildVisit("c401d8f0-492f-4fff-8a3e-124fcd5449b6", "2023-12-24 13:00", "Cat Needs Check-Up", "53163352-8398-4513-bdff-b7715c056d1d", "ac9adeb8-625b-11ee-8c99-0242ac120002", Status.UPCOMING, LocalDateTime.parse("2023-12-24 13:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")).plusHours(1), false);
        Visit visit6 = buildVisit("0dde2620-d678-4850-b334-57ac581ea4bd", "2023-12-05 15:00", "Animal Needs Operation", "53163352-8398-4513-bdff-b7715c056d1d", "ac9adeb8-625b-11ee-8c99-0242ac120002", Status.UPCOMING, LocalDateTime.parse("2023-12-05 15:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")).plusHours(1), false);
        Visit visit7 = buildVisit("6c2b5ed4-7599-4125-9d72-106fdb41e216", "2022-05-20 09:00", "Cat Needs Check-Up", "7056652d-f2fd-4873-a480-5d2e86bed641", "ac9adeb8-625b-11ee-8c99-0242ac120002", Status.CONFIRMED, LocalDateTime.parse("2022-05-20 09:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")).plusHours(1), false);
        Visit visit8 = buildVisit("c6c22ab7-ef2a-432d-9f62-907d7d73bbab", "2023-01-01 10:00", "Cat Needs Check-Up", "ecb109cd-57ea-4b85-b51e-99751fd1c349", "69f852ca-625b-11ee-8c99-0242ac120002", Status.CONFIRMED, LocalDateTime.parse("2023-01-01 10:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")).plusHours(1), false);
        Visit visit9 = buildVisit("6fae2633-fdf0-4ec1-8238-44dd8aa440da", "2023-02-14 14:00", "Valentine's Day Special Treatment", "0e4d8481-b611-4e52-baed-af16caa8bf8a", "69f85766-625b-11ee-8c99-0242ac120002", Status.CANCELLED, LocalDateTime.parse("2023-02-14 14:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")).plusHours(1), false);
        Visit visit10 = buildVisit("84127dc8-01ca-4234-bc62-63a3d2961d03", "2023-03-17 16:00", "Dog Massage", "0e4d8481-b611-4e52-baed-af16caa8bf8a", "69f85bda-625b-11ee-8c99-0242ac120002", Status.UPCOMING, LocalDateTime.parse("2023-03-17 16:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")).plusHours(1), false);
        Visit visit11 = buildVisit("b6beaf33-341f-41e7-b215-4d812d0902a6", "2023-04-01 08:00", "Nail Cutting", "0e4d8481-b611-4e52-baed-af16caa8bf8a", "69f85d2e-625b-11ee-8c99-0242ac120002", Status.CONFIRMED, LocalDateTime.parse("2023-04-01 08:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")).plusHours(1), false);
        Visit visit12 = buildVisit("d926bc9a-64ef-43ad-af5d-1b0cc78f58a9", "2023-05-05 12:00", "Dog rabies injection", "53163352-8398-4513-bdff-b7715c056d1d", "ac9adeb8-625b-11ee-8c99-0242ac120002", Status.ARCHIVED, LocalDateTime.parse("2023-05-05 12:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")).plusHours(1), false);
        Visit visit13 = buildVisit("d1c22870-fa67-4b99-b74c-082b133eea5a", "2023-06-21 09:00", "Cast removal", "53163352-8398-4513-bdff-b7715c056d1d", "ac9adeb8-625b-11ee-8c99-0242ac120002", Status.COMPLETED, LocalDateTime.parse("2023-06-21 09:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")).plusHours(1), false);
        Visit visit14 = buildVisit("14268c82-a7d5-4b3e-9aa2-a7f7c7c00f34", "2023-07-04 15:00", "Regular check-up", "7056652d-f2fd-4873-a480-5d2e86bed641", "ac9adeb8-625b-11ee-8c99-0242ac120002", Status.CANCELLED, LocalDateTime.parse("2023-07-04 15:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")).plusHours(1), false);
        Visit visit15 = buildVisit("cb88fa33-ffa4-4917-b7b3-1914f3207181", "2021-10-18 13:00", "this is a dummy description2", "ecb109cd-57ea-4b85-b51e-99751fd1c349", "69f852ca-625b-11ee-8c99-0242ac120002", Status.UPCOMING, LocalDateTime.parse("2024-11-24 13:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")).plusHours(1), false);
        Visit visit16 = buildVisit("3a2d3a80-34b2-4433-883e-5a2943ebce14", "2024-01-24 19:00", "this is a dummy description3", "ecb109cd-57ea-4b85-b51e-99751fd1c349", "69f852ca-625b-11ee-8c99-0242ac120002", Status.UPCOMING, LocalDateTime.parse("2024-11-24 13:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")).plusHours(1), false);

        Flux.just(visit1, visit2, visit3, visit4, visit5, visit6, visit7, visit8, visit9, visit10, visit11, visit12, visit13, visit14, visit15, visit16)
                .flatMap(visitRepo::insert)
                .subscribe();
    }

    private void setupReviews() {
        Review review1 = buildReview("b6df8874-ac60-4600-8bad-39be336e3323", 5, "zako", "very good", "2022-11-24 13:00");
        Review review2 = buildReview("743b84dd-7c3a-4cae-a229-063005f23814", 5, "Regine", "very good", "2022-11-24 13:00");
        Review review3 = buildReview("dafcb99d-b1fb-4034-bb4e-ac604c9ebb05", 5, "zako2", "very good", "2022-11-24 13:00");
        Review review4 = buildReview("ad5e9781-163b-42fa-a104-b0b94c6c50ae", 5, "zako3", "very good", "2022-11-24 13:00");
        Review review5 = buildReview("6745c03f-19c8-4561-93d2-81b122d6385b", 5, "zako4", "very good", "2022-11-24 13:00");
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
