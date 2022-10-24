//package com.petclinic.vet.servicelayer;
//
//import com.petclinic.vet.dataaccesslayer.Photo;
//import com.petclinic.vet.dataaccesslayer.PhotoRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import reactor.core.publisher.Mono;
//
//@Service
//public class PhotoServiceImpl implements PhotoService{
////    @Autowired
////    private PhotoRepository photoRepo;
////
////    @Override
////    public Mono<Photo> insertPhoto(Mono<Photo> photoMono) {
////        return photoMono
////                .flatMap(photoRepo::insert);
////    }
////    @Override
////    public Mono<Photo> getPhotoByPhotoId(int photoId) {
////        return photoRepo.findPhotoById(photoId);
////    }
////
////    @Override
////    public Mono<Photo> getPhotoByPhotoName (String photoName) { return photoRepo.findPhotoByName(photoName); }
////    @Override
////    public Mono<Photo> deletePhoto(int photoId) {
////        return null;
////    }
//}
//
