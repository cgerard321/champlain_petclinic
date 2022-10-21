package com.petclinic.auth.UserRole;

import com.petclinic.auth.Role.RoleRepo;
import com.petclinic.auth.User.UserRepo;
import com.petclinic.auth.UserRole.data.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserRoleServiceImpl {

    private final UserRepo userRepo;
    private final RoleRepo roleRepo;

    public UserRoleServiceImpl(UserRepo userRepo, RoleRepo roleRepo) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
    }

//    public UserRole Bind(PetRequest petRequest, int ownerId)
}
