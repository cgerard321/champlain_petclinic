package com.petclinic.vet.utils;

import com.petclinic.vet.dataaccesslayer.albums.Album;
import com.petclinic.vet.dataaccesslayer.albums.AlbumRepository;
import com.petclinic.vet.dataaccesslayer.photos.Photo;
import com.petclinic.vet.dataaccesslayer.photos.PhotoRepository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ImageRefreshService implements CommandLineRunner {

    private static final String SEEDED_PHOTO_FILENAME = "vet_default.jpg";
    private static final String DEFAULT_PHOTO_PATH = "images/vet_default.jpg";

    private static final Map<String, String> SEEDED_PORTRAITS = seededPortraits();

    private final PhotoRepository photoRepository;
    private final AlbumRepository albumRepository;

    public ImageRefreshService(PhotoRepository photoRepository, AlbumRepository albumRepository) {
        this.photoRepository = photoRepository;
        this.albumRepository = albumRepository;
    }

    private static Map<String, String> seededPortraits() {
        Map<String, String> portraits = new LinkedHashMap<>();
        portraits.put("69f852ca-625b-11ee-8c99-0242ac120002", "james_carter.jpg");
        portraits.put("69f85766-625b-11ee-8c99-0242ac120002", "helen_leary.jpg");
        portraits.put("69f85bda-625b-11ee-8c99-0242ac120002", "linda_douglass.jpg");
        portraits.put("69f85d2e-625b-11ee-8c99-0242ac120002", "rafael_o.jpg");
        portraits.put("ac9adeb8-625b-11ee-8c99-0242ac120002", "henry_stevens.jpg");
        portraits.put("b2bc331e-625b-11ee-8c99-0242ac120002", "sharon_jenkins.jpg");
        portraits.put("c02cbf82-625b-11ee-8c99-0242ac120002", "johnn_doe.jpg");
        return portraits;
    }

    @Override
    public void run(String... args) {
        int refreshed;
        try {
            refreshed = refreshPortraits() + refreshAlbumPhotos();
        } catch (Exception e) {
            log.warn("Image refresh skipped: {}", e.getMessage());
            return;
        }

        if (refreshed == 0) {
            log.info("Vet images already match the packaged resources, nothing to refresh");
        } else {
            log.info("Refreshed {} vet image(s) from the packaged resources", refreshed);
        }
    }

    private int refreshPortraits() {
        byte[] packagedDefault = readPackaged(DEFAULT_PHOTO_PATH);
        int refreshed = 0;

        for (Map.Entry<String, String> portrait : SEEDED_PORTRAITS.entrySet()) {
            String vetId = portrait.getKey();
            byte[] packaged = readPackaged("images/" + portrait.getValue());
            if (packaged == null) {
                continue;
            }

            Photo stored = photoRepository.findByVetId(vetId).block();
            if (stored == null || !SEEDED_PHOTO_FILENAME.equals(stored.getFilename())) {
                continue;
            }
            if (Arrays.equals(stored.getData(), packaged)
                    || Arrays.equals(stored.getData(), packagedDefault)) {
                continue;
            }

            stored.setData(packaged);
            photoRepository.save(stored).block();
            log.info("Portrait refreshed for vet {} from {}", vetId, portrait.getValue());
            refreshed++;
        }

        return refreshed;
    }

    private int refreshAlbumPhotos() {
        List<Album> stored = albumRepository.findAll().collectList().block();
        if (stored == null || stored.isEmpty()) {
            return 0;
        }

        int refreshed = 0;
        for (Album album : stored) {
            String filename = album.getFilename();
            if (filename == null || filename.isBlank()) {
                continue;
            }

            byte[] packaged = readPackaged("album/" + filename);
            if (packaged == null || Arrays.equals(album.getData(), packaged)) {
                continue;
            }

            album.setData(packaged);
            albumRepository.save(album).block();
            log.info("Album photo refreshed: {}", filename);
            refreshed++;
        }

        return refreshed;
    }

    private byte[] readPackaged(String path) {
        try {
            ClassPathResource resource = new ClassPathResource(path);
            if (!resource.exists()) {
                return null;
            }
            return StreamUtils.copyToByteArray(resource.getInputStream());
        } catch (Exception e) {
            log.warn("Could not read packaged image {}: {}", path, e.getMessage());
            return null;
        }
    }
}
