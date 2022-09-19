package com.petclinic.customers.businesslayer;

import com.petclinic.customers.datalayer.Photo;

import java.util.Optional;

public interface PhotoService {

    Photo uploadPhoto(Photo photo);
    Optional<Photo> findPhotoById(Integer photoId);
}
