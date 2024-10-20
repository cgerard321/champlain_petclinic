package com.petclinic.inventoryservice.utils;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

class ImageUtilTest {

    @Test
    void readImage_ShouldReturnByteArray_WhenInputStreamIsValid() throws IOException {

        String imageContent = "test image content";
        InputStream inputStream = new ByteArrayInputStream(imageContent.getBytes());


        byte[] result = ImageUtil.readImage(inputStream);


        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(imageContent.getBytes());
    }

    @Test
    void readImage_ShouldThrowFileNotFoundException_WhenInputStreamIsNull() {

        assertThatThrownBy(() -> ImageUtil.readImage(null))
                .isInstanceOf(FileNotFoundException.class)
                .hasMessage("Image file not found");
    }

    @Test
    void readImage_ShouldThrowIOException_WhenInputStreamThrowsIOException() {

        InputStream faultyInputStream = new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("Faulty stream");
            }
        };


        assertThatThrownBy(() -> ImageUtil.readImage(faultyInputStream))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("Faulty stream");
    }

    @Test
    void readImage_ShouldHandleEmptyStream() throws IOException {

        InputStream emptyStream = new ByteArrayInputStream(new byte[0]);

        byte[] result = ImageUtil.readImage(emptyStream);

        assertThat(result).isNotNull();
        assertThat(result.length).isEqualTo(0);
    }
}
