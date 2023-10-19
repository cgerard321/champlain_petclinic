package com.petclinic.vet.dataaccesslayer.badges;

import lombok.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Table("badges")
public class Badge {
    @Id
    private Integer id;
    @Column("vet_id")
    private String vetId;
    @Column("badge_title")
    private BadgeTitle badgeTitle;
    @Column("badge_date")
    private String badgeDate;
    @Column("img_data")
    private byte[] data;
}
