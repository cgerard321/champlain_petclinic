package com.petclinic.authservice.Util.AdminAccount;

import com.petclinic.authservice.datalayer.roles.Role;
import com.petclinic.authservice.datalayer.roles.RoleRepo;
import com.petclinic.authservice.datalayer.user.User;
import com.petclinic.authservice.datalayer.user.UserIdentifier;
import com.petclinic.authservice.datalayer.user.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DatabaseLoaderService implements CommandLineRunner {

    private final UserRepo userRepo;

    private final PasswordEncoder passwordEncoder;

    private final RoleRepo roleRepo;


    @Override
    public void run(String... args) {
        
       // if items in the database, do not run this
        if (userRepo.count() > 0 || roleRepo.count() > 0)
            return;
        
        roleRepo.save(Role.builder().name("ADMIN").build());
        roleRepo.save(Role.builder().name("VET").build());
        roleRepo.save(Role.builder().name("OWNER").build());
        roleRepo.save(Role.builder().name("INVENTORY_MANAGER").build());
        roleRepo.save(Role.builder().name("RECEPTIONIST").build());


        Set<Role> manager = new HashSet<>();
        manager.add(roleRepo.findById(4L).get());
        User inventoryManager = User.builder()
                .username("InventoryManager")
                .userIdentifier(new UserIdentifier())
                .roles(manager)
                .email("inventory@email.com")
                .password(passwordEncoder.encode("pwd"))
                .verified(true)
                .build();

        userRepo.save(inventoryManager);

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

        Set <Role> receptionist = new HashSet<>();
        receptionist.add(roleRepo.findById(5L).get());
        User receptionist1 = User.builder()
                .username("Receptionist1")
                .userIdentifier(new UserIdentifier())
                .roles(receptionist)
                .email("reception@reception.com")
                .password(passwordEncoder.encode("pwd"))
                .verified(true)
                .build();

        userRepo.save(receptionist1);


        Set<Role> roles2 = new HashSet<>();
        roles2.add(roleRepo.findById(2L).get());

        Set<Role> owners = new HashSet<>();
        owners.add(roleRepo.findById(3L).get());

        User owner1 = User.builder()
                .username("Owner1")
                .userIdentifier(new UserIdentifier("f470653d-05c5-4c45-b7a0-7d70f003d2ac"))
                .roles(owners)
                .email("george@email.com")
                .password(passwordEncoder.encode("pwd"))
                .verified(true)
                .build();

        User owner2 = User.builder()
                .username("Owner2")
                .userIdentifier(new UserIdentifier("e6c7398e-8ac4-4e10-9ee0-03ef33f0361a"))
                .roles(owners)
                .email("betty@email.com")
                .password(passwordEncoder.encode("pwd"))
                .verified(true)
                .build();

        User owner3 = User.builder()
                .username("Owner3")
                .userIdentifier(new UserIdentifier("3f59dca2-903e-495c-90c3-7f4d01f3a2aa"))
                .roles(owners)
                .email("eduardo@email.com")
                .password(passwordEncoder.encode("pwd"))
                .verified(true)
                .build();

        User owner4 = User.builder()
                .username("Owner4")
                .userIdentifier(new UserIdentifier("a6e0e5b0-5f60-45f0-8ac7-becd8b330486"))
                .roles(owners)
                .email("harold@email.com")
                .password(passwordEncoder.encode("pwd"))
                .verified(true)
                .build();

        User owner5 = User.builder()
                .username("Owner5")
                .userIdentifier(new UserIdentifier("c6a0fb9d-fc6f-4c21-95fc-4f5e7311d0e2"))
                .roles(owners)
                .email("peter@email.com")
                .password(passwordEncoder.encode("pwd"))
                .verified(true)
                .build();

        User owner6 = User.builder()
                .username("Owner6")
                .userIdentifier(new UserIdentifier("b3d09eab-4085-4b2d-a121-78a0a2f9e501"))
                .roles(owners)
                .email("jean@email.com")
                .password(passwordEncoder.encode("pwd"))
                .verified(true)
                .build();

        User owner7 = User.builder()
                .username("Owner7")
                .userIdentifier(new UserIdentifier("5fe81e29-1f1d-4f9d-b249-8d3e0cc0b7dd"))
                .roles(owners)
                .email("jeff@email.com")
                .password(passwordEncoder.encode("pwd"))
                .verified(true)
                .build();

        User owner8 = User.builder()
                .username("Owner8")
                .userIdentifier(new UserIdentifier("48f9945a-4ee0-4b0b-9b44-3da829a0f0f7"))
                .roles(owners)
                .email("maria@email.com")
                .password(passwordEncoder.encode("pwd"))
                .verified(true)
                .build();

        User owner9 = User.builder()
                .username("Owner9")
                .userIdentifier(new UserIdentifier("9f6accd1-e943-4322-932e-199d93824317"))
                .roles(owners)
                .email("david@email.com")
                .password(passwordEncoder.encode("pwd"))
                .verified(true)
                .build();

        User owner10 = User.builder()
                .username("Owner10")
                .userIdentifier(new UserIdentifier("7c0d42c2-0c2d-41ce-bd9c-6ca67478956f"))
                .roles(owners)
                .email("carlos@email.com")
                .password(passwordEncoder.encode("pwd"))
                .verified(true)
                .build();



        User vet1 = User.builder()
                .username("Vet1")
                .userIdentifier(new UserIdentifier("69f852ca-625b-11ee-8c99-0242ac120002"))
                .roles(roles2)
                .email("carterjames@email.com")
                .password(passwordEncoder.encode("pwd"))
                .verified(true)
                .build();

        User vet2 = User.builder()
                .username("Vet2")
                .userIdentifier(new UserIdentifier("69f85766-625b-11ee-8c99-0242ac120002"))
                .roles(roles2)
                .email("learyhelen@email.com")
                .password(passwordEncoder.encode("pwd"))
                .verified(true)
                .build();

        User vet3 = User.builder()
                .username("Vet3")
                .userIdentifier(new UserIdentifier("69f85bda-625b-11ee-8c99-0242ac120002"))
                .roles(roles2)
                .email("douglaslinda@email.com")
                .password(passwordEncoder.encode("pwd"))
                .verified(true)
                .build();

        User vet4 = User.builder()
                .username("Vet4")
                .userIdentifier(new UserIdentifier("69f85d2e-625b-11ee-8c99-0242ac120002"))
                .roles(roles2)
                .email("ortegarafael@email.com")
                .password(passwordEncoder.encode("pwd"))
                .verified(true)
                .build();

        User vet5 = User.builder()
                .username("Vet5")
                .userIdentifier(new UserIdentifier("ac9adeb8-625b-11ee-8c99-0242ac120002"))
                .roles(roles2)
                .email("stevenshenry@email.com")
                .password(passwordEncoder.encode("pwd"))
                .verified(true)
                .build();

        User vet6 = User.builder()
                .username("Vet6")
                .userIdentifier(new UserIdentifier("ac9adeb8-625b-11ee-8c99-0242ac120003"))
                .roles(roles2)
                .email("jenkinssharon@email.com")
                .password(passwordEncoder.encode("pwd"))
                .verified(true)
                .build();

        User vet7 = User.builder()
                .username("Vet7")
                .userIdentifier(new UserIdentifier("c02cbf82-625b-11ee-8c99-0242ac120002"))
                .roles(roles2)
                .email("johndoe@email.com")
                .password(passwordEncoder.encode("pwd"))
                .verified(true)
                .build();



        userRepo.saveAll(List.of(vet1, vet2, vet3, vet4, vet5, vet6, vet7));

        userRepo.saveAll(List.of(owner1, owner2, owner3, owner4, owner5, owner6, owner7, owner8, owner9, owner10));
    }
}
