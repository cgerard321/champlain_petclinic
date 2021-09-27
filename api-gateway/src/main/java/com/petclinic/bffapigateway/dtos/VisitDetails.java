package com.petclinic.bffapigateway.dtos;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Maciej Szarlinski
 * Copied from https://github.com/spring-petclinic/spring-petclinic-microservices
 */

@Data
//@Builder(builderMethodName = "visitDetails")
@NoArgsConstructor
public class VisitDetails {

    private Integer id;

    private Integer petId;

    private String date;

    private String description;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getPetId() {
        return petId;
    }

    public void setPetId(Integer petId) {
        this.petId = petId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<VisitDetails> getItems() {
        return items;
    }

    public void setItems(List<VisitDetails> items) {
        this.items = items;
    }

    private List<VisitDetails> items = new ArrayList<>();

    public VisitDetails(Integer id, Integer petId, String date, String description) {
        this.id = id;
        this.petId = petId;
        this.date = date;
        this.description = description;

    }

}

