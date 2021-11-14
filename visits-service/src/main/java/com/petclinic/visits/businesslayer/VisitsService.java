package com.petclinic.visits.businesslayer;

import com.petclinic.visits.datalayer.Visit;
import com.petclinic.visits.datalayer.VisitDTO;
import com.petclinic.visits.datalayer.VisitIdLessDTO;

import java.util.Date;
import java.util.List;

/*
 * This interface defines all the methods that our service must implement.
 *
 * Contributors:
 *   70963776+cjayneb@users.noreply.github.com
 */

public interface VisitsService {

    public VisitDTO addVisit(VisitIdLessDTO visit);

    public List<VisitDTO> getVisitsForPet(int petId);

    List<VisitDTO> getVisitsForPet(int petId, boolean scheduled);
  
    public VisitDTO getVisitByVisitId(String visitId);

    public void deleteVisit(String visitId);

    public VisitDTO updateVisit(VisitDTO visit);

    public List<VisitDTO> getVisitsForPets(List<Integer> petIds);

    List<VisitDTO> getVisitsForPractitioner(int practitionerId);

    List<VisitDTO> getVisitsByPractitionerIdAndMonth(int practitionerId, Date startDate, Date EndDate);

    boolean validateVisitId(String visitId);

}
