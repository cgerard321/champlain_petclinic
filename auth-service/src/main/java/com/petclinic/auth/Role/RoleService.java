package com.petclinic.auth.Role;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface RoleService {

    Role createRole(RoleIDLessDTO roleIDLessDTO);
    Page<Role> findAll(PageRequest pageRequest);
    void deleteById(long id) throws EmptyResultDataAccessException;
}
