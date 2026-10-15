package com.petclinic.bffapigateway.presentationlayer.v2;

import com.petclinic.bffapigateway.domainclientlayer.AuthServiceClient;
import com.petclinic.bffapigateway.domainclientlayer.VetsServiceClient;
import com.petclinic.bffapigateway.dtos.Auth.TokenResponseDTO;
import com.petclinic.bffapigateway.exceptions.ForbiddenAccessException;
import com.petclinic.bffapigateway.utils.Security.Filters.IsUserFilter;
import com.petclinic.bffapigateway.utils.Security.Filters.RoleFilter;
import com.petclinic.bffapigateway.utils.Utility;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;

class VetProfileAuthorizationTest {
    @ParameterizedTest
    @CsvSource({
            "ADMIN, different-vet, true",
            "VET, selected-vet, true",
            "VET, different-vet, false",
            "RECEPTIONIST, selected-vet, false",
            "OWNER, selected-vet, false"
    })
    void protectsTheProfileUpdateEndpoint(String role, String userId, boolean allowed) throws Exception {
        VetController controller = new VetController(mock(VetsServiceClient.class), mock(AuthServiceClient.class));
        HandlerMethod handler = new HandlerMethod(controller, "updateVet", Mono.class, String.class);
        Utility utility = mock(Utility.class);
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.put("/api/v2/gateway/vets/selected-vet"));
        exchange.getAttributes().put("tokenValues", TokenResponseDTO.builder()
                .userId(userId).roles(List.of(role)).build());
        exchange.getAttributes().put(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE,
                Map.of("vetId", "selected-vet"));
        when(utility.getHandler(exchange)).thenReturn(handler);
        WebFilterChain target = mock(WebFilterChain.class);
        if (allowed) when(target.filter(exchange)).thenReturn(Mono.empty());
        RoleFilter roleFilter = new RoleFilter(utility);
        IsUserFilter ownershipFilter = new IsUserFilter(utility);

        Mono<Void> result = Mono.defer(() -> roleFilter.filter(exchange,
                next -> ownershipFilter.filter(next, target)));
        if (allowed) {
            StepVerifier.create(result).verifyComplete();
            verify(target).filter(exchange);
        } else {
            StepVerifier.create(result).expectError(ForbiddenAccessException.class).verify();
            verifyNoInteractions(target);
        }
    }
}
