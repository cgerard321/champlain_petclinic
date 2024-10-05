package com.petclinic.vet.dataaccesslayer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.List;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Table("albums")
public class Album {

    @Id
    private Integer id;

    @Column("vet_id")
    private String vetId;
    @Column("filename")
    private String filename;
    @Column("img_type")
    private String imgType;
    //@Lob
    @Column("img_data")
    private byte[] data;

}
