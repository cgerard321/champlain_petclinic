package com.petclinic.vets.presentationlayer;

import com.petclinic.vets.VetsServiceApplication;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * This is a test for the main application running
 *
 * @author Christian
 */
public class VetsServiceApplicationTest {

    @Test
    @DisplayName("Main Vets Service Application Test")
    public void main() {
        VetsServiceApplication.main(new String[]{});
    }
}
