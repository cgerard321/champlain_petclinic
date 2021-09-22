package com.petclinic.auth.Role;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class RoleServiceImpl implements RoleService {


    @Override
    public Role createRole(Role role) {
        return null;
    }

    @Override
    public Page<Role> findAll(PageRequest pageRequest) {
        return null;
    }

    @Override
    public void deleteById(long id) throws EmptyResultDataAccessException {

    }

}
