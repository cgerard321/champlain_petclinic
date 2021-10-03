package com.petclinic.vets.presentationlayer;

import com.petclinic.vets.VetsServiceApplication;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class VetsServiceApplicationTest {

    @Test
    @DisplayName("Main Vets Service Application Test")
    public void main(){
        VetsServiceApplication.main(new String[] {});
    }
}
