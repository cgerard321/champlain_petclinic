package com.petclinic.inventoryservice.utils;

import com.petclinic.inventoryservice.datalayer.Product.Product;
import com.petclinic.inventoryservice.datalayer.Supply.Status;
import com.petclinic.inventoryservice.presentationlayer.ProductResponseDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EntityDTOUtilTest {

    @Test
    public void toProductResponseDTO_ShouldReturnOutOfStock_WhenQuantityIsZero() {
        // Arrange
        Product product = new Product();
        product.setId("1");
        product.setProductId("P1");
        product.setInventoryId("Inv1");
        product.setProductName("Test Product");
        product.setProductDescription("Test Description");
        product.setProductPrice(100.00);
        product.setProductQuantity(0); // Quantity is 0
        product.setProductSalePrice(10.00);

        // Act
        ProductResponseDTO responseDTO = EntityDTOUtil.toProductResponseDTO(product);

        // Assert
        assertNotNull(responseDTO);
        assertEquals(Status.OUT_OF_STOCK, responseDTO.getStatus());
        assertEquals(0, responseDTO.getProductQuantity());
    }

    @Test
    public void toProductResponseDTO_ShouldReturnReOrder_WhenQuantityIsLessThan20() {
        // Arrange
        Product product = new Product();
        product.setId("2");
        product.setProductId("P2");
        product.setInventoryId("Inv2");
        product.setProductName("Test Product");
        product.setProductDescription("Test Description");
        product.setProductPrice(50.00);
        product.setProductQuantity(10); // Quantity is less than 20
        product.setProductSalePrice(5.00);

        // Act
        ProductResponseDTO responseDTO = EntityDTOUtil.toProductResponseDTO(product);

        // Assert
        assertNotNull(responseDTO);
        assertEquals(Status.RE_ORDER, responseDTO.getStatus());
        assertEquals(10, responseDTO.getProductQuantity());
    }

    @Test
    public void toProductResponseDTO_ShouldReturnAvailable_WhenQuantityIs20OrMore() {
        // Arrange
        Product product = new Product();
        product.setId("3");
        product.setProductId("P3");
        product.setInventoryId("Inv3");
        product.setProductName("Test Product");
        product.setProductDescription("Test Description");
        product.setProductPrice(200.00);
        product.setProductQuantity(25); // Quantity is 20 or more
        product.setProductSalePrice(20.00);

        // Act
        ProductResponseDTO responseDTO = EntityDTOUtil.toProductResponseDTO(product);

        // Assert
        assertNotNull(responseDTO);
        assertEquals(Status.AVAILABLE, responseDTO.getStatus());
        assertEquals(25, responseDTO.getProductQuantity());
    }
}
