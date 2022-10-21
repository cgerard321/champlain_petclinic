package com.petclinic.vet.dataaccesslayer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class Photo {
    @Id
    private String id;
    private String photoName;
    private String type;
    private String photo;

}
