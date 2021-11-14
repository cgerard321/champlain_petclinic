package com.petclinic.visits.businesslayer;

import com.petclinic.visits.datalayer.Visit;
import com.petclinic.visits.datalayer.VisitDTO;
import com.petclinic.visits.datalayer.VisitIdLessDTO;
import com.petclinic.visits.datalayer.VisitRepository;
import com.petclinic.visits.utils.exceptions.InvalidInputException;
import com.petclinic.visits.utils.exceptions.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/*
 * This class implements the necessary methods to make our service work. It is currently responsible for the logic
 * of basic CRUD operations.
 *
 * Contributors:
 *   70963776+cjayneb@users.noreply.github.com
 */

@Service
@Slf4j
public class VisitsServiceImpl implements VisitsService {

    private final VisitRepository visitRepository;

    private final VisitMapper mapper;

    public VisitsServiceImpl(VisitRepository repo, VisitMapper mapper){
        this.visitRepository = repo;
        this.mapper = mapper;
    }

    @Override
    public VisitDTO addVisit(VisitIdLessDTO visit) {

        if(visit.getDescription() == null || visit.getDescription().isEmpty()){
            throw new InvalidInputException("Visit description required.");
        }

        try{
            Visit visitEntity = mapper.VisitIdLessDtoToEntity(visit);
            log.info("Calling visit repo to create a visit for pet with petId: {}", visit.getPetId());
            Visit createdEntity = visitRepository.save(visitEntity);
            return mapper.entityToModel(createdEntity);
        }
        catch(DuplicateKeyException dke){
            throw new InvalidInputException("Duplicate visitId.", dke);
        }
    }

    @Override
    public List<VisitDTO> getVisitsForPet(int petId) {

        if(petId < 0)
            throw new InvalidInputException("PetId can't be negative.");

        log.info("Calling visit repo to get visits for pet with petId: {}", petId);
        List<Visit> returnedVisits = visitRepository.findByPetId(petId);
        List<VisitDTO> visitDTOList = returnedVisits.stream()
                .filter(v -> v != null)
                .map(visit -> mapper.entityToModel(visit))
                .collect(Collectors.toList());
        return visitDTOList;
    }

    @Override
    public VisitDTO getVisitByVisitId(String visitId) {
        if (!validateVisitId(visitId))
            throw new InvalidInputException("VisitId not in the right format.");

        Optional<Visit> returnedVisit = visitRepository.findByVisitId(UUID.fromString(visitId));

        if(returnedVisit.get().getDescription() == null)
            throw new NotFoundException("Visit with visitId: " + visitId + " does not exist.");

        VisitDTO visitDTO = mapper.entityToModel(returnedVisit.get());

        return visitDTO;
    }
  
    @Override
    public List<VisitDTO> getVisitsForPet(int petId, boolean scheduled) {  

        Date now = new Date(System.currentTimeMillis());
        log.debug("Fetching the visits for pet with petId: {}", petId);
        List<VisitDTO> visitsForPet = getVisitsForPet(petId);

        if(scheduled){
            log.debug("Filtering out visits before {}", now);
            visitsForPet = visitsForPet.stream().filter(v -> v.getDate().after(now)).collect(Collectors.toList());
        }
        else{
            log.debug("Filtering out visits after {}", now);
            visitsForPet = visitsForPet.stream().filter(v -> v.getDate().before(now)).collect(Collectors.toList());
        }
        return visitsForPet;
    }

    @Override
    public void deleteVisit(String visitId) {
        log.debug("Visit object is deleted with this id: " + visitId);
        Visit visit = visitRepository.findByVisitId(UUID.fromString(visitId)).orElse(new Visit());
        if(visit.getVisitId() != null)
            visitRepository.delete(visit);

        log.debug("Visit deleted");
    }

    @Override
    public VisitDTO updateVisit(VisitDTO visit){
        Visit visitEntity = mapper.VisitDtoToEntity(visit);
        Optional<Visit> entity = visitRepository.findByVisitId(UUID.fromString(visit.getVisitId()));
        visitEntity.setId(entity.get().getId());
        log.info("Updating visit with petId: {} and visitId: {}", visit.getPetId(), visit.getVisitId());
        Visit updatedVisitEntity = visitRepository.save(visitEntity);
        return mapper.entityToModel(updatedVisitEntity);
    }

    @Override
    public List<VisitDTO> getVisitsForPets(List<Integer> petIds){
        List<Visit> returnedVisits = visitRepository.findByPetIdIn(petIds);
        List<VisitDTO> visitDTOList = returnedVisits.stream()
                .filter(v -> v != null)
                .map(visit -> mapper.entityToModel(visit))
                .collect(Collectors.toList());
        return visitDTOList;
    }

    @Override
    public List<VisitDTO> getVisitsForPractitioner(int practitionerId) {
        if(practitionerId < 0)
            throw new InvalidInputException("PractitionerId can't be negative.");
        List<Visit> returnedVisits = visitRepository.findVisitsByPractitionerId(practitionerId);
        List<VisitDTO> visitDTOList = returnedVisits.stream()
                .filter(v -> v != null)
                .map(visit -> mapper.entityToModel(visit))
                .collect(Collectors.toList());
        return visitDTOList;
    }

    @Override
    public List<VisitDTO> getVisitsByPractitionerIdAndMonth(int practitionerId, Date startDate, Date endDate) {
        List<Visit> returnedVisits = visitRepository.findAllByDateBetween(startDate, endDate);

        if(practitionerId < 0)
            throw new InvalidInputException("PractitionerId can't be negative.");

        returnedVisits = returnedVisits.stream().filter(v -> v.getPractitionerId() == practitionerId).collect(Collectors.toList());

        List<VisitDTO> visitDTOList = returnedVisits.stream()
                .filter(v -> v != null)
                .map(visit -> mapper.entityToModel(visit))
                .collect(Collectors.toList());

        return visitDTOList;
    }

    @Override
    public boolean validateVisitId(String visitId){
        String uuidRegex = "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}";
        if(visitId.matches(uuidRegex))
            return true;
        return false;
    }
}
