package com.petclinic.visits.businesslayer;

import com.petclinic.visits.datalayer.Visit;

import java.util.Date;
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

<<<<<<< HEAD
    List<Visit> getVisitsByPractitionerIdAndMonth(int practitionerId, Date startDate, Date EndDate);
=======
    List<String> getVisitDatesForPractitioner(int practitionerId);
>>>>>>> 5159b3a4 (Added methods in each layer for getting list of string dates for a practitioner (#158))
}
