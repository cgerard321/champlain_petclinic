package com.petclinic.customers.datalayer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

import static org.springframework.http.HttpHeaders.FROM;

public interface PhotoRepository extends JpaRepository<Photo, Integer> {

    @Query("FROM Photo cPhoto WHERE cPhoto.photoId = :photoId")
    Optional<Photo> findPhotoById(@Param("photoId") int photoId);
}
