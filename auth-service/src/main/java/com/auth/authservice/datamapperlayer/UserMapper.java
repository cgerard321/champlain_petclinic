/**
 * Created by IntelliJ IDEA.
 * <p>
 * User: @Trilikin21
 * Date: 24/09/21
 * Ticket: feat(AUTH-CPC-64)
 * <p>
 * User: @JordanAlbayrak
 * Date: 24/09/21
 * Ticket: feat(AUTH-CPC-102)
 * <p>
 * User: @Fube
 * Date: 09/10/21
 * Ticket: feat(AUTH-CPC-310)
 * <p>
 * User: @Fube
 * Date: 2021-10-14
 * Ticket: feat(AUTH-CPC-388)
 */

package com.auth.authservice.datamapperlayer;

import com.auth.authservice.datalayer.user.User;

import com.auth.authservice.presentationlayer.User.UserDetails;
import com.auth.authservice.presentationlayer.User.UserIDLessRoleLessDTO;
import com.auth.authservice.presentationlayer.User.UserIDLessUsernameLessDTO;
import com.auth.authservice.presentationlayer.User.UserPasswordLessDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "roles", expression = "java(new HashSet())")
    })
    User idLessRoleLessDTOToModel(UserIDLessRoleLessDTO dto);

    @Mappings({
            @Mapping(target = "userId",expression = "java(model.getUserIdentifier().getUserId())")
    })
    UserPasswordLessDTO modelToPasswordLessDTO(User model);

    @Mappings({
            @Mapping(target = "userId",expression = "java(model.getUserIdentifier().getUserId())")
    })
    UserPasswordLessDTO modelToIDLessPasswordLessDTO(User model);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "username", ignore = true),
            @Mapping(target = "roles", ignore = true)
    })
    User idLessUsernameLessToModel(UserIDLessUsernameLessDTO userIDLessUsernameLessDTO);


    @Mappings({
            @Mapping(target = "userId",expression = "java(model.getUserIdentifier().getUserId())"),
    })
    UserDetails modelToDetails(User model);


    List<UserDetails> modelToDetailsList(List<User> model);
}