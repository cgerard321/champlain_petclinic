package com.petclinic.customers.datalayer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PhotoRepository extends JpaRepository<Photo, String> {
    Photo findPhotoById(int id);
    Photo findPhotoByName(String name);
    Void deletePhotoById(int id);
    Void deletePhotoByName(String name);

}
