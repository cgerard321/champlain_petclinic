package com.petclinic.auth.Role;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/roles")
public class RoleController {

    @PostMapping
    public Role createRole(@RequestBody RoleIDLessDTO role) {
        return null;
    }
}
