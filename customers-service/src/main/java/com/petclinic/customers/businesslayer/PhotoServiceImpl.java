package com.petclinic.customers.businesslayer;

import com.petclinic.customers.customerExceptions.exceptions.InvalidInputException;
import com.petclinic.customers.customerExceptions.exceptions.NotFoundException;
import com.petclinic.customers.datalayer.Photo;
import com.petclinic.customers.datalayer.PhotoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PhotoServiceImpl implements PhotoService{

    private static final Logger LOG = LoggerFactory.getLogger(PhotoServiceImpl.class);
    private final PhotoRepository repository;

    public PhotoServiceImpl(PhotoRepository repository) {
        this.repository = repository;
    }


    @Override
    public Photo uploadPhoto(Photo photo) {
        try{
            LOG.debug("uploadPhoto: photo with id {} saved",photo.getPhotoId());
            return repository.save(photo);
        }
        catch(DuplicateKeyException duplicateKeyException){
            throw new InvalidInputException("Duplicate key, photoId: " + photo.getPhotoId());
        }
    }

    @Override
    public Optional<Photo> findPhotoById(Integer photoId) {
        try {
            Optional<Photo> photo = repository.findPhotoById(photoId);
            LOG.debug("Photo with ID: " + photoId + " has been found");
            return photo;
        } catch (Exception e) {
            throw new NotFoundException("Photo with ID: " + photoId + " is not found!");
        }
    }
}
