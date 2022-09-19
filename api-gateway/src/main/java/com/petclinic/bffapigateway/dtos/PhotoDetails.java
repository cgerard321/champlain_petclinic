package com.petclinic.bffapigateway.dtos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Data
public class PhotoDetails {

        private int photoId;

        private String name;

        private String type;

}
