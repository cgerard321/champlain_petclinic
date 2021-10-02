package com.petclinic.vets.datalayer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

public interface SpecialtyRepository extends JpaRepository<Specialty, Integer>{
}
@Repository
public interface VetRepository extends JpaRepository<Vet, Integer>
{
    Optional<Vet> findByVetId (int vetId);

    @Query(value = "SELECT v FROM Vet v WHERE v.isActive = 0")
    List<Vet> findAllDisabledVets();

    @Query(value = "SELECT v FROM Vet v WHERE v.isActive = 1")
    List<Vet> findAllEnabledVets();
}
