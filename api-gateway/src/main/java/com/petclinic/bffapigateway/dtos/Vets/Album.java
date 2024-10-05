package com.petclinic.bffapigateway.dtos.Vets;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class Album {

    private Integer id;
    private String vetId;
    private String filename;
    private String imgType;
    private byte[] data;
    //private List<Photo> photos;

    @Override
    public String toString() {
        return "Album{" +
                "id=" + id +
                ", vetId='" + vetId + '\'' +
                ", filename='" + filename + '\'' +
                ", imgType='" + imgType + '\'' +
                ", dataSize=" + (data != null ? data.length : 0) + " bytes" +
                '}';
    }
}
