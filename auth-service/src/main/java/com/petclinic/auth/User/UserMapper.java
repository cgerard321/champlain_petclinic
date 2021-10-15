/**
 * Created by IntelliJ IDEA.
 *
 * User: @Trilikin21
 * Date: 24/09/21
 * Ticket: feat(AUTH-CPC-64)
 *
 * User: @JordanAlbayrak
 * Date: 24/09/21
 * Ticket: feat(AUTH-CPC-102)
 *
 * User: @Fube
 * Date: 09/10/21
 * Ticket: feat(AUTH-CPC-310)
 *
 * User: @Fube
 * Date: 2021-10-14
 * Ticket: feat(AUTH-CPC-388)
 */

package com.petclinic.auth.User;

import com.petclinic.auth.User.data.User;
import com.petclinic.auth.User.data.UserIDLessRoleLessDTO;
import com.petclinic.auth.User.data.UserPasswordLessDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "roles", expression = "java(new HashSet())")
    })
    User idLessRoleLessDTOToModel(UserIDLessRoleLessDTO dto);

    @Mappings({
    })
    UserPasswordLessDTO modelToPasswordLessDTO(User model);
    UserPasswordLessDTO modelToIDLessPasswordLessDTO(User model);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "username", ignore = true),
            @Mapping(target = "roles", ignore = true)
    })
    User idLessUsernameLessToModel(UserIDLessUsernameLessDTO userIDLessUsernameLessDTO);
}