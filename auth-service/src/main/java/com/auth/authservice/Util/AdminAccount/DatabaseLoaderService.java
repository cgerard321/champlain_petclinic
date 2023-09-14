package com.auth.authservice.Util.AdminAccount;

import com.auth.authservice.datalayer.roles.Role;
import com.auth.authservice.datalayer.roles.RoleRepo;
import com.auth.authservice.datalayer.user.User;
import com.auth.authservice.datalayer.user.UserRepo;
import org.hibernate.mapping.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class DatabaseLoaderService implements CommandLineRunner {

    @Autowired
    UserRepo userRepo;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    RoleRepo roleRepo;


    @Override
    public void run(String... args) throws Exception {
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
