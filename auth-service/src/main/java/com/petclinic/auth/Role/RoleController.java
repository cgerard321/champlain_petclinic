/**
 * Created by IntelliJ IDEA.
 *
 *  User: @Fube
 * Date: 21/09/21
 * Ticket: feat(AUTH-CPC-95)
 *
 */
package com.petclinic.auth.Role;

import com.petclinic.auth.Role.data.Role;
import com.petclinic.auth.Role.data.RoleIDLessDTO;
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

    private final RoleService roleService;

    @PostMapping
    public Role createRole(@RequestBody RoleIDLessDTO dto) {

        log.info("Trying to persist role");
        final Role saved = roleService.createRole(dto);
        log.info("Successfully persisted role");

        return saved;
    }

    @GetMapping
    public Page<Role> getAllRoles(
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int size
    )
    {

        log.info("page={}", page);
        final Page<Role> all = roleService.findAll(PageRequest.of(page - 1, size));
        log.info("Retrieved paginated result with {} entries and {} pages", all.getTotalElements(), all.getTotalPages());

        return all;
    }

    @DeleteMapping
    public void deleteRole(@RequestParam long id) {

        roleService.deleteById(id);
        log.info("Deleted role with id {}", id);
    }
}
