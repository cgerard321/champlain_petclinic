package com.petclinic.auth.Role;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
