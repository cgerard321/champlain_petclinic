package com.petclinic.visits.businesslayer;

import com.petclinic.visits.datalayer.Visit;
import com.petclinic.visits.datalayer.VisitDTO;
import com.petclinic.visits.datalayer.VisitIdLessDTO;
import com.petclinic.visits.datalayer.VisitRepository;
import com.petclinic.visits.utils.exceptions.InvalidInputException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;
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
    public List<Visit> getVisitsForPet(int petId) {

        if(petId < 0)
            throw new InvalidInputException("PetId can't be negative.");

        log.info("Calling visit repo to get visits for pet with petId: {}", petId);
        return visitRepository.findByPetId(petId);
    }

    @Override
    public void deleteVisit(int visitId) {
        log.debug("Visit object is deleted with this id: " + visitId);
        visitRepository.findById(visitId).ifPresent(e -> visitRepository.delete(e));
        log.debug("Visit deleted");
    }

    @Override
    public Visit updateVisit(Visit visit){
        Visit v = visitRepository.save(visit);
        log.info("Updating visit with petId: {} and id: {}", visit.getPetId(), visit.getId());
        return v;
    }

    @Override
    public List<Visit> getVisitsForPets(List<Integer> petIds){
        return visitRepository.findByPetIdIn(petIds);
    }

    @Override
    public List<Visit> getVisitsForPet(int petId, boolean scheduled) {
        Date now = new Date(System.currentTimeMillis());
        log.debug("Fetching the visits for pet with petId: {}", petId);
        List<Visit> visits = getVisitsForPet(petId);

        if(scheduled){
            log.debug("Filtering out visits before {}", now);
            visits = visits.stream().filter(v -> v.getDate().after(now)).collect(Collectors.toList());
        }
        else{
            log.debug("Filtering out visits after {}", now);
            visits = visits.stream().filter(v -> v.getDate().before(now)).collect(Collectors.toList());
        }
        return visits;
    }

    @Override
    public List<Visit> getVisitsForPractitioner(int practitionerId) {
        if(practitionerId < 0)
            throw new InvalidInputException("PractitionerId can't be negative.");
        List<Visit> visits = visitRepository.findVisitsByPractitionerId(practitionerId);
        return visits;
    }

    @Override
    public List<Visit> getVisitsByPractitionerIdAndMonth(int practitionerId, Date startDate, Date endDate) {
        List<Visit> visits = visitRepository.findAllByDateBetween(startDate, endDate);

        if(practitionerId < 0)
            throw new InvalidInputException("PractitionerId can't be negative.");

        visits = visits.stream().filter(v -> v.getPractitionerId() == practitionerId).collect(Collectors.toList());

        return visits;
    }
}
