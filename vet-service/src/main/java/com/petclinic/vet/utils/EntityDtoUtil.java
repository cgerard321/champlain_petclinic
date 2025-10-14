package com.petclinic.vet.utils;
/**
 @author Kamilah Hatteea & Brandon Levis : Vet-Service
  * Worked together with (Code with Friends) on IntelliJ IDEA
  * <p>
  * User: @Kamilah Hatteea
  * Date: 2022-09-22
  * Ticket: feat(VVS-CPC-554): edit veterinarian
  * User: Brandon Levis
  * Date: 202
  * Ticket: feat(VVS-CPC-553): add veterinarian
 */

import com.petclinic.vet.dataaccesslayer.badges.Badge;
import com.petclinic.vet.dataaccesslayer.education.Education;
import com.petclinic.vet.dataaccesslayer.photos.Photo;
import com.petclinic.vet.dataaccesslayer.ratings.Rating;
import com.petclinic.vet.dataaccesslayer.vets.Specialty;
import com.petclinic.vet.dataaccesslayer.vets.Vet;
import com.petclinic.vet.presentationlayer.badges.BadgeResponseDTO;
import com.petclinic.vet.presentationlayer.photos.PhotoResponseDTO;
import com.petclinic.vet.presentationlayer.vets.SpecialtyDTO;
import com.petclinic.vet.presentationlayer.vets.VetRequestDTO;
import com.petclinic.vet.presentationlayer.vets.VetResponseDTO;
import com.petclinic.vet.presentationlayer.education.EducationRequestDTO;
import com.petclinic.vet.presentationlayer.education.EducationResponseDTO;
import com.petclinic.vet.presentationlayer.ratings.RatingRequestDTO;
import com.petclinic.vet.presentationlayer.ratings.RatingResponseDTO;
import com.petclinic.vet.utils.exceptions.InvalidInputException;

import lombok.Generated;
import org.postgresql.ds.PGSimpleDataSource;
import org.springframework.beans.BeanUtils;
import org.springframework.core.io.Resource;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.*;

public class EntityDtoUtil {
    @Generated
    public EntityDtoUtil(){}

    public static VetResponseDTO vetEntityToResponseDTO(Vet vet) {
        VetResponseDTO dto = new VetResponseDTO();
        dto.setVetId(vet.getVetId());
        dto.setVetBillId(vet.getVetBillId());
        dto.setFirstName(vet.getFirstName());
        dto.setLastName(vet.getLastName());
        dto.setEmail(vet.getEmail());
        dto.setPhoneNumber(vet.getPhoneNumber());
        dto.setResume(vet.getResume());
        dto.setWorkday(vet.getWorkday());
        dto.setActive(vet.isActive());
        dto.setSpecialties(toDTOSet(vet.getSpecialties()));
        dto.setWorkHoursJson(vet.getWorkHoursJson());
        return dto;
    }

    public static Vet vetRequestDtoToEntity(VetRequestDTO dto) {
        Vet vet = new Vet();
        vet.setVetId(dto.getVetId());
        vet.setVetBillId(dto.getVetBillId());
        vet.setFirstName(dto.getFirstName());
        vet.setLastName(dto.getLastName());
        vet.setEmail(dto.getEmail());
        vet.setPhoneNumber(dto.getPhoneNumber());
        vet.setResume(dto.getResume());
        vet.setWorkday(dto.getWorkday());
        vet.setActive(dto.isActive());
        vet.setSpecialties(toEntitySet(dto.getSpecialties()));
        vet.setWorkHoursJson(dto.getWorkHoursJson());
        return vet;
    }

    public static String generateVetId() {
        return UUID.randomUUID().toString();
    }

    public static Photo toPhotoEntity(String vetId, String photoName, Resource resource) {
        Photo photo = new Photo();
        photo.setFilename(photoName);
        //StreamUtils.copyToByteArray(resource.getInputStream())
        try {
            photo.setData(resource.getInputStream().readAllBytes());
        } catch (IOException io){
            throw new InvalidInputException("Picture does not exist" + io.getMessage());
        }
        photo.setVetId(vetId);
        photo.setImgType("image/" + getPhotoType(photoName));

        return photo;
    }

    public static String getPhotoType(String photoName){
        String type = photoName.split("\\.")[1];
        if(type.equals("jpg"))
            type = "jpeg";
        return type;
    }

    public static SpecialtyDTO toDTO(Specialty specialty) {
        SpecialtyDTO dto = new SpecialtyDTO();
        BeanUtils.copyProperties(specialty, dto);
        return dto;
    }

    public static Specialty toEntity(SpecialtyDTO dto) {
        Specialty specialty = new Specialty();
        BeanUtils.copyProperties(dto, specialty);
        return specialty;
    }

    public static RatingResponseDTO toDTO(Rating rating) {
        RatingResponseDTO dto = new RatingResponseDTO();
        BeanUtils.copyProperties(rating, dto);
        return dto;
    }

    public static EducationResponseDTO toDTO(Education education) {
        EducationResponseDTO dto = new EducationResponseDTO();
        BeanUtils.copyProperties(education, dto);
        return dto;
    }

    public static Education toEntity(EducationRequestDTO educationRequestDTO) {
        Education education = new Education();
        BeanUtils.copyProperties(educationRequestDTO, education);
        return education;
    }

    public static Rating toEntity(RatingRequestDTO ratingRequestDTO) {
        Rating rating = new Rating();
        BeanUtils.copyProperties(ratingRequestDTO, rating);
        return rating;
    }

    public static Set<SpecialtyDTO> toDTOSet(Set<Specialty> specialties) {
        Set<SpecialtyDTO> specialtyDTOS = new HashSet<>();
        for (Specialty specialty:
                specialties) {
            SpecialtyDTO specialtyDTO = toDTO(specialty);
            specialtyDTOS.add(specialtyDTO);
        }

        return specialtyDTOS;
    }

    public static Set<Specialty>  toEntitySet(Set<SpecialtyDTO> specialtyDTOS){
        Set<Specialty> specialties = new HashSet<>();
        for (SpecialtyDTO specialtyDTO:
                specialtyDTOS) {
            Specialty specialty = toEntity(specialtyDTO);
            specialties.add(specialty);
        }

        return specialties;
    }

    public static BadgeResponseDTO toBadgeResponseDTO(Badge badge){
        BadgeResponseDTO badgeResponseDTO=new BadgeResponseDTO();
        badgeResponseDTO.setBadgeDate(badge.getBadgeDate());
        badgeResponseDTO.setBadgeTitle(badge.getBadgeTitle());
        badgeResponseDTO.setVetId(badge.getVetId());
        badgeResponseDTO.setResourceBase64(Base64.getEncoder().encodeToString(badge.getData()));
        return badgeResponseDTO;
    }

    public static PhotoResponseDTO toPhotoResponseDTO(Photo photo){
        PhotoResponseDTO photoResponseDTO = new PhotoResponseDTO();
        photoResponseDTO.setVetId(photo.getVetId());
        photoResponseDTO.setFilename(photo.getFilename());
        photoResponseDTO.setImgType(photo.getImgType());
        if(photo.getFilename().equals("vet_default.jpg"))
            photoResponseDTO.setResourceBase64(Base64.getEncoder().encodeToString(photo.getData()));
        else {
            photoResponseDTO.setResource(photo.getData());
        }
        return photoResponseDTO;
    }

    public static DataSource createDataSource() {
        // url specifies address of database along with username and password
        final String url =
                "jdbc:postgresql://postgres-vet:5432/images?user=user&password=pwd";
        final PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setUrl(url);
        return dataSource;
    }

    public static String verifyId(String id) {
        if(id.length() != 36)
            throw new InvalidInputException("This id is not valid");
        return id;
    }

}
