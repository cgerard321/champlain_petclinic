package com.petclinic.bffapigateway.utils.InventoryUtils;

import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;

import static org.junit.jupiter.api.Assertions.*;

class ImageUtilTest {

    @Test
    void readImage_withNullStream_shouldThrowFileNotFound() {
        assertNotNull(new ImageUtil());
        assertThrows(FileNotFoundException.class, () -> ImageUtil.readImage(null));
    }
}
