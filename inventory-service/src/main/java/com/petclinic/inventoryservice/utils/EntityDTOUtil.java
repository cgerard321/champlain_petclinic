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

// Keep the original English fields for clients that do not read bilingual fields yet.
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
                .supplierName(product.getSupplierName())
                .productDescription(product.getProductDescription())
                .name(LocalizedContent.stored(product.getName(), product.getProductName()))
                .description(LocalizedContent.stored(product.getDescription(), product.getProductDescription()))
                .productPrice(product.getProductPrice())
                .productQuantity(product.getProductQuantity())
                .productSalePrice(product.getProductSalePrice())
                .productProfit(product.getProductProfit())
                .status(status)
                .lastUpdatedAt(product.getLastUpdatedAt() != null ? product.getLastUpdatedAt() : LocalDateTime.of(1900, 1, 1, 0, 0))
                .photoData(product.getPhotoData())
                .photoType(product.getPhotoType())
                .expirationDate(product.getExpirationDate())
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
        product.setName(LocalizedContent.complete(productRequestDTO.getName(), productRequestDTO.getProductName()));
        product.setDescription(LocalizedContent.complete(productRequestDTO.getDescription(), productRequestDTO.getProductDescription()));
        product.setProductName(product.getName().get("en"));
        product.setProductDescription(product.getDescription().get("en"));
        return product;
    }

    public static InventoryResponseDTO toInventoryResponseDTO(Inventory inventory){
        InventoryResponseDTO inventoryResponseDTO = new InventoryResponseDTO();
        BeanUtils.copyProperties(inventory, inventoryResponseDTO);
        inventoryResponseDTO.setName(LocalizedContent.stored(inventory.getName(), inventory.getInventoryName()));
        inventoryResponseDTO.setDescription(LocalizedContent.stored(inventory.getDescription(), inventory.getInventoryDescription()));
        List<ProductResponseDTO> productResponseDTOs = inventory.getProducts().stream()
                .map(EntityDTOUtil::toProductResponseDTO)
                .collect(Collectors.toList());
        inventoryResponseDTO.setProducts(productResponseDTOs);
        return inventoryResponseDTO;
    }

    public static Inventory toInventoryEntity(InventoryRequestDTO inventoryResponseDTO){
        Inventory inventory = new Inventory();
        BeanUtils.copyProperties(inventoryResponseDTO, inventory);
        inventory.setName(LocalizedContent.complete(inventoryResponseDTO.getName(), inventoryResponseDTO.getInventoryName()));
        inventory.setDescription(LocalizedContent.complete(inventoryResponseDTO.getDescription(), inventoryResponseDTO.getInventoryDescription()));
        inventory.setInventoryName(inventory.getName().get("en"));
        inventory.setInventoryDescription(inventory.getDescription().get("en"));
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
