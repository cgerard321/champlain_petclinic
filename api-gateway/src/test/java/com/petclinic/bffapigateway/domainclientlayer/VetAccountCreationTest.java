package com.petclinic.bffapigateway.domainclientlayer;

import com.petclinic.bffapigateway.dtos.Auth.RegisterVet;
import com.petclinic.bffapigateway.dtos.Vets.VetRequestDTO;
import com.petclinic.bffapigateway.dtos.Vets.VetResponseDTO;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class VetAccountCreationTest {
    @Test
    void removesNewAccountWhenProfileCreationFails() {
        List<String> deleted = new ArrayList<>();
        WebClient.Builder builder = WebClient.builder().exchangeFunction(request -> {
            if (request.method() == HttpMethod.DELETE) {
                assertEquals(List.of("admin-token"), request.cookies().get("Bearer"));
                deleted.add(request.url().getPath());
                return Mono.just(ClientResponse.create(HttpStatus.NO_CONTENT).build());
            }
            return Mono.just(ClientResponse.create(HttpStatus.CREATED)
                    .header("Content-Type", "application/json").body("{}").build());
        });
        VetsServiceClient vets = mock(VetsServiceClient.class);
        IllegalStateException failure = new IllegalStateException("Vet service unavailable");
        when(vets.addVet(any())).thenReturn(Mono.error(failure));
        AuthServiceClient client = new AuthServiceClient(builder, mock(CustomersServiceClient.class),
                vets, "localhost", "7005", mock(CartServiceClient.class));
        RegisterVet registration = RegisterVet.builder().email("alex@example.com").username("alex")
                .password("SafePass1!").vet(VetRequestDTO.builder().firstName("Alex").lastName("Morin").build()).build();
        StepVerifier.create(client.addVetUser(Mono.just(registration), "admin-token")).expectErrorMatches(e -> e == failure).verify();
        assertEquals(List.of("/users/" + registration.getUserId()), deleted);
    }
    @Test
    void keepsAccountAndReturnsProfileOnSuccess() {
        List<HttpMethod> calls = new ArrayList<>();
        WebClient.Builder builder = WebClient.builder().exchangeFunction(request -> {
            calls.add(request.method());
            return Mono.just(ClientResponse.create(HttpStatus.CREATED)
                    .header("Content-Type", "application/json").body("{}").build());
        });
        VetsServiceClient vets = mock(VetsServiceClient.class);
        when(vets.addVet(any())).thenReturn(Mono.just(VetResponseDTO.builder().vetId("new-vet").build()));
        AuthServiceClient client = new AuthServiceClient(builder, mock(CustomersServiceClient.class),
                vets, "localhost", "7005", mock(CartServiceClient.class));
        RegisterVet registration = RegisterVet.builder().email("alex@example.com").username("alex")
                .password("SafePass1!").vet(VetRequestDTO.builder().firstName("Alex").lastName("Morin").build()).build();
        StepVerifier.create(client.addVetUser(Mono.just(registration), "admin-token"))
                .assertNext(vet -> assertEquals("new-vet", vet.getVetId())).verifyComplete();
        assertEquals(List.of(HttpMethod.POST), calls);
    }
}
