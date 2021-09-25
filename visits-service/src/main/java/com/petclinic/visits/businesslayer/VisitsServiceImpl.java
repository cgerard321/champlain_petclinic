package com.petclinic.visits.businesslayer;

import com.petclinic.visits.datalayer.Visit;
import com.petclinic.visits.datalayer.VisitRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class VisitsServiceImpl implements VisitsService {

    private final VisitRepository visitRepository;

    public VisitsServiceImpl(VisitRepository repo){
        this.visitRepository = repo;
    }

    @Override
    public Visit addVisit(Visit visit) {
        log.info("Calling visit repo to create a visit for pet with petId: {}", visit.getPetId());
        return visitRepository.save(visit);
    }

    @Override
    public List<Visit> getVisitsForPet(int petId) {
        log.info("Calling visit repo to get visits for pet with petId: {}", petId);
        return visitRepository.findByPetId(petId);
    }

    @Override
    public void deleteVisit(int visitId) {

    }

    @Override
    public List<Visit> getVisitsForPets(List<Integer> petIds) {
        return visitRepository.findByPetIdIn(petIds);
    }
}
