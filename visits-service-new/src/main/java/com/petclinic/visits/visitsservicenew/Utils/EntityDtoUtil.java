package com.petclinic.visits.visitsservicenew.Utils;


import com.petclinic.visits.visitsservicenew.DataLayer.Visit;
import com.petclinic.visits.visitsservicenew.PresentationLayer.VisitRequestDTO;
import com.petclinic.visits.visitsservicenew.PresentationLayer.VisitResponseDTO;
import org.springframework.beans.BeanUtils;

import java.util.UUID;


public class EntityDtoUtil {

    public static VisitResponseDTO toVisitResponseDTO(Visit visit) {
        VisitResponseDTO visitResponseDTO = new VisitResponseDTO();
        BeanUtils.copyProperties(visit, visitResponseDTO);
        return visitResponseDTO;
    }

    public static Visit toVisitEntity(VisitRequestDTO visitRequestDTO){
        Visit visit = new Visit();
        BeanUtils.copyProperties(visitRequestDTO, visit);
        return visit;
    }

    public static String generateVisitIdString(){
        return UUID.randomUUID().toString();
    }

}
