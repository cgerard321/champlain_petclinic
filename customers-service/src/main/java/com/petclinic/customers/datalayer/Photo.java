package com.petclinic.customers.datalayer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Arrays;

@Entity
@Table(name = "photos")
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Photo {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "name")
    private String name;

    @Column(name = "type")
    private String type;

    @Column(name = "image", unique = false, nullable = false, length = 100000)
    private byte[] photo;

    @Override
    public String toString()
    {
        return "ID: " +
                this.id + ", Name: " +
                this.name + ", Type: " +
                this.type + ", Image: " +
                Arrays.toString(this.photo);
    }
}
