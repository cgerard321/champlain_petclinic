package com.petclinic.vet.dataaccesslayer;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Table("images")
@Setter
public class Photo {
    @Id
    private Integer id;
    @Column("vet_id")
    private String vetId;
    @Column("filename")
    private String filename;
    @Column("img_type")
    private String imgType;
    //@Lob
    @Column("img_base64")
    private String imgBase64;
}
