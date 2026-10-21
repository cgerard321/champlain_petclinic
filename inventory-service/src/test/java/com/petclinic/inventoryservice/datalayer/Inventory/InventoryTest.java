package com.petclinic.inventoryservice.datalayer.Inventory;

import com.petclinic.inventoryservice.datalayer.Product.Product;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InventoryTest {

    @Test
    void addProduct_whenProductListIsNull_shouldCreateList() {
        Inventory inventory = new Inventory();
        inventory.setProducts(null);

        inventory.addProduct(new Product());

        assertEquals(1, inventory.getProducts().size());
    }
}
