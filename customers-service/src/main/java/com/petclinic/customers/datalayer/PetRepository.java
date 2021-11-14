package com.petclinic.customers.datalayer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repository class for <code>Pet</code> domain objects All method names are compliant with Spring Data naming
 * conventions so this interface can easily be extended for Spring Data See here: http://static.springsource.org/spring-data/jpa/docs/current/reference/html/jpa.repositories.html#jpa.query-methods.query-creation
 * Copied from https://github.com/spring-petclinic/spring-petclinic-microservices
 *
 * @author Ken Krebs
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @author Michael Isvy
 * @author Maciej Szarlinski
 * @author lpsim
 */

public interface PetRepository extends JpaRepository<Pet, Integer> {

    /**
     * Retrieve all {@link PetType}s from the data store.
     * @return a Collection of {@link PetType}s.
     */

    @Query("FROM Pet cPet WHERE cPet.owner = :owner AND cPet.id = :petId")
    Optional<Pet> findPetByOwner(@Param("owner") Owner owner, @Param("petId") int petId);

    @Query("FROM Pet cPet WHERE cPet.owner = :owner")
    List<Pet> findAllPetByOwner(@Param("owner") Owner owner);

    @Query("SELECT ptype FROM PetType ptype ORDER BY ptype.name")
    List<PetType> findPetTypes();

    @Query("FROM PetType ptype WHERE ptype.id = :typeId")
    Optional<PetType> findPetTypeById(@Param("typeId") int typeId);


}