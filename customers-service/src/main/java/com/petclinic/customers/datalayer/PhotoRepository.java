package com.petclinic.customers.datalayer;

import org.springframework.data.jpa.repository.JpaRepository;


public interface PhotoRepository extends JpaRepository<Photo, String> {
    Photo findPhotoById(int id);
    Photo findPhotoByName(String name);

}
