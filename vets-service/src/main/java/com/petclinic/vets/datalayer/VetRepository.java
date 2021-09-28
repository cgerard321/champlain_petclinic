package com.petclinic.vets.datalayer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Repository class for <code>Vet</code> domain objects All method names are compliant with Spring Data naming
 * conventions so this interface can easily be extended for Spring Data See here: http://static.springsource.org/spring-data/jpa/docs/current/reference/html/jpa.repositories.html#jpa.query-methods.query-creation
 *
 * @author Ken Krebs
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @author Michael Isvy
 * @author Maciej Szarlinski
 * Copied from https://github.com/spring-petclinic/spring-petclinic-microservices
 */
@Repository
public interface VetRepository extends JpaRepository<Vet, Integer>
{
    Optional<Vet> findByVetId (int vetId);

    @Query(value = "SELECT v FROM Vet v WHERE v.isActive = 0")
    List<Vet> findAllDisabledVets();

    @Query(value = "SELECT v FROM Vet v WHERE v.isActive = 1")
    List<Vet> findAllEnabledVets();
}

