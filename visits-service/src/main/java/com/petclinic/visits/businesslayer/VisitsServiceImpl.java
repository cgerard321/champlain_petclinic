package com.petclinic.visits.businesslayer;

import com.petclinic.visits.datalayer.Visit;
import com.petclinic.visits.datalayer.VisitRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


public class VisitsServiceImpl implements VisitsService {

    private final VisitRepository visitRepository;

    public VisitsServiceImpl(VisitRepository repo){
        this.visitRepository = repo;
    }

    @Override
    public List<Visit> getVisitsForPet(int petId){
        return visitRepository.findByPetId(petId);
    }

    @Override
    public List<Visit> getVisitsForPets(List<Integer> petIds){
        return visitRepository.findByPetIdIn(petIds);
    }

    @Override
    public void deleteVisit(int visitId) {
        visitRepository.findById(visitId).ifPresent(e -> visitRepository.delete(e));
    }
}
