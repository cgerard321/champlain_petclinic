package com.petclinic.visits.datalayer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Repository class for <code>Visit</code> domain objects All method names are compliant with Spring Data naming conventions so this interface can easily be extended for Spring
 * Data See here: http://static.springsource.org/spring-data/jpa/docs/current/reference/html/jpa.repositories.html#jpa.query-methods.query-creation
 *
 * @author Ken Krebs
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @author Michael Isvy
 * @author Maciej Szarlinski
 * Copied from https://github.com/spring-petclinic/spring-petclinic-microservices
 */

public interface VisitRepository extends JpaRepository<Visit, Integer> {

    Optional<Visit> findById(int visitId);

    List<Visit> findByPetId(int petId);

    List<Visit> findByPetIdIn(Collection<Integer> petIds);

<<<<<<< HEAD
<<<<<<< HEAD
    List<Visit> findAllByDateBetween(Date startingDate, Date EndDate);

    //Optional<Visit> findById(int visitId);

=======
>>>>>>> bcfb440f (feat/(VISE-CPC-213) Make UI for delete visit (#143))
=======
    List<Visit> findVisitsByPractitionerId(int practitionerId);

>>>>>>> 5159b3a4 (Added methods in each layer for getting list of string dates for a practitioner (#158))
    void findVisitById(int visitId);

}
