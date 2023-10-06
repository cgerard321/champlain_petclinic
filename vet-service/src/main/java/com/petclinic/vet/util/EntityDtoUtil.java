package com.petclinic.vet.util;
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

import com.petclinic.vet.dataaccesslayer.Photo;
import com.petclinic.vet.dataaccesslayer.education.Education;
import com.petclinic.vet.dataaccesslayer.ratings.Rating;
import com.petclinic.vet.dataaccesslayer.Specialty;
import com.petclinic.vet.dataaccesslayer.Vet;
import com.petclinic.vet.exceptions.InvalidInputException;
import com.petclinic.vet.servicelayer.*;
import com.petclinic.vet.servicelayer.education.EducationRequestDTO;
import com.petclinic.vet.servicelayer.education.EducationResponseDTO;
import com.petclinic.vet.servicelayer.ratings.RatingRequestDTO;
import com.petclinic.vet.servicelayer.ratings.RatingResponseDTO;
import lombok.Generated;
import org.springframework.beans.BeanUtils;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class EntityDtoUtil {
    @Generated
    public EntityDtoUtil(){}

    public static VetDTO toDTO(Vet vet) {
        VetDTO dto = new VetDTO();
        dto.setVetId(vet.getVetId());
        dto.setVetBillId(vet.getVetBillId());
        dto.setFirstName(vet.getFirstName());
        dto.setLastName(vet.getLastName());
        dto.setEmail(vet.getEmail());
        dto.setPhoneNumber(vet.getPhoneNumber());
        dto.setImageId(vet.getImageId());
        dto.setResume(vet.getResume());
        dto.setWorkday(vet.getWorkday());
        dto.setActive(vet.isActive());
        dto.setSpecialties(toDTOSet(vet.getSpecialties()));
        return dto;
    }

    public static Vet toEntity(VetDTO dto) {
        Vet vet = new Vet();
        vet.setVetId(dto.getVetId());
        vet.setVetBillId(dto.getVetBillId());
        vet.setFirstName(dto.getFirstName());
        vet.setLastName(dto.getLastName());
        vet.setEmail(dto.getEmail());
        vet.setPhoneNumber(dto.getPhoneNumber());
        vet.setImageId(dto.getImageId());
        vet.setResume(dto.getResume());
        vet.setWorkday(dto.getWorkday());
        vet.setActive(dto.isActive());
        vet.setSpecialties(toEntitySet(dto.getSpecialties()));
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

    public static String verifyId(String id) {
        if(id.length() != 36)
            throw new InvalidInputException("This id is not valid");
        return id;
    }

}
