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
@Table("images")
public class Album {

    @Id
    private Integer id;

    @Column("vet_id")
    private String vetId;

    @Column("title")
    private String title;

    private List<Photo> photos;
}
