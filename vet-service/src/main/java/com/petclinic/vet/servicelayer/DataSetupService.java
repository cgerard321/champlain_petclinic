package com.petclinic.vet.servicelayer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petclinic.vet.dataaccesslayer.*;
import com.petclinic.vet.dataaccesslayer.badges.Badge;
import com.petclinic.vet.dataaccesslayer.badges.BadgeRepository;
import com.petclinic.vet.dataaccesslayer.badges.BadgeTitle;
import com.petclinic.vet.dataaccesslayer.education.Education;
import com.petclinic.vet.dataaccesslayer.education.EducationRepository;
import com.petclinic.vet.dataaccesslayer.ratings.PredefinedDescription;
import com.petclinic.vet.dataaccesslayer.ratings.Rating;
import com.petclinic.vet.dataaccesslayer.ratings.RatingRepository;
import com.petclinic.vet.exceptions.InvalidInputException;
import com.petclinic.vet.util.EntityDtoUtil;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.ds.PGSimpleDataSource;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import reactor.core.publisher.Flux;

import java.io.IOException;
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

    public DataSetupService(VetRepository vetRepository, RatingRepository ratingRepository, EducationRepository educationRepository, BadgeRepository badgeRepository, PhotoRepository photoRepository){
        this.vetRepository = vetRepository;
        this.ratingRepository = ratingRepository;
        this.educationRepository = educationRepository;
        this.badgeRepository=badgeRepository;
        this.photoRepository = photoRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        Specialty s1 = new Specialty("100001", "radiology");
        Specialty s2 = new Specialty("100002", "surgery");
        Specialty s3 = new Specialty("100003", "dentistry");
        Specialty s4 = new Specialty("100004", "general");

        Set<Workday> workdays1 = EnumSet.of(Workday.Monday, Workday.Tuesday, Workday.Friday);
        Set<Workday> workdays2 = EnumSet.of(Workday.Wednesday, Workday.Thursday);
        Set<Workday> workdays3 = EnumSet.of(Workday.Monday, Workday.Wednesday, Workday.Thursday);
        Set<Workday> workdays4 = EnumSet.of(Workday.Wednesday, Workday.Thursday, Workday.Friday);
        Set<Workday> workdays5 = EnumSet.of(Workday.Monday, Workday.Tuesday, Workday.Wednesday, Workday.Thursday);
        Set<Workday> workdays6 = EnumSet.of(Workday.Monday, Workday.Tuesday, Workday.Friday);

        List<WorkHour> workHourList1=new ArrayList<>();
        workHourList1.addAll(Arrays.asList(WorkHour.Hour_8_9, WorkHour.Hour_9_10, WorkHour.Hour_10_11, WorkHour.Hour_11_12, WorkHour.Hour_12_13, WorkHour.Hour_13_14, WorkHour.Hour_14_15, WorkHour.Hour_15_16));
        List<WorkHour> workHourList2=new ArrayList<>();
        workHourList2.addAll(Arrays.asList(WorkHour.Hour_12_13, WorkHour.Hour_13_14, WorkHour.Hour_14_15, WorkHour.Hour_15_16, WorkHour.Hour_16_17, WorkHour.Hour_17_18, WorkHour.Hour_18_19, WorkHour.Hour_19_20));
        List<WorkHour> workHourList3=new ArrayList<>();
        workHourList3.addAll(Arrays.asList(WorkHour.Hour_10_11, WorkHour.Hour_11_12, WorkHour.Hour_12_13, WorkHour.Hour_13_14, WorkHour.Hour_14_15, WorkHour.Hour_15_16, WorkHour.Hour_16_17, WorkHour.Hour_17_18));
        List<WorkHour> workHourList4=new ArrayList<>();
        workHourList4.addAll(Arrays.asList(WorkHour.Hour_8_9, WorkHour.Hour_9_10, WorkHour.Hour_10_11, WorkHour.Hour_11_12));
        List<WorkHour> workHourList5=new ArrayList<>();
        workHourList5.addAll(Arrays.asList(WorkHour.Hour_14_15, WorkHour.Hour_15_16, WorkHour.Hour_16_17, WorkHour.Hour_17_18));
        List<WorkHour> workHourList6=new ArrayList<>();
        workHourList6.addAll(Arrays.asList(WorkHour.Hour_16_17, WorkHour.Hour_17_18, WorkHour.Hour_18_19, WorkHour.Hour_19_20));

        //list of the work hours
        List<WorkHour>[] workHourLists = new List[] {workHourList1, workHourList2, workHourList3, workHourList4, workHourList5, workHourList6};

        //work hours each day for vet 1
        Map<Workday, List<WorkHour>> workHours1=new HashMap<>();
        List<Workday> workdayList1 = new ArrayList<>(workdays1);
        for (int i = 0; i < workdayList1.size(); i++) {
            Workday workday = workdayList1.get(i);
            List<WorkHour> workHourList = workHourLists[i];
            workHours1.put(workday, workHourList);
        }

        //work hours each day for vet 2
        Map<Workday, List<WorkHour>> workHours2=new HashMap<>();
        List<Workday> workdayList2 = new ArrayList<>(workdays2);
        for (int i = 0; i < workdayList2.size(); i++) {
            Workday workday = workdayList2.get(i);
            List<WorkHour> workHourList = workHourLists[i];
            workHours2.put(workday, workHourList);
        }

        //work hours each day for vet 3
        Map<Workday, List<WorkHour>> workHours3=new HashMap<>();
        List<Workday> workdayList3 = new ArrayList<>(workdays3);
        for (int i = 0; i < workdayList3.size(); i++) {
            Workday workday = workdayList3.get(i);
            List<WorkHour> workHourList = workHourLists[i];
            workHours3.put(workday, workHourList);
        }

        //work hours each day for vet 4
        Map<Workday, List<WorkHour>> workHours4=new HashMap<>();
        List<Workday> workdayList4 = new ArrayList<>(workdays4);
        for (int i = 0; i < workdayList4.size(); i++) {
            Workday workday = workdayList4.get(i);
            List<WorkHour> workHourList = workHourLists[i];
            workHours4.put(workday, workHourList);
        }

        //work hours each day for vet 5
        Map<Workday, List<WorkHour>> workHours5=new HashMap<>();
        List<Workday> workdayList5 = new ArrayList<>(workdays5);
        for (int i = 0; i < workdayList5.size(); i++) {
            Workday workday = workdayList5.get(i);
            List<WorkHour> workHourList = workHourLists[i];
            workHours5.put(workday, workHourList);
        }

        //work hours each day for vet 6
        Map<Workday, List<WorkHour>> workHours6=new HashMap<>();
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
                .rateScore(5.0)
                .rateDescription(null)
                .predefinedDescription(PredefinedDescription.EXCELLENT)
                .date(date1)
                .build();
        Rating r2 = Rating.builder()
                .ratingId(UUID.randomUUID().toString())
                .vetId(v2.getVetId())
                .rateScore(4.0)
                .predefinedDescription(null)
                .rateDescription("Good vet.")
                .date(date2)
                .build();
        Rating r3 = Rating.builder()
                .ratingId(UUID.randomUUID().toString())
                .vetId(v3.getVetId())
                .rateScore(3.0)
                .rateDescription("The vet is ok.")
                .predefinedDescription(null)
                .date(date3)
                .build();
        Rating r4 = Rating.builder()
                .ratingId(UUID.randomUUID().toString())
                .vetId(v3.getVetId())
                .rateScore(4.0)
                .rateDescription(null)
                .predefinedDescription(PredefinedDescription.GOOD)
                .date(date4)
                .build();
        Flux.just(r1, r2, r3, r4)
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

        ClassPathResource cpr1=new ClassPathResource("images/empty_food_bowl.png");
        ClassPathResource cpr2=new ClassPathResource("images/half-full_food_bowl.png");
        ClassPathResource cpr3=new ClassPathResource("images/full_food_bowl.png");
        ClassPathResource cpr4=new ClassPathResource("images/jamesCarter.png");

        //default photo
        String defaultPhotoName = "vet_default.jpg";
        String jamesCarterPhoto = "jamesCarter.png";
        String jpgPhotoType = "image/jpg";
        String pngPhotoType = "image/png";


        ClassPathResource defaultPhoto = new ClassPathResource("images/" + defaultPhotoName);

        String base64DefaultPhoto = Base64.getEncoder().encodeToString(StreamUtils.copyToByteArray(defaultPhoto.getInputStream()));
        String base64PhotoJames = Base64.getEncoder().encodeToString(StreamUtils.copyToByteArray(cpr4.getInputStream())); // James Carter's photo

        Photo photo1 = Photo.builder()
                .vetId(v1.getVetId())
                .filename(jamesCarterPhoto)
                .imgType(pngPhotoType)
                .imgBase64(base64PhotoJames)
                .build();

        Photo photo2 = Photo.builder()
                .vetId(v2.getVetId())
                .filename(defaultPhotoName)
                .imgType(jpgPhotoType)
                .imgBase64(base64DefaultPhoto)
                .build();

        Photo photo3 = Photo.builder()
                .vetId(v3.getVetId())
                .filename(defaultPhotoName)
                .imgType(jpgPhotoType)
                .imgBase64(base64DefaultPhoto)
                .build();

        Photo photo4 = Photo.builder()
                .vetId(v4.getVetId())
                .filename(defaultPhotoName)
                .imgType(jpgPhotoType)
                .imgBase64(base64DefaultPhoto)
                .build();

        Photo photo5 = Photo.builder()
                .vetId(v5.getVetId())
                .filename(defaultPhotoName)
                .imgType(jpgPhotoType)
                .imgBase64(base64DefaultPhoto)
                .build();

        Photo photo6 = Photo.builder()
                .vetId(v6.getVetId())
                .filename(defaultPhotoName)
                .imgType(jpgPhotoType)
                .imgBase64(base64DefaultPhoto)
                .build();

        Photo photo7 = Photo.builder()
                .vetId(v7.getVetId())
                .filename(defaultPhotoName)
                .imgType(jpgPhotoType)
                .imgBase64(base64DefaultPhoto)
                .build();

        Flux.just(photo1, photo2, photo3, photo4, photo5, photo6, photo7)
                .flatMap(photoRepository::save)
                .log()
                .subscribe();


        String badgeData;

        try {
            byte[] imageBytes = StreamUtils.copyToByteArray(cpr3.getInputStream());
            badgeData = Base64.getEncoder().encodeToString(imageBytes); // Encode to Base64
        } catch (IOException e) {
            e.printStackTrace();
            badgeData = "";
        }


        Badge b1 = Badge.builder()
                .vetId(v1.getVetId())
                .badgeTitle(BadgeTitle.HIGHLY_RESPECTED)
                .badgeDate("2020")
                .imgBase64(badgeData)
                .build();
        Badge b2 = Badge.builder()
                .vetId(v2.getVetId())
                .badgeTitle(BadgeTitle.HIGHLY_RESPECTED)
                .badgeDate("2022")
                .imgBase64(badgeData)
                .build();
        Badge b3 = Badge.builder()
                .vetId(v3.getVetId())
                .badgeTitle(BadgeTitle.MUCH_APPRECIATED)
                .badgeDate("2023")
                .imgBase64(badgeData)
                .build();
        Badge b4 = Badge.builder()
                .vetId(v4.getVetId())
                .badgeTitle(BadgeTitle.VALUED)
                .badgeDate("2010")
                .imgBase64(badgeData)
                .build();
        Badge b5 = Badge.builder()
                .vetId(v5.getVetId())
                .badgeTitle(BadgeTitle.VALUED)
                .badgeDate("2013")
                .imgBase64(badgeData)
                .build();
        Badge b6 = Badge.builder()
                .vetId(v6.getVetId())
                .badgeTitle(BadgeTitle.VALUED)
                .badgeDate("2016")
                .imgBase64(badgeData)
                .build();
        Badge b7 = Badge.builder()
                .vetId(v7.getVetId())
                .badgeTitle(BadgeTitle.VALUED)
                .badgeDate("2018")
                .imgBase64(badgeData)
                .build();

        // Use method defined to create datasource
        DataSource dataSource = EntityDtoUtil.createDataSource();
        try(
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

                // Convert Base64 string to byte array
                byte[] imageBytes = Base64.getDecoder().decode(badge.getImgBase64());

                // Set the byte array in the prepared statement
                insertStmt.setBytes(4, imageBytes);

                // Run insert query for each badge
                int insertedRows = insertStmt.executeUpdate();

                // Print out number of inserted rows for each badge
                System.out.printf("Inserted %d badge(s)%n", insertedRows);
            }


            // Close PreparedStatement after use
            insertStmt.close();
        }
        catch (SQLException e) {
            // Handle any SQL exceptions
            e.printStackTrace();
        }

        try (
                // Get connection from datasource
                Connection conn = dataSource.getConnection();

                // Prepare INSERT statement
                PreparedStatement insertStmt = conn.prepareStatement(
                        "INSERT INTO images (vet_id, filename, img_type, img_data) " +
                                "VALUES (?, ?, ?, ?)")
        ) {
            // Insert photo1 for vet1
            insertStmt.setString(1, photo1.getVetId());  // vet1's ID
            insertStmt.setString(2, photo1.getFilename());  // e.g., vet_default.jpg
            insertStmt.setString(3, photo1.getImgType());  // e.g., image/png or image/jpg
            byte[] imgData = Base64.getDecoder().decode(photo1.getImgBase64());  // Decode the Base64 image data
            insertStmt.setBytes(4, imgData);  // Set the binary image data

            int insertedRows = insertStmt.executeUpdate();  // Execute the insert query
            System.out.printf("Inserted %d photo(s) for vet1%n", insertedRows);
        }

        catch (SQLException e) {
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
