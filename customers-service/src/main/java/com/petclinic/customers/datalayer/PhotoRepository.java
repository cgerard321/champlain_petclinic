package com.petclinic.customers.datalayer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PhotoRepository extends JpaRepository<Photo, String> {
    public Photo findPhotoById(int id);

}
