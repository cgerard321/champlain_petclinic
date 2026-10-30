package com.petclinic.cartsservice.utils;

import com.petclinic.cartsservice.domainclientlayer.FileRequestDTO;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Set;

public final class PromoImage {
    public static final int MAX_BYTES = 1024 * 1024;
    private PromoImage() { }

    public static void validate(FileRequestDTO image) {
        if (image == null) return;
        if (image.getFileName() == null || image.getFileName().isBlank()) {
            throw invalid("Image file name is required.");
        }
        if (image.getFileType() == null || !Set.of("image/png", "image/jpeg", "image/jpg").contains(image.getFileType())) {
            throw invalid("Only JPEG, JPG, and PNG images are supported.");
        }
        byte[] data = image.getFileData();
        if (data == null || data.length == 0 || data.length > MAX_BYTES) {
            throw invalid("Promotion images must be between 1 byte and 1 MB.");
        }

        //Extra validation so that the image matches its file type.
        try (ImageInputStream input = ImageIO.createImageInputStream(new ByteArrayInputStream(data))) {
            var readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) throw invalid("The selected image is invalid.");
            var reader = readers.next();
            try {
                reader.setInput(input);
                String format = reader.getFormatName();
                if (!(image.getFileType().equals("image/png") ? format.equalsIgnoreCase("png") : format.equalsIgnoreCase("jpeg"))) {
                    throw invalid("Image content does not match its file type.");
                }
                if ((long) reader.getWidth(0) * reader.getHeight(0) > 16_000_000) {
                    throw invalid("Promotion images must contain at most 16 million pixels.");
                }
                reader.read(0);
            } finally {
                reader.dispose();
            }
        } catch (IOException | IllegalArgumentException e) {
            throw invalid("The selected image is invalid.");
        }
    }

    private static ResponseStatusException invalid(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
