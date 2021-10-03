package com.petclinic.visits.businesslayer;

import com.petclinic.visits.datalayer.Visit;
import com.petclinic.visits.datalayer.VisitRepository;
import com.petclinic.visits.utils.exceptions.InvalidInputException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import java.util.List;

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

    public VisitsServiceImpl(VisitRepository repo){
        this.visitRepository = repo;
    }

    @Override
    public Visit addVisit(Visit visit) {

        if(visit.getDescription().isEmpty()){
            throw new InvalidInputException("Visit description required.");
        }

        try{
            log.info("Calling visit repo to create a visit for pet with petId: {}", visit.getPetId());
            Visit v = visitRepository.save(visit);
            return v;
        }
        catch(DuplicateKeyException dke){
            throw new InvalidInputException("Duplicate visitId: " + visit.getId(), dke);
        }


    }

    @Override
    public List<Visit> getVisitsForPet(int petId) {
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
}
