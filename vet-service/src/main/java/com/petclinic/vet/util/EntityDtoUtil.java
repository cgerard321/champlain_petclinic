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

import com.petclinic.vet.dataaccesslayer.Vet;
import com.petclinic.vet.servicelayer.VetDTO;

import org.springframework.beans.BeanUtils;

import java.util.UUID;

public class EntityDtoUtil {


    public static VetDTO toDTO(Vet Vet) {
        VetDTO dto = new VetDTO();
        BeanUtils.copyProperties(Vet, dto);
        return dto;
    }

    public static Vet toEntity(VetDTO dto) {
        Vet vet = new Vet();
        BeanUtils.copyProperties(dto, vet);
        return vet;
    }

//    public static TeacherDTO toDTO(Teacher Teacher) {
//        TeacherDTO dto = new TeacherDTO();
//        BeanUtils.copyProperties(Teacher, dto);
//        return dto;
//    }
//
//    public static Teacher toEntity(TeacherDTO dto) {
//        Teacher teacher = new Teacher();
//        BeanUtils.copyProperties(dto, teacher);
//        return teacher;
//    }

    public static String generateTeacherIdString() {
        return UUID.randomUUID().toString();
    }

}
