package com.petclinic.vet.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petclinic.vet.dataaccesslayer.albums.Album;
import com.petclinic.vet.dataaccesslayer.albums.AlbumRepository;
import com.petclinic.vet.dataaccesslayer.badges.Badge;
import com.petclinic.vet.dataaccesslayer.badges.BadgeRepository;
import com.petclinic.vet.dataaccesslayer.badges.BadgeTitle;
import com.petclinic.vet.dataaccesslayer.education.Education;
import com.petclinic.vet.dataaccesslayer.education.EducationRepository;
import com.petclinic.vet.dataaccesslayer.photos.Photo;
import com.petclinic.vet.dataaccesslayer.photos.PhotoRepository;
import com.petclinic.vet.dataaccesslayer.ratings.PredefinedDescription;
import com.petclinic.vet.dataaccesslayer.ratings.Rating;
import com.petclinic.vet.dataaccesslayer.ratings.RatingRepository;
import com.petclinic.vet.dataaccesslayer.vets.Specialty;
import com.petclinic.vet.dataaccesslayer.vets.Vet;
import com.petclinic.vet.dataaccesslayer.vets.VetRepository;
import com.petclinic.vet.dataaccesslayer.vets.WorkHour;
import com.petclinic.vet.dataaccesslayer.vets.Workday;
import com.petclinic.vet.utils.exceptions.InvalidInputException;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import reactor.core.publisher.Flux;

import java.util.*;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@Slf4j
@Service
public class DataSetupService implements CommandLineRunner {


    String date3 = ("2023");
    String date4 = ("2024");
    String date2 = ("2022");
    String date1 = ("2021");
    private final VetRepository vetRepository;
    private final RatingRepository ratingRepository;
    private final EducationRepository educationRepository;
    private final BadgeRepository badgeRepository;
    private final PhotoRepository photoRepository;
    private final AlbumRepository albumRepository;

    public DataSetupService(VetRepository vetRepository, RatingRepository ratingRepository, EducationRepository educationRepository, BadgeRepository badgeRepository, PhotoRepository photoRepository, AlbumRepository albumRepository) {
        this.vetRepository = vetRepository;
        this.ratingRepository = ratingRepository;
        this.educationRepository = educationRepository;
        this.badgeRepository = badgeRepository;
        this.photoRepository = photoRepository;
        this.albumRepository = albumRepository;
    }

    @Override
    public void run(String... args) throws Exception {

        // If data exists, do not insert
        try {
            if (Boolean.TRUE.equals(vetRepository.findAll().hasElements().block())) {
                return;
            }

            if (Boolean.TRUE.equals(ratingRepository.findAll().hasElements().block())) {
                return;
            }

            if (Boolean.TRUE.equals(educationRepository.findAll().hasElements().block())) {
                return;
            }

            if (Boolean.TRUE.equals(badgeRepository.findAll().hasElements().block())) {
                return;
            }

            if (Boolean.TRUE.equals(photoRepository.findAll().hasElements().block())) {
                return;
            }

            if (Boolean.TRUE.equals(albumRepository.findAll().hasElements().block())) {
                return;
            }
        } catch (Exception e) {
            System.out.println("Error checking if vets exist: " + e.getMessage());
            return;
        }

        Specialty s1 = new Specialty(EntityDtoUtil.generateSpecialtyId(), "radiology");
        Specialty s2 = new Specialty(EntityDtoUtil.generateSpecialtyId(), "surgery");
        Specialty s3 = new Specialty(EntityDtoUtil.generateSpecialtyId(), "dentistry");
        Specialty s4 = new Specialty(EntityDtoUtil.generateSpecialtyId(), "general");

        Set<Workday> workdays1 = EnumSet.of(Workday.Monday, Workday.Tuesday, Workday.Friday);
        Set<Workday> workdays2 = EnumSet.of(Workday.Wednesday, Workday.Thursday);
        Set<Workday> workdays3 = EnumSet.of(Workday.Monday, Workday.Wednesday, Workday.Thursday);
        Set<Workday> workdays4 = EnumSet.of(Workday.Wednesday, Workday.Thursday, Workday.Friday);
        Set<Workday> workdays5 = EnumSet.of(Workday.Monday, Workday.Tuesday, Workday.Wednesday, Workday.Thursday);
        Set<Workday> workdays6 = EnumSet.of(Workday.Monday, Workday.Tuesday, Workday.Friday);

        List<WorkHour> workHourList1 = new ArrayList<>();
        workHourList1.addAll(Arrays.asList(WorkHour.Hour_8_9, WorkHour.Hour_9_10, WorkHour.Hour_10_11, WorkHour.Hour_11_12, WorkHour.Hour_12_13, WorkHour.Hour_13_14, WorkHour.Hour_14_15, WorkHour.Hour_15_16));
        List<WorkHour> workHourList2 = new ArrayList<>();
        workHourList2.addAll(Arrays.asList(WorkHour.Hour_12_13, WorkHour.Hour_13_14, WorkHour.Hour_14_15, WorkHour.Hour_15_16, WorkHour.Hour_16_17, WorkHour.Hour_17_18, WorkHour.Hour_18_19, WorkHour.Hour_19_20));
        List<WorkHour> workHourList3 = new ArrayList<>();
        workHourList3.addAll(Arrays.asList(WorkHour.Hour_10_11, WorkHour.Hour_11_12, WorkHour.Hour_12_13, WorkHour.Hour_13_14, WorkHour.Hour_14_15, WorkHour.Hour_15_16, WorkHour.Hour_16_17, WorkHour.Hour_17_18));
        List<WorkHour> workHourList4 = new ArrayList<>();
        workHourList4.addAll(Arrays.asList(WorkHour.Hour_8_9, WorkHour.Hour_9_10, WorkHour.Hour_10_11, WorkHour.Hour_11_12));
        List<WorkHour> workHourList5 = new ArrayList<>();
        workHourList5.addAll(Arrays.asList(WorkHour.Hour_14_15, WorkHour.Hour_15_16, WorkHour.Hour_16_17, WorkHour.Hour_17_18));
        List<WorkHour> workHourList6 = new ArrayList<>();
        workHourList6.addAll(Arrays.asList(WorkHour.Hour_16_17, WorkHour.Hour_17_18, WorkHour.Hour_18_19, WorkHour.Hour_19_20));

        //list of the work hours
        List<WorkHour>[] workHourLists = new List[]{workHourList1, workHourList2, workHourList3, workHourList4, workHourList5, workHourList6};

        //work hours each day for vet 1
        Map<Workday, List<WorkHour>> workHours1 = new HashMap<>();
        List<Workday> workdayList1 = new ArrayList<>(workdays1);
        for (int i = 0; i < workdayList1.size(); i++) {
            Workday workday = workdayList1.get(i);
            List<WorkHour> workHourList = workHourLists[i];
            workHours1.put(workday, workHourList);
        }

        //work hours each day for vet 2
        Map<Workday, List<WorkHour>> workHours2 = new HashMap<>();
        List<Workday> workdayList2 = new ArrayList<>(workdays2);
        for (int i = 0; i < workdayList2.size(); i++) {
            Workday workday = workdayList2.get(i);
            List<WorkHour> workHourList = workHourLists[i];
            workHours2.put(workday, workHourList);
        }

        //work hours each day for vet 3
        Map<Workday, List<WorkHour>> workHours3 = new HashMap<>();
        List<Workday> workdayList3 = new ArrayList<>(workdays3);
        for (int i = 0; i < workdayList3.size(); i++) {
            Workday workday = workdayList3.get(i);
            List<WorkHour> workHourList = workHourLists[i];
            workHours3.put(workday, workHourList);
        }

        //work hours each day for vet 4
        Map<Workday, List<WorkHour>> workHours4 = new HashMap<>();
        List<Workday> workdayList4 = new ArrayList<>(workdays4);
        for (int i = 0; i < workdayList4.size(); i++) {
            Workday workday = workdayList4.get(i);
            List<WorkHour> workHourList = workHourLists[i];
            workHours4.put(workday, workHourList);
        }

        //work hours each day for vet 5
        Map<Workday, List<WorkHour>> workHours5 = new HashMap<>();
        List<Workday> workdayList5 = new ArrayList<>(workdays5);
        for (int i = 0; i < workdayList5.size(); i++) {
            Workday workday = workdayList5.get(i);
            List<WorkHour> workHourList = workHourLists[i];
            workHours5.put(workday, workHourList);
        }

        //work hours each day for vet 6
        Map<Workday, List<WorkHour>> workHours6 = new HashMap<>();
        List<Workday> workdayList6 = new ArrayList<>(workdays6);
        for (int i = 0; i < workdayList6.size(); i++) {
            Workday workday = workdayList6.get(i);
            List<WorkHour> workHourList = workHourLists[i];
            workHours6.put(workday, workHourList);
        }

        Set<Specialty> set1 = new HashSet<>();
        set1.add(s1);
        set1.add(s3);
        Set<Specialty> set2 = new HashSet<>();
        set2.add(s4);
        set2.add(s2);
        set2.add(s1);
        Set<Specialty> set3 = new HashSet<>();
        set3.add(s1);
        set3.add(s4);
        Vet v1 = Vet.builder()
                .vetId("69f852ca-625b-11ee-8c99-0242ac120002")
                .vetBillId("1")
                .firstName("James")
                .lastName("Carter")
                .email("carterjames@email.com")
                .phoneNumber("(514)-634-8276 #2384")
                .resume("Practicing since 3 years")
                .workday(workdays1)
                .workHoursJson(setWorkHours(workHours1))
                .active(true)
                .specialties(set1)
                .build();
        Vet v2 = Vet.builder()
                .vetId("69f85766-625b-11ee-8c99-0242ac120002")
                .vetBillId("2")
                .firstName("Helen")
                .lastName("Leary")
                .email("learyhelen@email.com")
                .phoneNumber("(514)-634-8276 #2385")
                .resume("Practicing since 10 years")
                .workday(workdays2)
                .workHoursJson(setWorkHours(workHours2))
                .active(true)
                .specialties(set3)
                .build();
        Vet v3 = Vet.builder()
                .vetId("69f85bda-625b-11ee-8c99-0242ac120002")
                .vetBillId("3")
                .firstName("Linda")
                .lastName("Douglas")
                .email("douglaslinda@email.com")
                .phoneNumber("(514)-634-8276 #2386")
                .resume("Practicing since 5 years")
                .workday(workdays3)
                .workHoursJson(setWorkHours(workHours3))
                .active(true)
                .specialties(set2)
                .build();
        Vet v4 = Vet.builder()
                .vetId("69f85d2e-625b-11ee-8c99-0242ac120002")
                .vetBillId("4")
                .firstName("Rafael")
                .lastName("Ortega")
                .email("ortegarafael@email.com")
                .phoneNumber("(514)-634-8276 #2387")
                .resume("Practicing since 8 years")
                .workday(workdays4)
                .workHoursJson(setWorkHours(workHours4))
                .active(false)
                .specialties(set2)
                .build();
        Vet v5 = Vet.builder()
                .vetId("ac9adeb8-625b-11ee-8c99-0242ac120002")
                .vetBillId("5")
                .firstName("Henry")
                .lastName("Stevens")
                .email("stevenshenry@email.com")
                .phoneNumber("(514)-634-8276 #2389")
                .resume("Practicing since 1 years")
                .workday(workdays5)
                .workHoursJson(setWorkHours(workHours5))
                .active(false)
                .specialties(set1)
                .build();
        Vet v6 = Vet.builder()
                .vetId("b2bc331e-625b-11ee-8c99-0242ac120002")
                .vetBillId("6")
                .firstName("Sharon")
                .lastName("Jenkins")
                .email("jenkinssharon@email.com")
                .phoneNumber("(514)-634-8276 #2383")
                .resume("Practicing since 6 years")
                .workday(workdays5)
                .workHoursJson(setWorkHours(workHours5))
                .active(false)
                .specialties(set1)
                .build();
        Vet v7 = Vet.builder()
                .vetId("c02cbf82-625b-11ee-8c99-0242ac120002")
                .vetBillId("7")
                .firstName("John")
                .lastName("Doe")
                .email("johndoe@email.com")
                .phoneNumber("(514)-634-8276 #2363")
                .resume("Practicing since 9 years")
                .workday(workdays6)
                .workHoursJson(setWorkHours(workHours6))
                .active(true)
                .specialties(set1)
                .build();
        Flux.just(v1, v2, v3, v4, v5, v6, v7)
                .flatMap(vetRepository::insert)
                .log()
                .subscribe();
        Rating r1 = Rating.builder()
                .ratingId(UUID.randomUUID().toString())
                .vetId(v1.getVetId())
                .customerName("Maria Escobito")
                .rateScore(5.0)
                .rateDescription("The vet has done an excellent job for my pet.")
                .predefinedDescription(PredefinedDescription.EXCELLENT)
                .rateDate("2024-05-15")
                .build();
        Rating r2 = Rating.builder()
                .ratingId(UUID.randomUUID().toString())
                .vetId(v1.getVetId())
                .customerName("Eduardo Rodriguez")
                .rateScore(3.0)
                .rateDescription("The vet did a good job, but I was expecting more.")
                .predefinedDescription(PredefinedDescription.GOOD)
                .rateDate("2024-06-20")
                .build();
        Rating r3 = Rating.builder()
                .ratingId(UUID.randomUUID().toString())
                .vetId(v2.getVetId())
                .customerName("Peter McTavish")
                .rateScore(3.0)
                .rateDescription("The vet did a good job.")
                .predefinedDescription(PredefinedDescription.GOOD)
                .rateDate("2024-07-05")
                .build();
        Rating r4 = Rating.builder()
                .ratingId(UUID.randomUUID().toString())
                .vetId(v2.getVetId())
                .customerName("George Franklin")
                .rateScore(2.0)
                .rateDescription("I didn't like how the vet did his job.")
                .predefinedDescription(PredefinedDescription.AVERAGE)
                .rateDate("2024-08-10")
                .build();
        Rating r5 = Rating.builder()
                .ratingId(UUID.randomUUID().toString())
                .vetId(v3.getVetId())
                .customerName("Harold Davis")
                .rateScore(5.0)
                .rateDescription("The vet was very careful and took a good care of my pet.")
                .predefinedDescription(PredefinedDescription.EXCELLENT)
                .rateDate("2024-09-15")
                .build();
        Rating r6 = Rating.builder()
                .ratingId(UUID.randomUUID().toString())
                .vetId(v3.getVetId())
                .customerName("Betty Davis")
                .rateScore(4.5)
                .rateDescription("Excellent job from the vet.")
                .predefinedDescription(PredefinedDescription.EXCELLENT)
                .rateDate("2024-10-05")
                .build();
        Rating r7 = Rating.builder()
                .ratingId(UUID.randomUUID().toString())
                .vetId(v4.getVetId())
                .customerName("Carlos Esteban")
                .rateScore(5.0)
                .rateDescription("The vet showed a lot of experience and took good care of my pet.")
                .predefinedDescription(PredefinedDescription.EXCELLENT)
                .rateDate("2024-11-25")
                .build();
        Rating r8 = Rating.builder()
                .ratingId(UUID.randomUUID().toString())
                .vetId(v4.getVetId())
                .customerName("")
                .rateScore(4.5)
                .rateDescription("Excellent job from the vet.")
                .predefinedDescription(PredefinedDescription.EXCELLENT)
                .rateDate("2024-12-01")
                .build();
        Rating r9 = Rating.builder()
                .ratingId(UUID.randomUUID().toString())
                .vetId(v5.getVetId())
                .customerName("David Schroeder")
                .rateScore(3.0)
                .rateDescription("Good job.")
                .predefinedDescription(PredefinedDescription.GOOD)
                .rateDate("2024-01-10")
                .build();
        Rating r10 = Rating.builder()
                .ratingId(UUID.randomUUID().toString())
                .vetId(v5.getVetId())
                .customerName("Jean Coleman")
                .rateScore(4.0)
                .rateDescription("Treated my vet with good care.")
                .predefinedDescription(PredefinedDescription.GOOD)
                .rateDate("2024-02-18")
                .build();
        Rating r11 = Rating.builder()
                .ratingId(UUID.randomUUID().toString())
                .vetId(v6.getVetId())
                .customerName("")
                .rateScore(4.0)
                .rateDescription("Treated my vet with good care.")
                .predefinedDescription(PredefinedDescription.GOOD)
                .rateDate("2024-03-12")
                .build();
        Rating r12 = Rating.builder()
                .ratingId(UUID.randomUUID().toString())
                .vetId(v6.getVetId())
                .customerName("Maria Escobito")
                .rateScore(5.0)
                .rateDescription("I had a wonderful experience with this vet.")
                .predefinedDescription(PredefinedDescription.EXCELLENT)
                .rateDate("2024-04-09")
                .build();
        Rating r13 = Rating.builder()
                .ratingId(UUID.randomUUID().toString())
                .vetId(v7.getVetId())
                .customerName("Betty Davis")
                .rateScore(3.0)
                .rateDescription("Took good care of my pet.")
                .predefinedDescription(PredefinedDescription.GOOD)
                .rateDate("2024-05-20")
                .build();
        Rating r14 = Rating.builder()
                .ratingId(UUID.randomUUID().toString())
                .vetId(v7.getVetId())
                .customerName("Jeff Black")
                .rateScore(5.0)
                .rateDescription("Wonderful service.")
                .predefinedDescription(PredefinedDescription.EXCELLENT)
                .rateDate("2024-06-30")
                .build();
        Flux.just(r1, r2, r3, r4, r5, r6, r7, r8, r9, r10, r11, r12, r13, r14)
                .flatMap(ratingRepository::insert)
                .log()
                .subscribe();
        Education e1 = Education.builder()
                .educationId(UUID.randomUUID().toString())
                .vetId(v1.getVetId())
                .degree("Doctor of Veterinary Medicine")
                .fieldOfStudy("Veterinary Medicine")
                .schoolName("University of Montreal")
                .startDate("2010")
                .endDate("2014")
                .build();
        Education e2 = Education.builder()
                .educationId(UUID.randomUUID().toString())
                .vetId(v2.getVetId())
                .degree("Doctor of Veterinary Medicine")
                .fieldOfStudy("Veterinary Medicine")
                .schoolName("University of Veterinary Sciences")
                .startDate("2008")
                .endDate("2013")
                .build();
        Education e3 = Education.builder()
                .educationId(UUID.randomUUID().toString())
                .vetId(v3.getVetId())
                .degree("Doctor of Veterinary Medicine")
                .fieldOfStudy("Animal Surgery")
                .schoolName("California Veterinary College")
                .startDate("2009")
                .endDate("2015")
                .build();
        Education e4 = Education.builder()
                .educationId(UUID.randomUUID().toString())
                .vetId(v4.getVetId())
                .degree("Master of Science in Veterinary Pathology")
                .fieldOfStudy("Pathology")
                .schoolName("Texas A&M University")
                .startDate("2016")
                .endDate("2018")
                .build();
        Education e5 = Education.builder()
                .educationId(UUID.randomUUID().toString())
                .vetId(v1.getVetId())
                .degree("Bachelor of Veterinary Science")
                .fieldOfStudy("Veterinary Genetics")
                .schoolName("University of Sydney")
                .startDate("2016")
                .endDate("2018")
                .build();
        Flux.just(e1, e2, e3, e4, e5)
                .flatMap(educationRepository::insert)
                .log()
                .subscribe();

        ClassPathResource cpr1 = new ClassPathResource("images/empty_food_bowl.png");
        ClassPathResource cpr2 = new ClassPathResource("images/half-full_food_bowl.png");
        ClassPathResource cpr3 = new ClassPathResource("images/full_food_bowl.png");

        //default photo
        String defaultPhotoName = "vet_default.jpg";
        String defaultPhotoType = "image/jpeg";

        //vet photo
        String jamesCarterPhotoName = "james_carter.jpg";
        String helenLearyPhotoName = "helen_leary.jpg";
        String henryStevensPhotoName = "henry_stevens.jpg";
        String johnDoePhotoName = "johnn_doe.jpg";
        String lindaDouglassPhotoName = "linda_douglass.jpg";
        String rafaelOrtegaPhotoName = "rafael_o.jpg";
        String sharonJenkinsPhotoName = "sharon_jenkins.jpg";

        String JohnPhoto1 = "john_photo1.jpg";
        String JohnPhoto2 = "john_photo2.jpg";
        String JohnPhoto3 = "john_photo3.jpg";
        String JohnPhoto4 = "john_photo4.jpg";
        String JohnPhoto5 = "john_photo5.jpg";

        String HelenPhoto1 = "helen_photo1.jpg";
        String HelenPhoto2 = "helen_photo2.jpg";
        String HelenPhoto3 = "helen_photo3.jpg";
        String HelenPhoto4 = "helen_photo4.jpg";
        String HelenPhoto5 = "helen_photo5.jpg";

        String HenryPhoto1 = "henry_photo1.jpg";
        String HenryPhoto2 = "henry_photo2.jpg";
        String HenryPhoto3 = "henry_photo3.jpg";
        String HenryPhoto4 = "henry_photo4.jpg";
        String HenryPhoto5 = "henry_photo5.jpg";

        String JamesPhoto1 = "james_photo1.jpg";
        String JamesPhoto2 = "james_photo2.jpg";
        String JamesPhoto3 = "james_photo3.jpg";
        String JamesPhoto4 = "james_photo4.jpg";
        String JamesPhoto5 = "james_photo5.jpg";

        String LindaPhoto1 = "linda_photo1.jpg";
        String LindaPhoto2 = "linda_photo2.jpg";
        String LindaPhoto3 = "linda_photo3.jpg";
        String LindaPhoto4 = "linda_photo4.jpg";
        String LindaPhoto5 = "linda_photo5.jpg";

        String RafaelPhoto1 = "rafael_photo1.jpg";
        String RafaelPhoto2 = "rafael_photo2.jpg";
        String RafaelPhoto3 = "rafael_photo3.jpg";
        String RafaelPhoto4 = "rafael_photo4.jpg";
        String RafaelPhoto5 = "rafael_photo5.jpg";

        String SharonPhoto1 = "sharon_photo1.jpg";
        String SharonPhoto2 = "sharon_photo2.jpg";
        String SharonPhoto3 = "sharon_photo3.jpg";
        String SharonPhoto4 = "sharon_photo4.jpg";
        String SharonPhoto5 = "sharon_photo5.jpg";


        ClassPathResource defaultPhoto = new ClassPathResource("images/" + defaultPhotoName);
        ClassPathResource jamesCarterPhoto = new ClassPathResource("images/" + jamesCarterPhotoName);
        ClassPathResource helenLearyPhoto = new ClassPathResource("images/" + helenLearyPhotoName);
        ClassPathResource henryStevensPhoto = new ClassPathResource("images/" + henryStevensPhotoName);
        ClassPathResource johnDoePhoto = new ClassPathResource("images/" + johnDoePhotoName);
        ClassPathResource lindaDouglassPhoto = new ClassPathResource("images/" + lindaDouglassPhotoName);
        ClassPathResource rafaelOrtegaPhoto = new ClassPathResource("images/" + rafaelOrtegaPhotoName);
        ClassPathResource sharonJenkinsPhoto = new ClassPathResource("images/" + sharonJenkinsPhotoName);

        ClassPathResource John1 = new ClassPathResource("album/" + JohnPhoto1);
        ClassPathResource John2 = new ClassPathResource("album/" + JohnPhoto2);
        ClassPathResource John3 = new ClassPathResource("album/" + JohnPhoto3);
        ClassPathResource John4 = new ClassPathResource("album/" + JohnPhoto4);
        ClassPathResource John5 = new ClassPathResource("album/" + JohnPhoto5);

        ClassPathResource Helen1 = new ClassPathResource("album/" + HelenPhoto1);
        ClassPathResource Helen2 = new ClassPathResource("album/" + HelenPhoto2);
        ClassPathResource Helen3 = new ClassPathResource("album/" + HelenPhoto3);
        ClassPathResource Helen4 = new ClassPathResource("album/" + HelenPhoto4);
        ClassPathResource Helen5 = new ClassPathResource("album/" + HelenPhoto5);

        ClassPathResource Henry1 = new ClassPathResource("album/" + HenryPhoto1);
        ClassPathResource Henry2 = new ClassPathResource("album/" + HenryPhoto2);
        ClassPathResource Henry3 = new ClassPathResource("album/" + HenryPhoto3);
        ClassPathResource Henry4 = new ClassPathResource("album/" + HenryPhoto4);
        ClassPathResource Henry5 = new ClassPathResource("album/" + HenryPhoto5);

        ClassPathResource James1 = new ClassPathResource("album/" + JamesPhoto1);
        ClassPathResource James2 = new ClassPathResource("album/" + JamesPhoto2);
        ClassPathResource James3 = new ClassPathResource("album/" + JamesPhoto3);
        ClassPathResource James4 = new ClassPathResource("album/" + JamesPhoto4);
        ClassPathResource James5 = new ClassPathResource("album/" + JamesPhoto5);

        ClassPathResource Linda1 = new ClassPathResource("album/" + LindaPhoto1);
        ClassPathResource Linda2 = new ClassPathResource("album/" + LindaPhoto2);
        ClassPathResource Linda3 = new ClassPathResource("album/" + LindaPhoto3);
        ClassPathResource Linda4 = new ClassPathResource("album/" + LindaPhoto4);
        ClassPathResource Linda5 = new ClassPathResource("album/" + LindaPhoto5);

        ClassPathResource Rafael1 = new ClassPathResource("album/" + RafaelPhoto1);
        ClassPathResource Rafael2 = new ClassPathResource("album/" + RafaelPhoto2);
        ClassPathResource Rafael3 = new ClassPathResource("album/" + RafaelPhoto3);
        ClassPathResource Rafael4 = new ClassPathResource("album/" + RafaelPhoto4);
        ClassPathResource Rafael5 = new ClassPathResource("album/" + RafaelPhoto5);

        ClassPathResource Sharon1 = new ClassPathResource("album/" + SharonPhoto1);
        ClassPathResource Sharon2 = new ClassPathResource("album/" + SharonPhoto2);
        ClassPathResource Sharon3 = new ClassPathResource("album/" + SharonPhoto3);
        ClassPathResource Sharon4 = new ClassPathResource("album/" + SharonPhoto4);
        ClassPathResource Sharon5 = new ClassPathResource("album/" + SharonPhoto5);


        Photo photo1 = Photo.builder()
                .vetId(v1.getVetId())
                .filename(defaultPhotoName)
                .imgType(defaultPhotoType)
                .data(StreamUtils.copyToByteArray(jamesCarterPhoto.getInputStream()))
                .build();

        Photo photo2 = Photo.builder()
                .vetId(v2.getVetId())
                .filename(defaultPhotoName)
                .imgType(defaultPhotoType)
                .data(StreamUtils.copyToByteArray(helenLearyPhoto.getInputStream()))
                .build();

        Photo photo3 = Photo.builder()
                .vetId(v3.getVetId())
                .filename(defaultPhotoName)
                .imgType(defaultPhotoType)
                .data(StreamUtils.copyToByteArray(lindaDouglassPhoto.getInputStream()))
                .build();

        Photo photo4 = Photo.builder()
                .vetId(v4.getVetId())
                .filename(defaultPhotoName)
                .imgType(defaultPhotoType)
                .data(StreamUtils.copyToByteArray(rafaelOrtegaPhoto.getInputStream()))
                .build();

        Photo photo5 = Photo.builder()
                .vetId(v5.getVetId())
                .filename(defaultPhotoName)
                .imgType(defaultPhotoType)
                .data(StreamUtils.copyToByteArray(henryStevensPhoto.getInputStream()))
                .build();

        Photo photo6 = Photo.builder()
                .vetId(v6.getVetId())
                .filename(defaultPhotoName)
                .imgType(defaultPhotoType)
                .data(StreamUtils.copyToByteArray(sharonJenkinsPhoto.getInputStream()))
                .build();

        Photo photo7 = Photo.builder()
                .vetId(v7.getVetId())
                .filename(defaultPhotoName)
                .imgType(defaultPhotoType)
                .data(StreamUtils.copyToByteArray(johnDoePhoto.getInputStream()))
                .build();

        Album album1 = Album.builder()
                .vetId(v7.getVetId())
                .filename("john_photo1.jpg")
                .imgType("image/jpeg")
                .data(StreamUtils.copyToByteArray(John1.getInputStream()))
                .build();

        log.info("Album created: vetId={}, filename={}, imgType={}, size={} bytes",
                album1.getVetId(), album1.getFilename(), album1.getImgType(), album1.getData().length);

        Album album2 = Album.builder()
                .vetId(v7.getVetId())
                .filename("john_photo2.jpg")
                .imgType("image/jpeg")
                .data(StreamUtils.copyToByteArray(John2.getInputStream()))
                .build();

        log.info("Album created: vetId={}, filename={}, imgType={}, size={} bytes",
                album2.getVetId(), album2.getFilename(), album2.getImgType(), album2.getData().length);

        Album album3 = Album.builder()
                .vetId(v7.getVetId())
                .filename("john_photo3.jpg")
                .imgType("image/jpeg")
                .data(StreamUtils.copyToByteArray(John3.getInputStream()))
                .build();

        log.info("Album created: vetId={}, filename={}, imgType={}, size={} bytes",
                album3.getVetId(), album3.getFilename(), album3.getImgType(), album3.getData().length);

        Album album4 = Album.builder()
                .vetId(v7.getVetId())
                .filename("john_photo4.jpg")
                .imgType("image/jpeg")
                .data(StreamUtils.copyToByteArray(John4.getInputStream()))
                .build();

        log.info("Album created: vetId={}, filename={}, imgType={}, size={} bytes",
                album4.getVetId(), album4.getFilename(), album4.getImgType(), album4.getData().length);

        Album album5 = Album.builder()
                .vetId(v7.getVetId())
                .filename("john_photo5.jpg")
                .imgType("image/jpeg")
                .data(StreamUtils.copyToByteArray(John5.getInputStream()))
                .build();

        log.info("Album created: vetId={}, filename={}, imgType={}, size={} bytes",
                album5.getVetId(), album5.getFilename(), album5.getImgType(), album5.getData().length);

        Album album6 = Album.builder()
                .vetId(v2.getVetId())
                .filename("helen_photo1.jpg")
                .imgType("image/jpeg")
                .data(StreamUtils.copyToByteArray(Helen1.getInputStream()))
                .build();

        log.info("Album created: vetId={}, filename={}, imgType={}, size={} bytes",
                album6.getVetId(), album6.getFilename(), album6.getImgType(), album6.getData().length);

        Album album7 = Album.builder()
                .vetId(v2.getVetId())
                .filename("helen_photo2.jpg")
                .imgType("image/jpeg")
                .data(StreamUtils.copyToByteArray(Helen2.getInputStream()))
                .build();

        log.info("Album created: vetId={}, filename={}, imgType={}, size={} bytes",
                album7.getVetId(), album7.getFilename(), album7.getImgType(), album7.getData().length);

        Album album8 = Album.builder()
                .vetId(v2.getVetId())
                .filename("helen_photo3.jpg")
                .imgType("image/jpeg")
                .data(StreamUtils.copyToByteArray(Helen3.getInputStream()))
                .build();

        log.info("Album created: vetId={}, filename={}, imgType={}, size={} bytes",
                album8.getVetId(), album8.getFilename(), album8.getImgType(), album8.getData().length);

        Album album9 = Album.builder()
                .vetId(v2.getVetId())
                .filename("helen_photo4.jpg")
                .imgType("image/jpeg")
                .data(StreamUtils.copyToByteArray(Helen4.getInputStream()))
                .build();

        log.info("Album created: vetId={}, filename={}, imgType={}, size={} bytes",
                album9.getVetId(), album9.getFilename(), album9.getImgType(), album9.getData().length);

        Album album10 = Album.builder()
                .vetId(v2.getVetId())
                .filename("helen_photo5.jpg")
                .imgType("image/jpeg")
                .data(StreamUtils.copyToByteArray(Helen5.getInputStream()))
                .build();

        log.info("Album created: vetId={}, filename={}, imgType={}, size={} bytes",
                album10.getVetId(), album10.getFilename(), album10.getImgType(), album10.getData().length);

        Album album11 = Album.builder()
                .vetId(v5.getVetId())
                .filename("henry_photo1.jpg")
                .imgType("image/jpeg")
                .data(StreamUtils.copyToByteArray(Henry1.getInputStream()))
                .build();

        log.info("Album created: vetId={}, filename={}, imgType={}, size={} bytes",
                album11.getVetId(), album11.getFilename(), album11.getImgType(), album11.getData().length);

        Album album12 = Album.builder()
                .vetId(v5.getVetId())
                .filename("henry_photo2.jpg")
                .imgType("image/jpeg")
                .data(StreamUtils.copyToByteArray(Henry2.getInputStream()))
                .build();

        log.info("Album created: vetId={}, filename={}, imgType={}, size={} bytes",
                album12.getVetId(), album12.getFilename(), album12.getImgType(), album12.getData().length);

        Album album13 = Album.builder()
                .vetId(v5.getVetId())
                .filename("henry_photo3.jpg")
                .imgType("image/jpeg")
                .data(StreamUtils.copyToByteArray(Henry3.getInputStream()))
                .build();

        log.info("Album created: vetId={}, filename={}, imgType={}, size={} bytes",
                album13.getVetId(), album13.getFilename(), album13.getImgType(), album13.getData().length);

        Album album14 = Album.builder()
                .vetId(v5.getVetId())
                .filename("henry_photo4.jpg")
                .imgType("image/jpeg")
                .data(StreamUtils.copyToByteArray(Henry4.getInputStream()))
                .build();

        log.info("Album created: vetId={}, filename={}, imgType={}, size={} bytes",
                album14.getVetId(), album14.getFilename(), album14.getImgType(), album14.getData().length);

        Album album15 = Album.builder()
                .vetId(v5.getVetId())
                .filename("henry_photo5.jpg")
                .imgType("image/jpeg")
                .data(StreamUtils.copyToByteArray(Henry5.getInputStream()))
                .build();

        log.info("Album created: vetId={}, filename={}, imgType={}, size={} bytes",
                album15.getVetId(), album15.getFilename(), album15.getImgType(), album15.getData().length);

        Album album16 = Album.builder()
                .vetId(v1.getVetId())
                .filename("james_photo1.jpg")
                .imgType("image/jpeg")
                .data(StreamUtils.copyToByteArray(James1.getInputStream()))
                .build();

        log.info("Album created: vetId={}, filename={}, imgType={}, size={} bytes",
                album16.getVetId(), album16.getFilename(), album16.getImgType(), album16.getData().length);

        Album album17 = Album.builder()
                .vetId(v1.getVetId())
                .filename("james_photo2.jpg")
                .imgType("image/jpeg")
                .data(StreamUtils.copyToByteArray(James2.getInputStream()))
                .build();

        log.info("Album created: vetId={}, filename={}, imgType={}, size={} bytes",
                album17.getVetId(), album17.getFilename(), album17.getImgType(), album17.getData().length);

        Album album18 = Album.builder()
                .vetId(v1.getVetId())
                .filename("james_photo3.jpg")
                .imgType("image/jpeg")
                .data(StreamUtils.copyToByteArray(James3.getInputStream()))
                .build();

        log.info("Album created: vetId={}, filename={}, imgType={}, size={} bytes",
                album18.getVetId(), album18.getFilename(), album18.getImgType(), album18.getData().length);

        Album album19 = Album.builder()
                .vetId(v1.getVetId())
                .filename("james_photo4.jpg")
                .imgType("image/jpeg")
                .data(StreamUtils.copyToByteArray(James4.getInputStream()))
                .build();

        log.info("Album created: vetId={}, filename={}, imgType={}, size={} bytes",
                album19.getVetId(), album19.getFilename(), album19.getImgType(), album19.getData().length);

        Album album20 = Album.builder()
                .vetId(v1.getVetId())
                .filename("james_photo5.jpg")
                .imgType("image/jpeg")
                .data(StreamUtils.copyToByteArray(James5.getInputStream()))
                .build();

        log.info("Album created: vetId={}, filename={}, imgType={}, size={} bytes",
                album20.getVetId(), album20.getFilename(), album20.getImgType(), album20.getData().length);

        Album album21 = Album.builder()
                .vetId(v3.getVetId())
                .filename("linda_photo1.jpg")
                .imgType("image/jpeg")
                .data(StreamUtils.copyToByteArray(Linda1.getInputStream()))
                .build();

        log.info("Album created: vetId={}, filename={}, imgType={}, size={} bytes",
                album21.getVetId(), album21.getFilename(), album21.getImgType(), album21.getData().length);

        Album album22 = Album.builder()
                .vetId(v3.getVetId())
                .filename("linda_photo2.jpg")
                .imgType("image/jpeg")
                .data(StreamUtils.copyToByteArray(Linda2.getInputStream()))
                .build();

        log.info("Album created: vetId={}, filename={}, imgType={}, size={} bytes",
                album22.getVetId(), album22.getFilename(), album22.getImgType(), album22.getData().length);

        Album album23 = Album.builder()
                .vetId(v3.getVetId())
                .filename("linda_photo3.jpg")
                .imgType("image/jpeg")
                .data(StreamUtils.copyToByteArray(Linda3.getInputStream()))
                .build();

        log.info("Album created: vetId={}, filename={}, imgType={}, size={} bytes",
                album23.getVetId(), album23.getFilename(), album23.getImgType(), album23.getData().length);


        Album album24 = Album.builder()
                .vetId(v3.getVetId())
                .filename("linda_photo4.jpg")
                .imgType("image/jpeg")
                .data(StreamUtils.copyToByteArray(Linda4.getInputStream()))
                .build();

        log.info("Album created: vetId={}, filename={}, imgType={}, size={} bytes",
                album24.getVetId(), album24.getFilename(), album24.getImgType(), album24.getData().length);

        Album album25 = Album.builder()
                .vetId(v3.getVetId())
                .filename("linda_photo5.jpg")
                .imgType("image/jpeg")
                .data(StreamUtils.copyToByteArray(Linda5.getInputStream()))
                .build();

        log.info("Album created: vetId={}, filename={}, imgType={}, size={} bytes",
                album25.getVetId(), album25.getFilename(), album25.getImgType(), album25.getData().length);

        Album album26 = Album.builder()
                .vetId(v4.getVetId())
                .filename("rafael_photo1.jpg")
                .imgType("image/jpeg")
                .data(StreamUtils.copyToByteArray(Rafael1.getInputStream()))
                .build();

        log.info("Album created: vetId={}, filename={}, imgType={}, size={} bytes",
                album26.getVetId(), album26.getFilename(), album26.getImgType(), album26.getData().length);

        Album album27 = Album.builder()
                .vetId(v4.getVetId())
                .filename("rafael_photo2.jpg")
                .imgType("image/jpeg")
                .data(StreamUtils.copyToByteArray(Rafael2.getInputStream()))
                .build();

        log.info("Album created: vetId={}, filename={}, imgType={}, size={} bytes",
                album27.getVetId(), album27.getFilename(), album27.getImgType(), album27.getData().length);

        Album album28 = Album.builder()
                .vetId(v4.getVetId())
                .filename("rafael_photo3.jpg")
                .imgType("image/jpeg")
                .data(StreamUtils.copyToByteArray(Rafael3.getInputStream()))
                .build();

        log.info("Album created: vetId={}, filename={}, imgType={}, size={} bytes",
                album28.getVetId(), album28.getFilename(), album28.getImgType(), album28.getData().length);

        Album album29 = Album.builder()
                .vetId(v4.getVetId())
                .filename("rafael_photo4.jpg")
                .imgType("image/jpeg")
                .data(StreamUtils.copyToByteArray(Rafael4.getInputStream()))
                .build();

        log.info("Album created: vetId={}, filename={}, imgType={}, size={} bytes",
                album29.getVetId(), album29.getFilename(), album29.getImgType(), album29.getData().length);

        Album album30 = Album.builder()
                .vetId(v4.getVetId())
                .filename("rafael_photo5.jpg")
                .imgType("image/jpeg")
                .data(StreamUtils.copyToByteArray(Rafael5.getInputStream()))
                .build();

        log.info("Album created: vetId={}, filename={}, imgType={}, size={} bytes",
                album30.getVetId(), album30.getFilename(), album30.getImgType(), album30.getData().length);

        Album album31 = Album.builder()
                .vetId(v6.getVetId())
                .filename("sharon_photo1.jpg")
                .imgType("image/jpeg")
                .data(StreamUtils.copyToByteArray(Sharon1.getInputStream()))
                .build();

        log.info("Album created: vetId={}, filename={}, imgType={}, size={} bytes",
                album31.getVetId(), album31.getFilename(), album31.getImgType(), album31.getData().length);


        Album album32 = Album.builder()
                .vetId(v6.getVetId())
                .filename("sharon_photo2.jpg")
                .imgType("image/jpeg")
                .data(StreamUtils.copyToByteArray(Sharon2.getInputStream()))
                .build();

        log.info("Album created: vetId={}, filename={}, imgType={}, size={} bytes",
                album32.getVetId(), album32.getFilename(), album32.getImgType(), album32.getData().length);

        Album album33 = Album.builder()
                .vetId(v6.getVetId())
                .filename("sharon_photo3.jpg")
                .imgType("image/jpeg")
                .data(StreamUtils.copyToByteArray(Sharon3.getInputStream()))
                .build();

        log.info("Album created: vetId={}, filename={}, imgType={}, size={} bytes",
                album33.getVetId(), album33.getFilename(), album33.getImgType(), album33.getData().length);

        Album album34 = Album.builder()
                .vetId(v6.getVetId())
                .filename("sharon_photo4.jpg")
                .imgType("image/jpeg")
                .data(StreamUtils.copyToByteArray(Sharon4.getInputStream()))
                .build();

        log.info("Album created: vetId={}, filename={}, imgType={}, size={} bytes",
                album34.getVetId(), album34.getFilename(), album34.getImgType(), album34.getData().length);

        Album album35 = Album.builder()
                .vetId(v6.getVetId())
                .filename("sharon_photo5.jpg")
                .imgType("image/jpeg")
                .data(StreamUtils.copyToByteArray(Sharon5.getInputStream()))
                .build();

        log.info("Album created: vetId={}, filename={}, imgType={}, size={} bytes",
                album35.getVetId(), album35.getFilename(), album35.getImgType(), album35.getData().length);

        Flux.just(album1, album2, album3, album4, album5, album6, album7, album8, album9, album10, album11, album12, album13, album14, album15, album16, album17, album18, album19, album20, album21, album22, album23, album24, album25, album26, album27, album28, album29, album30, album31, album32, album33, album34, album35)
                .doOnNext(album -> log.info("Album created: vetId={}, filename={}, imgType={}, dataSize={} bytes",
                        album.getVetId(), album.getFilename(), album.getImgType(), album.getData().length))
                .flatMap(albumRepository::save)
                .log()
                .subscribe();


        Badge b1 = Badge.builder()
                .vetId(v1.getVetId())
                .badgeTitle(BadgeTitle.HIGHLY_RESPECTED)
                .badgeDate("2020")
                .data(StreamUtils.copyToByteArray(cpr3.getInputStream()))
                .build();
        Badge b2 = Badge.builder()
                .vetId(v2.getVetId())
                .badgeTitle(BadgeTitle.HIGHLY_RESPECTED)
                .badgeDate("2022")
                .data(StreamUtils.copyToByteArray(cpr3.getInputStream()))
                .build();
        Badge b3 = Badge.builder()
                .vetId(v3.getVetId())
                .badgeTitle(BadgeTitle.MUCH_APPRECIATED)
                .badgeDate("2023")
                .data(StreamUtils.copyToByteArray(cpr2.getInputStream()))
                .build();
        Badge b4 = Badge.builder()
                .vetId(v4.getVetId())
                .badgeTitle(BadgeTitle.VALUED)
                .badgeDate("2010")
                .data(StreamUtils.copyToByteArray(cpr1.getInputStream()))
                .build();
        Badge b5 = Badge.builder()
                .vetId(v5.getVetId())
                .badgeTitle(BadgeTitle.VALUED)
                .badgeDate("2013")
                .data(StreamUtils.copyToByteArray(cpr1.getInputStream()))
                .build();
        Badge b6 = Badge.builder()
                .vetId(v6.getVetId())
                .badgeTitle(BadgeTitle.VALUED)
                .badgeDate("2016")
                .data(StreamUtils.copyToByteArray(cpr1.getInputStream()))
                .build();
        Badge b7 = Badge.builder()
                .vetId(v7.getVetId())
                .badgeTitle(BadgeTitle.VALUED)
                .badgeDate("2018")
                .data(StreamUtils.copyToByteArray(cpr1.getInputStream()))
                .build();

        // Use method defined to create datasource
        DataSource dataSource = EntityDtoUtil.createDataSource();
        try (
                // get connection from datasource
                Connection conn = dataSource.getConnection();

                // Prepare INSERT statement
                PreparedStatement insertStmt = conn.prepareStatement(
                        "INSERT INTO badges (vet_id, badge_title, badge_date, img_data) " +
                                "VALUES (?, ?, ?, ?)")
        ) {

            // Define Badge objects (b1, b2, ..., b7) and set parameters for PreparedStatement
            Badge[] badges = {b1, b2, b3, b4, b5, b6, b7};

            for (Badge badge : badges) {
                insertStmt.setString(1, badge.getVetId());
                insertStmt.setString(2, badge.getBadgeTitle().name());
                insertStmt.setString(3, badge.getBadgeDate());

                // Assuming badge.getData() returns image data as byte array
                insertStmt.setBytes(4, badge.getData());

                // Run insert query for each badge
                int insertedRows = insertStmt.executeUpdate();

                // Print out number of inserted rows for each badge
                System.out.printf("Inserted %d badge(s)%n", insertedRows);
            }

            // Close PreparedStatement after use
            insertStmt.close();
        } catch (SQLException e) {
            // Handle any SQL exceptions
            e.printStackTrace();
        }

        try (
                // get connection from datasource
                Connection conn = dataSource.getConnection();

                // Prepare INSERT statement
                PreparedStatement insertStmt = conn.prepareStatement(
                        "INSERT INTO images (vet_id, filename, img_type, img_data) " +
                                "VALUES (?, ?, ?, ?)")
        ) {

            Photo[] photos = {photo1, photo2, photo3, photo4, photo5, photo6, photo7};

            for (Photo photo : photos) {
                insertStmt.setString(1, photo.getVetId());
                insertStmt.setString(2, photo.getFilename());
                insertStmt.setString(3, photo.getImgType());
                insertStmt.setBytes(4, photo.getData());

                int insertedRows = insertStmt.executeUpdate();
                System.out.printf("Inserted %d defaultPhoto(s)%n", insertedRows);
            }

            insertStmt.close();
        } catch (SQLException e) {
            // Handle any SQL exceptions
            e.printStackTrace();
        }
    }

    /*private static Map<Workday, List<WorkHour>> getWorkHours(String workHoursJson) {
        return getWorkHoursFromJson(workHoursJson);
    }*/

    //method that converts the work hours map to a string
    private static String setWorkHours(Map<Workday, List<WorkHour>> workHours) {
        try {
            String workHoursJson = new ObjectMapper().writeValueAsString(workHours);
            return workHoursJson;
        } catch (JsonProcessingException e) {
            throw new InvalidInputException("Work hours are invalid");
        }
    }

    //method to get the work hours string in json format to a map of work hours
    /*private static Map<Workday, List<WorkHour>> getWorkHoursFromJson(String json) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (IOException e) {
            throw new InvalidInputException("Work hours are invalid");
        }
    }*/
}
