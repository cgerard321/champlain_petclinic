package com.petclinic.authservice.Util.AdminAccount;

import com.petclinic.authservice.datalayer.roles.Role;
import com.petclinic.authservice.datalayer.roles.RoleRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LegacyCustomerRoleMigration {
    private final RoleRepo roleRepo;

    @Transactional
    public void migrate() {
        List<Role> roles = roleRepo.findAllForMigration();
        Role owner = roles.stream().filter(role -> "OWNER".equals(role.getName())).findFirst().orElse(null);
        boolean customerExists = roles.stream().anyMatch(role -> "CUST".equals(role.getName()));
        if (owner != null && customerExists) {
            throw new IllegalStateException("AUTH-CPC-2062: OWNER and CUST both exist. "
                    + "Stop AUTH rollout and have the database operator reconcile role memberships "
                    + "before restarting; no automatic merge or deletion is performed.");
        }
        if (owner != null) {
            owner.setName("CUST");
            roleRepo.saveAndFlush(owner);
        }
    }
}
