package com.petclinic.visits.visitsservicenew.DataLayer;


import com.petclinic.visits.visitsservicenew.DataLayer.Emergency.Emergency;
import com.petclinic.visits.visitsservicenew.DataLayer.Emergency.EmergencyRepository;
import com.petclinic.visits.visitsservicenew.DataLayer.Emergency.UrgencyLevel;
import com.petclinic.visits.visitsservicenew.DataLayer.Review.Review;
import com.petclinic.visits.visitsservicenew.DataLayer.Review.ReviewRepository;
import com.petclinic.visits.visitsservicenew.DataLayer.Status;
import com.petclinic.visits.visitsservicenew.DataLayer.Visit;
import com.petclinic.visits.visitsservicenew.DataLayer.VisitRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class DataSetupService implements CommandLineRunner {

    @Autowired
    VisitRepo visitRepo;

    @Autowired
    ReviewRepository reviewRepository;

    @Autowired
    EmergencyRepository emergencyRepository;

    @Override
    public void run(String... args) throws Exception {
        // If db contains anything, skip.
        if (Boolean.TRUE.equals(visitRepo.findAll().hasElements().block())) {
            return;
        }

        if (Boolean.TRUE.equals(reviewRepository.findAll().hasElements().block())) {
            return;
        }

        if (Boolean.TRUE.equals(emergencyRepository.findAll().hasElements().block())) {
            return;
        }

        setupVisits();
        setupReviews();
        setupEmergencies();
    }

    private void setupVisits() {
        Visit visit1 = buildVisit("visitId1", "2022-11-24 13:00", "this is a dummy description", "ecb109cd-57ea-4b85-b51e-99751fd1c349", "69f852ca-625b-11ee-8c99-0242ac120002", Status.UPCOMING, LocalDateTime.parse("2024-11-24 13:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")).plusHours(1));
        Visit visit2 = buildVisit("visitId2", "2022-03-01 13:00", "Dog Needs Meds", "0e4d8481-b611-4e52-baed-af16caa8bf8a", "69f85766-625b-11ee-8c99-0242ac120002", Status.COMPLETED, LocalDateTime.parse("2022-03-01 13:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")).plusHours(1));
        Visit visit3 = buildVisit("visitId3", "2020-07-19 13:00", "Dog Needs Surgery After Meds", "0e4d8481-b611-4e52-baed-af16caa8bf8a", "69f85bda-625b-11ee-8c99-0242ac120002", Status.COMPLETED, LocalDateTime.parse("2020-07-19 13:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")).plusHours(1));
        Visit visit4 = buildVisit("visitId4", "2022-12-24 13:00", "Dog Needs Physio-Therapy", "0e4d8481-b611-4e52-baed-af16caa8bf8a", "69f85d2e-625b-11ee-8c99-0242ac120002", Status.ARCHIVED, LocalDateTime.parse("2022-12-24 13:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")).plusHours(1));
        Visit visit5 = buildVisit("visitId5", "2023-12-24 13:00", "Cat Needs Check-Up", "53163352-8398-4513-bdff-b7715c056d1d", "ac9adeb8-625b-11ee-8c99-0242ac120002", Status.UPCOMING, LocalDateTime.parse("2023-12-24 13:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")).plusHours(1));
        Visit visit6 = buildVisit("visitId6", "2023-12-05 15:00", "Animal Needs Operation", "53163352-8398-4513-bdff-b7715c056d1d", "ac9adeb8-625b-11ee-8c99-0242ac120002", Status.UPCOMING, LocalDateTime.parse("2023-12-05 15:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")).plusHours(1));
        Visit visit7 = buildVisit("visitId7", "2022-05-20 09:00", "Cat Needs Check-Up", "7056652d-f2fd-4873-a480-5d2e86bed641", "ac9adeb8-625b-11ee-8c99-0242ac120002", Status.CONFIRMED, LocalDateTime.parse("2022-05-20 09:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")).plusHours(1));
        Visit visit8 = buildVisit("visitId8", "2023-01-01 10:00", "Cat Needs Check-Up", "ecb109cd-57ea-4b85-b51e-99751fd1c349", "69f852ca-625b-11ee-8c99-0242ac120002", Status.CONFIRMED, LocalDateTime.parse("2023-01-01 10:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")).plusHours(1));
        Visit visit9 = buildVisit("visitId9", "2023-02-14 14:00", "Valentine's Day Special Treatment", "0e4d8481-b611-4e52-baed-af16caa8bf8a", "69f85766-625b-11ee-8c99-0242ac120002", Status.CANCELLED, LocalDateTime.parse("2023-02-14 14:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")).plusHours(1));
        Visit visit10 = buildVisit("visitId10", "2023-03-17 16:00", "Dog Massage", "0e4d8481-b611-4e52-baed-af16caa8bf8a", "69f85bda-625b-11ee-8c99-0242ac120002", Status.UPCOMING, LocalDateTime.parse("2023-03-17 16:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")).plusHours(1));
        Visit visit11 = buildVisit("visitId11", "2023-04-01 08:00", "Nail Cutting", "0e4d8481-b611-4e52-baed-af16caa8bf8a", "69f85d2e-625b-11ee-8c99-0242ac120002", Status.CONFIRMED, LocalDateTime.parse("2023-04-01 08:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")).plusHours(1));
        Visit visit12 = buildVisit("visitId12", "2023-05-05 12:00", "Dog rabies injection", "53163352-8398-4513-bdff-b7715c056d1d", "ac9adeb8-625b-11ee-8c99-0242ac120002", Status.ARCHIVED, LocalDateTime.parse("2023-05-05 12:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")).plusHours(1));
        Visit visit13 = buildVisit("visitId13", "2023-06-21 09:00", "Cast removal", "53163352-8398-4513-bdff-b7715c056d1d", "ac9adeb8-625b-11ee-8c99-0242ac120002", Status.COMPLETED, LocalDateTime.parse("2023-06-21 09:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")).plusHours(1));
        Visit visit14 = buildVisit("visitId14", "2023-07-04 15:00", "Regular check-up", "7056652d-f2fd-4873-a480-5d2e86bed641", "ac9adeb8-625b-11ee-8c99-0242ac120002", Status.CANCELLED, LocalDateTime.parse("2023-07-04 15:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")).plusHours(1));
        Visit visit15 = buildVisit("visitId15", "2021-10-18 13:00", "this is a dummy description2", "ecb109cd-57ea-4b85-b51e-99751fd1c349", "69f852ca-625b-11ee-8c99-0242ac120002", Status.UPCOMING, LocalDateTime.parse("2024-11-24 13:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")).plusHours(1));
        Visit visit16 = buildVisit("visitId16", "2024-01-24 19:00", "this is a dummy description3", "ecb109cd-57ea-4b85-b51e-99751fd1c349", "69f852ca-625b-11ee-8c99-0242ac120002", Status.UPCOMING, LocalDateTime.parse("2024-11-24 13:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")).plusHours(1));

        Flux.just(visit1, visit2, visit3, visit4, visit5, visit6, visit7, visit8, visit9, visit10, visit11, visit12, visit13, visit14, visit15, visit16)
                .flatMap(visitRepo::insert)
                .subscribe();
    }

    private void setupReviews() {
        Review review1 = buildReview("reviewId1", 5, "zako", "very good", "2022-11-24 13:00");
        Review review2 = buildReview("reviewId2", 5, "Regine", "very good", "2022-11-24 13:00");
        Review review3 = buildReview("reviewId3", 5, "zako2", "very good", "2022-11-24 13:00");
        Review review4 = buildReview("reviewId4", 5, "zako3", "very good", "2022-11-24 13:00");
        Review review5 = buildReview("reviewId5", 5, "zako4", "very good", "2022-11-24 13:00");
        // Add more reviews...
        Flux.just(review1, review2, review3, review4, review5)
                .flatMap(reviewRepository::insert)
                .subscribe();
    }

    private void setupEmergencies() {
        Emergency emergency1 = buildEmergency("emergencyId1", "2022-12-01 10:00", "Severe bleeding", "ecb109cd-57ea-4b85-b51e-99751fd1c349", "69f852ca-625b-11ee-8c99-0242ac120002", UrgencyLevel.HIGH, "Accident");
        Emergency emergency2 = buildEmergency("emergencyId2", "2023-01-15 15:00", "Broken leg", "0e4d8481-b611-4e52-baed-af16caa8bf8a", "69f85766-625b-11ee-8c99-0242ac120002", UrgencyLevel.MEDIUM, "Injury");
        Emergency emergency3 = buildEmergency("emergencyId3", "2023-06-05 09:30", "Breathing issues", "0e4d8481-b611-4e52-baed-af16caa8bf8a", "69f85bda-625b-11ee-8c99-0242ac120002", UrgencyLevel.HIGH, "Respiratory");

        Flux.just(emergency1, emergency2, emergency3)
                .flatMap(emergencyRepository::insert)
                .subscribe();
    }

    private Visit buildVisit(String visitId, String visitDate, String description, String petId, String practitionerId, Status status, LocalDateTime visitEndDate) {
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

    private Emergency buildEmergency(String emergencyId, String visitDate, String description, String petId, String practitionerId, UrgencyLevel urgencyLevel, String emergencyType) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime parsedVisitDate = LocalDateTime.parse(visitDate, formatter);
        return Emergency.builder()
                .visitEmergencyId(emergencyId)
                .visitDate(parsedVisitDate)
                .description(description)
                .petId(petId)
                .practitionerId(practitionerId)
                .urgencyLevel(urgencyLevel)
                .emergencyType(emergencyType)
                .build();
    }
}
