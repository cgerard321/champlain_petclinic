package com.petclinic.customers.businesslayer;

import com.petclinic.customers.datalayer.Photo;
import com.petclinic.customers.datalayer.PhotoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.util.stream.Stream;

import java.io.IOException;

@Service
public class PhotoServiceImpl {

    @Autowired
    private PhotoRepository photoRepository;

//    public Photo store(MultipartFile photo) throws IOException {
//        String photoName = StringUtils.cleanPath(photo.getOriginalFilename());
//        Photo Photo = new Photo(photoName, photo.getContentType(), photo.getBytes());
//
//        return photoRepository.save(Photo);
//    }

//    public Photo getPhoto(String id) {
//        return photoRepository.findById(id).get();
//    }
}
