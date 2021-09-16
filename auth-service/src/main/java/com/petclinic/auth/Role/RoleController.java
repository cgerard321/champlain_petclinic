package com.petclinic.auth.Role;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/roles")
@Slf4j
@RequiredArgsConstructor
public class RoleController {

    private final RoleRepo roleRepo;
    private final RoleMapper roleMapper;

    @PostMapping
    public Role createRole(@RequestBody RoleIDLessDTO dto) {

        log.info("Received role dto, trying to convert model");
        log.info("DTO info: { name={}, parent={} }", dto.getName(), dto.getParent());
        final Role role = roleMapper.idLessDTOToModel(dto);
        log.info("Successfully converted dto -> model");

        log.info("Trying to persist role");
        final Role saved = roleRepo.save(role);
        log.info("Successfully persisted role");

        return saved;
    }

    @GetMapping
    public Page<Role> getAllRoles(
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int size
    ) {

        log.info("page={}", page);
        final Page<Role> all = roleRepo.findAll(PageRequest.of(page, size));
        log.info("Retrieved paginated result with {} entries and {} pages", all.getTotalElements(), all.getTotalPages());

        return all;
    }
}
