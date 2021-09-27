package com.petclinic.auth.User;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "roles", ignore = true)
    })
    User idLessDTOToModel(UserIDLessDTO dto);
}