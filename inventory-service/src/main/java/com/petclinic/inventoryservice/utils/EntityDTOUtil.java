package com.petclinic.inventoryservice.utils;

import com.petclinic.inventoryservice.datalayer.Inventory.Inventory;
import com.petclinic.inventoryservice.datalayer.Inventory.InventoryName;
import com.petclinic.inventoryservice.datalayer.Inventory.InventoryType;
import com.petclinic.inventoryservice.datalayer.Product.Product;
import com.petclinic.inventoryservice.datalayer.Product.Status;
import com.petclinic.inventoryservice.presentationlayer.*;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
                .productId(product.getProductId())
                .inventoryId(product.getInventoryId())
                .productName(product.getProductName())
                .productDescription(product.getProductDescription())
                .productPrice(product.getProductPrice())
                .productQuantity(product.getProductQuantity())
                .productSalePrice(product.getProductSalePrice())
                .status(status)
                .lastUpdatedAt(product.getLastUpdatedAt() != null ? product.getLastUpdatedAt() : LocalDateTime.of(1900, 1, 1, 0, 0))
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
        List<ProductResponseDTO> productResponseDTOs = inventory.getProducts().stream()
                .map(product -> new ProductResponseDTO(
                        product.getProductId(),
                        product.getInventoryId(),
                        product.getProductName(),
                        product.getProductDescription(),
                        product.getProductPrice(),
                        product.getProductQuantity(),
                        product.getProductSalePrice(),
                        product.getProductProfit(),
                        product.getStatus(),
                        product.getLastUpdatedAt()
                ))
                .collect(Collectors.toList());
        inventoryResponseDTO.setProducts(productResponseDTOs);
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

//    public static InventoryName toInventoryNameEntity(InventoryNameRequestDTO inventoryNameRequestDTO){
//        InventoryName inventoryName = new InventoryName();
//        BeanUtils.copyProperties(inventoryNameRequestDTO, inventoryName);
//        return inventoryName;
//    }

    public static InventoryTypeResponseDTO toInventoryTypeResponseDTO(InventoryType inventoryType){
        InventoryTypeResponseDTO inventoryTypeResponseDTO = new InventoryTypeResponseDTO();
        BeanUtils.copyProperties(inventoryType, inventoryTypeResponseDTO);
        return inventoryTypeResponseDTO;
    }

    public static InventoryNameResponseDTO toInventoryNameResponseDTO(InventoryName inventoryName){
        InventoryNameResponseDTO inventoryNameResponseDTO = new InventoryNameResponseDTO();
        BeanUtils.copyProperties(inventoryName, inventoryNameResponseDTO);
        return inventoryNameResponseDTO;
    }

    public static String generateUUID(){
        return UUID.randomUUID().toString();
    }
}
