package com.petclinic.bffapigateway.dtos.Vets;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhotoDetails {
    private String id;

    private String filename;

    private String imgType;

    private byte[] data;
}
