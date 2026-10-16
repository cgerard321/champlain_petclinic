package com.petclinic.inventoryservice;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

import static org.mockito.Mockito.mockStatic;

class InventoryServiceApplicationMainTest {

    @Test
    void main_shouldStartSpringApplication() {
        String[] args = {};
        try (MockedStatic<SpringApplication> springApplication = mockStatic(SpringApplication.class)) {
            InventoryServiceApplication.main(args);
            springApplication.verify(() -> SpringApplication.run(InventoryServiceApplication.class, args));
        }
    }
}
