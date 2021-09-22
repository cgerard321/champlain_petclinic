package com.petclinic.auth.Role;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/roles")
@Slf4j
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;
    private final RoleMapper roleMapper;

    @PostMapping
    public Role createRole(@RequestBody RoleIDLessDTO dto) {

        log.info("Received role dto, trying to convert model");
        log.info("DTO info: { name={}, parent={} }", dto.getName(), dto.getParent());
        final Role role = roleMapper.idLessDTOToModel(dto);
        log.info("Successfully converted dto -> model");

        log.info("Trying to persist role");
        final Role saved = roleService.createRole(role);
        log.info("Successfully persisted role");

        return saved;
    }

    @GetMapping
    public Page<Role> getAllRoles(
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int size
    ) {

        log.info("page={}", page);
        final Page<Role> all = roleService.findAll(PageRequest.of(page - 1, size));
        log.info("Retrieved paginated result with {} entries and {} pages", all.getTotalElements(), all.getTotalPages());

        return all;
    }

    @DeleteMapping
    public void deleteRole(@RequestParam long id) {

        log.info("id={}", id);
        try {

            roleService.deleteById(id);
        } catch (EmptyResultDataAccessException e) {
            log.info("No role with id {}. Ignoring", id);
            return;
        }
        log.info("Deleted role with id {}", id);
    }
}
