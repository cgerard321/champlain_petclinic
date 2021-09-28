package com.petclinic.visits.businesslayer;

import com.petclinic.visits.datalayer.Visit;

import java.util.List;

public interface VisitsService {


    public Visit addVisit(Visit visit);

    public List<Visit> getVisitsForPet(int petId);

    public void deleteVisit(int visitId);

    public Visit updateVisit(Visit visit);

    public List<Visit> getVisitsForPets(List<Integer> petIds);

}
