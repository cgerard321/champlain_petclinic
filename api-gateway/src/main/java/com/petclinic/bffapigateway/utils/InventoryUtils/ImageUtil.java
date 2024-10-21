package com.petclinic.bffapigateway.utils.InventoryUtils;

import org.apache.commons.io.IOUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class ImageUtil {

    public static byte[] readImage(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            throw new FileNotFoundException("Image file not found");
        }
        return IOUtils.toByteArray(inputStream);
    }


}
