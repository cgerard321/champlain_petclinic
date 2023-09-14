/**
 * Created by IntelliJ IDEA.
 *
 * User: @Fube
 * Date: 21/09/21
 * Ticket: feat(AUTH-CPC-95)
 *
 */
package com.petclinic.auth.Role;

import com.petclinic.auth.Role.data.Role;
import com.petclinic.auth.Role.data.RoleIDLessDTO;
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
