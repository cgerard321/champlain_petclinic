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
    private final RoleMapper roleMapper;

    @Override
    public Role createRole(RoleIDLessDTO roleIDLessDTO) {

        log.info("Received role dto, trying to convert model");
        log.info("DTO info: { name={}, parent={} }", roleIDLessDTO.getName(), roleIDLessDTO.getParent());
        final Role role = roleMapper.idLessDTOToModel(roleIDLessDTO);
        log.info("Successfully converted dto -> model");
        log.info("Saving role with name {}", roleIDLessDTO.getName());
        return roleRepo.save(role);
    }

    @Override
    public Page<Role> findAll(PageRequest pageRequest) {

        return roleRepo.findAll(pageRequest);
    }

    @Override
    public void deleteById(long id) throws EmptyResultDataAccessException {
        try {
            log.info("Deleting role with id {}", id);
            roleRepo.deleteById(id);
        } catch (EmptyResultDataAccessException e) {
            log.info("No role with id {}. Ignoring", id);
        }
    }

}
