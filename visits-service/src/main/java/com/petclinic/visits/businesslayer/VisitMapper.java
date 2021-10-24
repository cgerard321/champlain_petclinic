package com.petclinic.visits.businesslayer;

import com.petclinic.visits.datalayer.Visit;
import com.petclinic.visits.datalayer.VisitDTO;
import com.petclinic.visits.datalayer.VisitIdLessDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.UUID;

@Mapper(componentModel = "spring", imports = {UUID.class})
public interface VisitMapper {
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "visitId", expression = "java(UUID.fromString(model.getVisitId()))")
    })
    Visit VisitDtoToEntity(VisitDTO model);

    @Mapping(target = "visitId", expression = "java(entity.getVisitId().toString())")
    VisitDTO entityToModel(Visit entity);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "visitId", ignore = true)
    })
    Visit VisitIdLessDtoToEntity(VisitIdLessDTO visit);
}
