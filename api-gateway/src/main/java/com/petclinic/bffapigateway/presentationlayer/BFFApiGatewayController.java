package com.petclinic.bffapigateway.presentationlayer;

import com.petclinic.bffapigateway.domainclientlayer.CustomersServiceClient;
import com.petclinic.bffapigateway.domainclientlayer.VetsServiceClient;
import com.petclinic.bffapigateway.domainclientlayer.VisitsServiceClient;
import com.petclinic.bffapigateway.dtos.OwnerDetails;
import com.petclinic.bffapigateway.dtos.VetDetails;
import com.petclinic.bffapigateway.dtos.Visits;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Maciej Szarlinski
 * @author Christine Gerard
 * Copied from https://github.com/spring-petclinic/spring-petclinic-microservices
 * Modified to remove circuitbreaker
 */

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/gateway")
public class BFFApiGatewayController {

    private final CustomersServiceClient customersServiceClient;

    private final VisitsServiceClient visitsServiceClient;

    private final VetsServiceClient vetsServiceClient;

    @GetMapping(value = "owners/{ownerId}")
    public Mono<OwnerDetails> getOwnerDetails(final @PathVariable int ownerId) {
        return customersServiceClient.getOwner(ownerId)
            .flatMap(owner ->
                visitsServiceClient.getVisitsForPets(owner.getPetIds())
                    .map(addVisitsToOwner(owner))
            );
    }

    @GetMapping(value = "customer/owners")
    public Flux<OwnerDetails> getOwners() {
        return customersServiceClient.getOwners()
            .flatMap(n ->
                visitsServiceClient.getVisitsForPets(n.getPetIds())
                    .map(addVisitsToOwner(n))
            );
    }


    private Function<Visits, OwnerDetails> addVisitsToOwner(OwnerDetails owner) {
        return visits -> {
            owner.getPets()
                .forEach(pet -> pet.getVisits()
                    .addAll(visits.getItems().stream()
                        .filter(v -> v.getPetId() == pet.getId())
                        .collect(Collectors.toList()))
                );
            return owner;
        };
    }

    @GetMapping(value = "vets")
    public Flux<VetDetails> getVets() {
        return vetsServiceClient.getVets();
    }

    private final HashMap<Integer, HashMap<Object, Object>> roles = new HashMap<Integer, HashMap<Object, Object>>() {{
        put(1, new HashMap<Object, Object>(){{ put("name", "test"); }});
        put(2, new HashMap<Object, Object>(){{ put("name", "test1"); }});
        put(3, new HashMap<Object, Object>(){{ put("name", "test2"); }});
    }};

    @GetMapping(value = "/admin/roles")
    public Map<String, Object> getRoles() {
        Map<String, Object> toRet = new HashMap<>();
        List<Map<Object, Object>> content = new ArrayList<>();


        roles.forEach((k, v) -> {
            HashMap<Object, Object> role = new HashMap<>();
            role.put("id", k);
            role.put("name", v.get("name"));
            content.add(role);
        });

        toRet.put("content", content);

        return toRet;
    }

    @DeleteMapping(value = "/admin/roles/{id}")
    public void deleteRole(@PathVariable int id) {
        roles.remove(id);
    }

    @PostMapping(value = "/admin/roles")
    public Map<Object, Object> addRole(
            @RequestBody Map<String, String> body
    ) {
        final String name = body.get("name");
        final int id = new Random().nextInt() & Integer.MAX_VALUE;
        roles.put(id, new HashMap<Object, Object>(){{ put("name", name); }});

        return new HashMap<Object, Object>() {{
            put("id", id);
            put("name", roles.get(id));
        }};
    }
}
