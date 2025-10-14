package com.petclinic.inventoryservice.datalayer.Inventory;

import com.petclinic.inventoryservice.datalayer.Product.Product;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
@Document(collection = "inventories")
public class Inventory {

    @Id
    private String id;
    private String inventoryId;
    private String inventoryCode;
    private String inventoryName;
    private String inventoryType;
    private String inventoryDescription;
    private String inventoryImage;
    private String inventoryBackupImage;

    private byte[] imageUploaded;
    private Boolean important;


    @Builder.Default
    private List<Product> products = new ArrayList<>();


    public void addProduct(Product product) {
        if (this.products == null) {
            this.products = new ArrayList<>();
        }
        this.products.add(product);
    }
}