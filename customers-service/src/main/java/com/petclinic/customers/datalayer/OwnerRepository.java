package com.petclinic.customers.datalayer;

import org.mapstruct.Qualifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository class for <code>Owner</code> domain objects All method names are compliant with Spring Data naming
 * conventions so this interface can easily be extended for Spring Data See here: http://static.springsource.org/spring-data/jpa/docs/current/reference/html/jpa.repositories.html#jpa.query-methods.query-creation
 * Copied from https://github.com/spring-petclinic/spring-petclinic-microservices
 *
 * @author Ken Krebs
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @author Michael Isvy
 * @author Maciej Szarlinski
 */

@Repository
public interface OwnerRepository extends JpaRepository<Owner, Integer> {

    /*
    @Transactional(readOnly = true)
    Optional<Owner> findByOwnerId (int id);

    Optional<Owner> findAccount(String un, String pass);

    Optional<Owner> findByName(String n);
    */

}
