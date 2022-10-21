package com.petclinic.visits.visitsservicenew.Utils;


import com.petclinic.visits.visitsservicenew.DataLayer.Visit;
import com.petclinic.visits.visitsservicenew.DataLayer.VisitDTO;
import org.springframework.beans.BeanUtils;

import java.util.UUID;

public class EntityDtoUtil {

    public static VisitDTO toDTO(Visit visit){
        VisitDTO dto = new VisitDTO();
        BeanUtils.copyProperties(visit, dto);
        return dto;
    }

    public static Visit toEntity(VisitDTO visitDTO){
        Visit visit = new Visit();
        BeanUtils.copyProperties(visitDTO, visit);
        return visit;
    }

    public static String generateVisitIdString(){
        return UUID.randomUUID().toString();
    }

}
