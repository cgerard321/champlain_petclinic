package com.petclinic.bffapigateway.dtos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class PhotoDetails {

    private String id;

    private String name;

    private String type;

    private byte[] photo;

}
