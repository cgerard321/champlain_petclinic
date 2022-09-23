package com.petclinic.visits.visitsservicenew.Utils;


import com.petclinic.visits.visitsservicenew.DataLayer.Visit;
import com.petclinic.visits.visitsservicenew.DataLayer.VisitDTO;
import com.petclinic.visits.visitsservicenew.DataLayer.VisitIdLessDTO;
import org.springframework.beans.BeanUtils;

import java.util.UUID;

public class EntityDtoUtil {

    public static VisitDTO toDTO(Visit visit){
        VisitDTO dto = new VisitDTO();
        BeanUtils.copyProperties(visit, dto);
        return dto;
    }

    public static VisitIdLessDTO toIdLessDTO(Visit visit){
        VisitIdLessDTO visitIdLessDTO = new VisitIdLessDTO();
        BeanUtils.copyProperties(visit, visitIdLessDTO);
        return visitIdLessDTO;
    }

    public static Visit toEntity(VisitDTO visitDTO){
        Visit visit = new Visit();
        BeanUtils.copyProperties(visitDTO, visit);
        return visit;
    }

    public static Visit IdlesstoEntity(VisitIdLessDTO visitIdLessDTO){
        Visit visit = new Visit();
        BeanUtils.copyProperties(visitIdLessDTO, visit);
        return visit;
    }

    public static String generateSecIdString(){
        return UUID.randomUUID().toString();
    }

}
