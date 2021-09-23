package com.petclinic.auth.Role;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    @Mappings({
            @Mapping(target = "id", ignore = true)
    })
    Role idLessDTOToModel(RoleIDLessDTO dto);
}
