package com.petclinic.inventoryservice.utils.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InventoryNotFoundExceptionTest {

    @Test
    void constructor_shouldKeepMessage() {
        assertEquals("Inventory not found", new InventoryNotFoundException("Inventory not found").getMessage());
    }
}
