package com.petclinic.visits.businesslayer;

import com.petclinic.visits.datalayer.Visit;
import com.petclinic.visits.datalayer.Visit.VisitBuilder;
import com.petclinic.visits.datalayer.VisitDTO;
import com.petclinic.visits.datalayer.VisitIdLessDTO;
import java.util.UUID;
import javax.annotation.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2021-10-24T12:08:39-0400",
    comments = "version: 1.4.1.Final, compiler: javac, environment: Java 13 (Oracle Corporation)"
)
@Component
public class VisitMapperImpl implements VisitMapper {

    @Override
    public Visit VisitDtoToEntity(VisitDTO model) {
        if ( model == null ) {
            return null;
        }

        VisitBuilder visit = Visit.visit();

        visit.date( model.getDate() );
        visit.description( model.getDescription() );
        visit.petId( model.getPetId() );
        visit.practitionerId( model.getPractitionerId() );
        visit.status( model.isStatus() );

        visit.visitId( UUID.fromString(model.getVisitId()) );

        return visit.build();
    }

    @Override
    public VisitDTO entityToModel(Visit entity) {
        if ( entity == null ) {
            return null;
        }

        VisitDTO visitDTO = new VisitDTO();

        visitDTO.setDate( entity.getDate() );
        visitDTO.setDescription( entity.getDescription() );
        visitDTO.setPetId( entity.getPetId() );
        visitDTO.setPractitionerId( entity.getPractitionerId() );
        visitDTO.setStatus( entity.isStatus() );

        visitDTO.setVisitId( entity.getVisitId().toString() );

        return visitDTO;
    }

    @Override
    public Visit VisitIdLessDtoToEntity(VisitIdLessDTO visit) {
        if ( visit == null ) {
            return null;
        }

        VisitBuilder visit1 = Visit.visit();

        visit1.date( visit.getDate() );
        visit1.description( visit.getDescription() );
        visit1.petId( visit.getPetId() );
        visit1.practitionerId( visit.getPractitionerId() );
        visit1.status( visit.isStatus() );

        return visit1.build();
    }
}
