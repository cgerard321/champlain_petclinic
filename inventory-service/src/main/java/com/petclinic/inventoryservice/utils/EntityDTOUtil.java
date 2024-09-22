package com.petclinic.inventoryservice.utils;

import com.petclinic.inventoryservice.datalayer.Inventory.Inventory;
import com.petclinic.inventoryservice.datalayer.Inventory.InventoryName;
import com.petclinic.inventoryservice.datalayer.Inventory.InventoryType;
import com.petclinic.inventoryservice.datalayer.Product.Product;
import com.petclinic.inventoryservice.datalayer.Supply.Status;
import com.petclinic.inventoryservice.datalayer.Supply.Supply;
import com.petclinic.inventoryservice.presentationlayer.*;
import org.springframework.beans.BeanUtils;

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

    public static SupplyResponseDTO toSupplyResponseDTO(Supply supply) {
        //should put the logic for the status here
        Status status;
        if (supply.getSupplyQuantity() == 0) {
            status = Status.OUT_OF_STOCK;
        } else if (supply.getSupplyQuantity() < 20) {
            status = Status.RE_ORDER;
        } else {
            status = Status.AVAILABLE;
        }

        return SupplyResponseDTO.builder()
                .supplyId(supply.getSupplyId())
                .inventoryId(supply.getInventoryId())
                .supplyName(supply.getSupplyName())
                .supplyDescription(supply.getSupplyDescription())
                .supplyPrice(supply.getSupplyPrice())
                .supplyQuantity(supply.getSupplyQuantity())
                .supplySalePrice(supply.getSupplySalePrice())
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

    public static Supply toSupplyEntity(SupplyRequestDTO supplyRequestDTO){
        Supply supply = new Supply();
        BeanUtils.copyProperties(supplyRequestDTO, supply);
        return supply;
    }

    public static InventoryResponseDTO toInventoryResponseDTO(Inventory inventory){
        InventoryResponseDTO inventoryResponseDTO = new InventoryResponseDTO();
        BeanUtils.copyProperties(inventory, inventoryResponseDTO);
        List<SupplyResponseDTO> supplyResponseDTOs = inventory.getSupplies().stream()
                .map(supply -> new SupplyResponseDTO(
                        supply.getSupplyId(),
                        supply.getInventoryId(),
                        supply.getSupplyName(),
                        supply.getSupplyDescription(),
                        supply.getSupplyPrice(),
                        supply.getSupplyQuantity(),
                        supply.getSupplySalePrice(),
                        supply.getStatus()
                ))
                .collect(Collectors.toList());
        inventoryResponseDTO.setSupplies(supplyResponseDTOs);
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

    public static InventoryName toInventoryNameEntity(InventoryNameRequestDTO inventoryNameRequestDTO){
        InventoryName inventoryName = new InventoryName();
        BeanUtils.copyProperties(inventoryNameRequestDTO, inventoryName);
        return inventoryName;
    }

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
