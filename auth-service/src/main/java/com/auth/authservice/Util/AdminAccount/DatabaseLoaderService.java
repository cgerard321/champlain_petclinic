package com.auth.authservice.Util.AdminAccount;

import com.auth.authservice.datalayer.roles.Role;
import com.auth.authservice.datalayer.roles.RoleRepo;
import com.auth.authservice.datalayer.user.User;
import com.auth.authservice.datalayer.user.UserIdentifier;
import com.auth.authservice.datalayer.user.UserRepo;
import lombok.RequiredArgsConstructor;
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
        roleRepo.save(Role.builder().name("VET").build());
        roleRepo.save(Role.builder().name("OWNER").build());


        Set<Role> roles = new HashSet<>();
        roles.add(roleRepo.findById(1L).get());
        User admin = User.builder()
                .username("Admin")
                .userIdentifier(new UserIdentifier())
                .roles(roles)
                .email("admin@admin.com")
                .password(passwordEncoder.encode("pwd"))
                .verified(true)
                .build();
        userRepo.save(admin);


        Set<Role> roles2 = new HashSet<>();
        roles2.add(roleRepo.findById(2L).get());
        User vet = User.builder()
                .username("Vet")
                .userIdentifier(new UserIdentifier())
                .roles(roles2)
                .email("dylan.brassard@outlook.com")
                .password(passwordEncoder.encode("pwd"))
                .verified(true)
                .build();


        userRepo.save(vet);
    }
}
