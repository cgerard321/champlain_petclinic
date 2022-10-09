package com.petclinic.customers.businesslayer;

import com.petclinic.customers.datalayer.Photo;

public interface PhotoService {
    public String setOwnerPhoto(Photo photo, int ownerId);
    public String setPetPhoto(Photo photo, int petId);
    public Photo getOwnerPhoto(int ownerId);
    public Photo getPetPhoto(int petId);
    public void deletePhoto(int photoId);
}
