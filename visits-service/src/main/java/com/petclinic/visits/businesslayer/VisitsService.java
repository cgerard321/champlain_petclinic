package com.petclinic.visits.businesslayer;

import com.petclinic.visits.datalayer.Visit;

import java.util.List;

/*
 * This interface defines all the methods that our service must implement.
 *
 * Contributors:
 *   70963776+cjayneb@users.noreply.github.com
 */

public interface VisitsService {

    public Visit addVisit(Visit visit);

    public List<Visit> getVisitsForPet(int petId);

    public void deleteVisit(int visitId);

    public Visit updateVisit(Visit visit);

    public List<Visit> getVisitsForPets(List<Integer> petIds);

    List<Visit> getVisitsForPet(int petId, boolean scheduled);
}
