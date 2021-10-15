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
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface RoleService {

    Role createRole(RoleIDLessDTO roleIDLessDTO);
    Page<Role> findAll(PageRequest pageRequest);
    void deleteById(long id) throws EmptyResultDataAccessException;
}
