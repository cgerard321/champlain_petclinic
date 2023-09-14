package com.auth.authservice.Util.AdminAccount;

import com.auth.authservice.datalayer.roles.Role;
import com.auth.authservice.datalayer.roles.RoleRepo;
import com.auth.authservice.datalayer.user.User;
import com.auth.authservice.datalayer.user.UserRepo;
import lombok.RequiredArgsConstructor;
import org.hibernate.mapping.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DatabaseLoaderService implements CommandLineRunner {

    private final UserRepo userRepo;

    private final PasswordEncoder passwordEncoder;

    private final RoleRepo roleRepo;


    @Override
    public void run(String... args) throws Exception {
        roleRepo.save(Role.builder().name("ADMIN").build());

        Set<Role> roles = new HashSet<>();
        roles.add(roleRepo.findById(1L).get());
        User admin = User.builder()
                .username("Admin")
                .roles(roles)
                .email("admin@admin.com")
                .password(passwordEncoder.encode("pwd"))
                .verified(true)
                .build();
        userRepo.save(admin);
    }
}
