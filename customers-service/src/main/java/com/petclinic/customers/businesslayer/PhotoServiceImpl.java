package com.petclinic.customers.businesslayer;

import com.petclinic.customers.customerExceptions.exceptions.NotFoundException;
import com.petclinic.customers.datalayer.*;
import com.petclinic.customers.presentationlayer.PetRequest;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PhotoServiceImpl implements PhotoService{
    private final PhotoRepository photoRepository;
    private final OwnerRepository ownerRepository;
    private final PetRepository petRepository;

    public PhotoServiceImpl(PhotoRepository photoRepository, OwnerRepository ownerRepository, PetRepository petRepository) {
        this.petRepository = petRepository;
        this.ownerRepository = ownerRepository ;
        this.photoRepository = photoRepository;
    }

    @Override
    public String setPhotoOwner(Photo photo, int ownerId) {
        try {
        photoRepository.save(photo);
        Owner owner = ownerRepository.findOwnerById(ownerId);
        int deleteId = owner.getImageId();
        owner.setImageId(photoRepository.findPhotoByName(photo.getName()).getId());
        ownerRepository.save(owner);
        if(deleteId!=1){
            this.deletePhoto(deleteId);
        }
        return "Image uploaded successfully: " + photo.getName();
        }
        catch (Exception e)
        {
            throw new NotFoundException("Owner with ID : " + ownerId+ " is not found");
        }
    }

    @Override
    public String setPhotoPet(Photo photo, int petId) {
        try {
            photoRepository.save(photo);
            Pet pet = petRepository.findPetById(petId);
            int deleteId = pet.getImageId();
            pet.setImageId(photoRepository.findPhotoByName(photo.getName()).getId());
            petRepository.save(pet);
            if(deleteId!=1){
                this.deletePhoto(deleteId);
            }
            return "Image uploaded successfully: " + photo.getName();
        }
        catch (Exception e)
        {
            throw new NotFoundException("Pet with ID : " + petId+ " is not found");
        }
    }

    @Override
    public Photo getPhotoOwner(int ownerId){
        try {
            return photoRepository.findPhotoById(ownerRepository.findOwnerById(ownerId).getImageId());
        }
        catch (Exception e)
        {
            throw new NotFoundException("Owner with ID : " + ownerId+ " is not found");
        }
    }

    @Override
    public Photo getPhotoPet(int petId){
        try {
            return photoRepository.findPhotoById(petRepository.findPetById(petId).getImageId());
        }
        catch (Exception e)
        {
            throw new NotFoundException("Pet with ID : " + petId+ " is not found");
        }
    }

    @Override
    public void deletePhoto(int photoId) {

        try{
            Photo photo = photoRepository.findPhotoById(photoId);
            photoRepository.delete(photo);
        }
        catch (Exception ex)
        {
            throw new NotFoundException("Photo with ID : " + photoId+ " is not found");        }
    }

    //test
}
