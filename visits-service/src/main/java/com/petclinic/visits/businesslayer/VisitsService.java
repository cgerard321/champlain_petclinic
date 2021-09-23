package com.petclinic.visits.businesslayer;

import com.petclinic.visits.datalayer.Visit;

import java.util.List;

public interface VisitsService {

    public List<Visit> getVisitsForPet(int petId);

    public List<Visit> getVisitsForPets(List<Integer> petIds);

    public void deleteVisit(int visitId);
}
