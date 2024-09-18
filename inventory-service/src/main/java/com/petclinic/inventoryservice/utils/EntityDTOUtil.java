package com.petclinic.inventoryservice.utils;

import com.petclinic.inventoryservice.datalayer.Inventory.Inventory;
import com.petclinic.inventoryservice.datalayer.Inventory.InventoryType;
import com.petclinic.inventoryservice.datalayer.Product.Product;
import com.petclinic.inventoryservice.datalayer.Product.Status;
import com.petclinic.inventoryservice.presentationlayer.*;
import org.springframework.beans.BeanUtils;

import java.util.Random;
import java.util.UUID;

public class EntityDTOUtil {

    public static ProductResponseDTO toProductResponseDTO(Product product) {
        //should put the logic for the status here
        Status status;
        if (product.getProductQuantity() == 0) {
            status = Status.OUT_OF_STOCK;
        } else if (product.getProductQuantity() < 20) {
            status = Status.RE_ORDER;
        } else {
            status = Status.AVAILABLE;
        }

        return ProductResponseDTO.builder()
                .id(product.getId())
                .productId(product.getProductId())
                .inventoryId(product.getInventoryId())
                .productName(product.getProductName())
                .productDescription(product.getProductDescription())
                .productPrice(product.getProductPrice())
                .productQuantity(product.getProductQuantity())
                .productSalePrice(product.getProductSalePrice())
                .status(status)
                .build();
    }
//    public static ProductResponseDTO toProductResponseDTO(Product product){
//        ProductResponseDTO productResponseDTO = new ProductResponseDTO();
//        BeanUtils.copyProperties(product, productResponseDTO);
//        return productResponseDTO;
//    }
    public static Product toProductEntity(ProductRequestDTO productRequestDTO){
        Product product = new Product();
        BeanUtils.copyProperties(productRequestDTO, product);
        return product;
    }
    public static InventoryResponseDTO toInventoryResponseDTO(Inventory inventory){
        InventoryResponseDTO inventoryResponseDTO = new InventoryResponseDTO();
        BeanUtils.copyProperties(inventory, inventoryResponseDTO);
        return inventoryResponseDTO;
    }
    public static Inventory toInventoryEntity(InventoryRequestDTO inventoryResponseDTO){
        Inventory inventory = new Inventory();
        BeanUtils.copyProperties(inventoryResponseDTO, inventory);
        return inventory;
    }
    public static InventoryType toInventoryTypeEntity(InventoryTypeRequestDTO inventoryTypeRequestDTO){
        InventoryType inventoryType = new InventoryType();
        BeanUtils.copyProperties(inventoryTypeRequestDTO, inventoryType);
        return inventoryType;
    }
    public static InventoryTypeResponseDTO toInventoryTypeResponseDTO(InventoryType inventoryType){
        InventoryTypeResponseDTO inventoryTypeResponseDTO = new InventoryTypeResponseDTO();
        BeanUtils.copyProperties(inventoryType, inventoryTypeResponseDTO);
        return inventoryTypeResponseDTO;
    }

    public static String generateUUID(){
        return UUID.randomUUID().toString();
    }
}
