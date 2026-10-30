package com.petclinic.visits.visitsservicenew.BusinessLayer;


import com.petclinic.visits.visitsservicenew.DataLayer.*;
import com.petclinic.visits.visitsservicenew.DomainClientLayer.*;
import com.petclinic.visits.visitsservicenew.DomainClientLayer.Auth.AuthServiceClient;
import com.petclinic.visits.visitsservicenew.PresentationLayer.*;
import com.petclinic.visits.visitsservicenew.Utils.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VisitIdUniquenessTest {

    @Mock private VisitRepo repo;
    @Mock private PetsClient petsClient;
    @Mock private VetsClient vetsClient;
    @Mock private AuthServiceClient authServiceClient;
    @Mock private EntityDtoUtil entityDtoUtil;

    private VisitServiceImpl service() {
        return new VisitServiceImpl(
                repo,
                vetsClient,
                petsClient,
                entityDtoUtil,
                authServiceClient,
                null,
                null,
                null,
                null
        );
    }

    private VisitRequestDTO request(int minutes) {
        return VisitRequestDTO.builder()
                .description("Checkup")
                .visitDate(LocalDateTime.now()
                        .plusDays(2)
                        .plusMinutes(minutes))
                .petId("pet-123")
                .practitionerId("vet-456")
                .ownerId("owner-789")
                .jwtToken("test-token")
                .status(Status.UPCOMING)
                .build();
    }

    private void stubVisitCreation() {
        when(petsClient.getPetById(anyString()))
                .thenReturn(Mono.just(new PetResponseDTO()));

        when(vetsClient.getVetByVetId(anyString()))
                .thenReturn(Mono.just(new VetDTO()));

        when(authServiceClient.getUserById(
                "test-token", "owner-789"))
                .thenReturn(Mono.empty());

        when(entityDtoUtil.toVisitEntity(
                any(VisitRequestDTO.class)))
                .thenAnswer(invocation -> {
                    VisitRequestDTO dto =
                            invocation.getArgument(0);

                    return Visit.builder()
                            .description(dto.getDescription())
                            .visitDate(dto.getVisitDate())
                            .petId(dto.getPetId())
                            .practitionerId(dto.getPractitionerId())
                            .status(dto.getStatus())
                            .build();
                });

        when(repo.findByVisitDateAndPractitionerId(
                any(LocalDateTime.class),
                anyString()))
                .thenReturn(Flux.empty());

        when(repo.insert(any(Visit.class)))
                .thenAnswer(invocation ->
                        Mono.just(invocation.getArgument(0)));

        when(entityDtoUtil.toVisitResponseDTO(
                any(Visit.class)))
                .thenAnswer(invocation -> {
                    Visit visit = invocation.getArgument(0);

                    return Mono.just(
                            VisitResponseDTO.builder()
                                    .visitId(visit.getVisitId())
                                    .build()
                    );
                });
    }

    // TEST 1: New ID is available.
    @Test
    void addVisit_whenIdIsUnused_insertsVisit() {
        stubVisitCreation();

        String id = "VIST-2609-3001";

        when(repo.existsByVisitId(id))
                .thenReturn(Mono.just(false));

        try (MockedStatic<IdGenerator> generator =
                     mockStatic(IdGenerator.class)) {

            generator.when(IdGenerator::generateVisitId)
                    .thenReturn(id);

            VisitResponseDTO response = service()
                    .addVisit(Mono.just(request(0)))
                    .block();

            assertNotNull(response);
            assertEquals(id, response.getVisitId());

            generator.verify(
                    IdGenerator::generateVisitId,
                    times(1));
        }

        // Database check must happen before insertion.
        InOrder order = inOrder(repo);

        order.verify(repo).existsByVisitId(id);
        order.verify(repo).insert(
                Mockito.<Visit>argThat(v -> id.equals(v.getVisitId())));

        verify(repo, times(1)).insert(any(Visit.class));
    }

    // TEST 2: Existing IDs must be skipped.
    @Test
    void addVisit_whenIdsExist_retriesUntilUnique() {
        stubVisitCreation();

        String first = "VIST-2609-3001";
        String second = "VIST-2609-3002";
        String third = "VIST-2609-3003";

        when(repo.existsByVisitId(first))
                .thenReturn(Mono.just(true));

        when(repo.existsByVisitId(second))
                .thenReturn(Mono.just(true));

        when(repo.existsByVisitId(third))
                .thenReturn(Mono.just(false));

        try (MockedStatic<IdGenerator> generator =
                     mockStatic(IdGenerator.class)) {

            generator.when(IdGenerator::generateVisitId)
                    .thenReturn(first, second, third);

            VisitResponseDTO response = service()
                    .addVisit(Mono.just(request(0)))
                    .block();

            assertNotNull(response);

            assertEquals(third, response.getVisitId());

            generator.verify(
                    IdGenerator::generateVisitId,
                    times(3));
        }

        InOrder order = inOrder(repo);

        order.verify(repo).existsByVisitId(first);
        order.verify(repo).existsByVisitId(second);
        order.verify(repo).existsByVisitId(third);

        order.verify(repo).insert(
                Mockito.<Visit>argThat(v -> third.equals(v.getVisitId())));

        verify(repo, times(1)).insert(any(Visit.class));
    }

    // TEST 3: Concurrent requests must get unique IDs.
    @Test
    void addVisit_concurrentRequests_generateUniqueIds() {
        stubVisitCreation();

        when(repo.existsByVisitId(anyString()))
                .thenReturn(Mono.just(false));

        int requestCount = 40;

        VisitServiceImpl visitService = service();

        List<VisitResponseDTO> responses =
                Flux.range(0, requestCount)
                        .flatMap(i ->
                                        visitService
                                                .addVisit(Mono.just(request(i)))
                                                .subscribeOn(
                                                        Schedulers.parallel()
                                                ),
                                12
                        )
                        .collectList()
                        .block(Duration.ofSeconds(15));

        assertNotNull(responses);
        assertEquals(requestCount, responses.size());

        Set<String> uniqueIds = responses.stream()
                .map(VisitResponseDTO::getVisitId)
                .collect(Collectors.toSet());

        assertEquals(requestCount, uniqueIds.size());

        assertTrue(uniqueIds.stream()
                .allMatch(id -> id != null &&
                        id.startsWith("VIST-")));

        verify(repo, times(requestCount))
                .existsByVisitId(anyString());

        verify(repo, times(requestCount))
                .insert(any(Visit.class));
    }
}