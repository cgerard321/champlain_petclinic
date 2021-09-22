package com.petclinic.auth.Role;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;

public interface RoleService {

    Role createRole(Role role);
    Page<Role> findAll(PageRequest pageRequest);
    void deleteById(long id) throws EmptyResultDataAccessException;
}
