package com.petclinic.customers.businesslayer;

import com.petclinic.customers.datalayer.Photo;

public interface PhotoService {
    public String setPhotoOwner(Photo photo, int ownerId);
    public String setPhotoPet(Photo photo, int petId);
    public Photo getPhotoOwner(int ownerId);
    public Photo getPhotoPet(int petId);
    public void deletePhoto(int photoId);
}
