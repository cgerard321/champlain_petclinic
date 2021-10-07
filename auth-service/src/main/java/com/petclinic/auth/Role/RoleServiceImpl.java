package com.petclinic.auth.Role;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepo roleRepo;

    @Override
    public Role createRole(Role role) {

        log.info("Saving role with name {}", role.getName());
        return roleRepo.save(role);
    }

    @Override
    public Page<Role> findAll(PageRequest pageRequest) {

        return roleRepo.findAll(pageRequest);
    }

    @Override
    public void deleteById(long id) throws EmptyResultDataAccessException {
        log.info("Deleting role with id {}", id);
        roleRepo.deleteById(id);
    }

}
