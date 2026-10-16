package com.petclinic.bffapigateway.dtos.Inventory;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InventoryTypeTest {

    @Test
    void inventoryType_shouldHaveInternalAndSales() {
        assertEquals(2, InventoryType.values().length);
        assertEquals(InventoryType.sales, InventoryType.valueOf("sales"));
    }
}
