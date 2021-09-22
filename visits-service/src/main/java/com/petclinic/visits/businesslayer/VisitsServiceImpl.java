package com.petclinic.visits.businesslayer;

import com.petclinic.visits.datalayer.Visit;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VisitsServiceImpl implements VisitsService {

    @Override
    public Visit addVisit(Visit visit) {
        return null;
    }

    @Override
    public List<Visit> getVisitsForPet(int petId) {
        return null;
    }

    @Override
    public void deleteVisit(int visitId) {

    }

    @Override
    public List<Visit> getVisitsForPets(List<Integer> petIds) {
        return null;
    }
}
