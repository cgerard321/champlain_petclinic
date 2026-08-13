package com.petclinic.cartsservice.domainclientlayer;
import lombok.Data;

@Data
public class CustomerResponseModel {
    private String customerId;
    private String firstName;
    private String lastName;

    public String getFullName() {
        String f = firstName == null ? "" : firstName.trim();
        String l = lastName  == null ? "" : lastName.trim();
        return (f + " " + l).trim();
    }
}
