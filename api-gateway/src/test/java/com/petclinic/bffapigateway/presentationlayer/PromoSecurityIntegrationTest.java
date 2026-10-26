package com.petclinic.bffapigateway.presentationlayer;

import com.petclinic.bffapigateway.config.GlobalServletExceptionHandler;
import com.petclinic.bffapigateway.domainclientlayer.*;
import com.petclinic.bffapigateway.dtos.Auth.TokenResponseDTO;
import com.petclinic.bffapigateway.dtos.Cart.PromoCodeResponseDTO;
import com.petclinic.bffapigateway.presentationlayer.v2.PromoCodeController;
import com.petclinic.bffapigateway.utils.Security.Filters.*;
import com.petclinic.bffapigateway.utils.Security.Variables.SecurityConst;
import com.petclinic.bffapigateway.utils.Utility;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import java.util.List;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

@WebFluxTest(controllers = PromoCodeController.class, properties = "frontend.url=http://localhost:3000")
@Import({Utility.class, JwtTokenUtil.class, SecurityConst.class, JwtTokenFilter.class,
        RoleFilter.class, IsUserFilter.class, CsrfFilter.class, GlobalServletExceptionHandler.class})
class PromoSecurityIntegrationTest {
    @Autowired private WebTestClient client;
    @MockBean private AuthServiceClient auth;
    @MockBean private CartServiceClient carts;
    @MockBean private CustomersServiceClient customers;
    private static final String BASE = "/api/v2/gateway/promos";

    private void role(String role) {
        when(auth.validateToken("token")).thenReturn(Mono.just(ResponseEntity.ok(TokenResponseDTO.builder()
                .token("token").userId("employee").roles(List.of(role)).build())));
    }

    @Test
    void adminCanCreateTargetedPromo() {
        role("ADMIN");
        when(carts.createPromoCode(any())).thenReturn(Mono.just(PromoCodeResponseDTO.builder()
                .scope("PRODUCT").productIds(List.of("food")).build()));
        client.post().uri(BASE).accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON)
                .cookie("Bearer", "token").cookie("XSRF-TOKEN", "csrf").header("X-XSRF-TOKEN", "csrf")
                .bodyValue("{\"name\":\"Food sale\",\"code\":\"FOOD25\",\"discount\":25,\"scope\":\"PRODUCT\",\"productIds\":[\"food\"]}")
                .exchange().expectStatus().isOk().expectBody()
                .jsonPath("$.scope").isEqualTo("PRODUCT").jsonPath("$.productIds[0]").isEqualTo("food");
        verify(carts).createPromoCode(argThat(p -> "PRODUCT".equals(p.getScope()) && p.getProductIds().equals(List.of("food"))));
    }

    @ParameterizedTest
    @ValueSource(strings = {"GET", "POST", "PUT", "DELETE"})
    void customerCannotManagePromos(String method) {
        role("CUST");
        var request = client.method(HttpMethod.valueOf(method))
                .uri(BASE + (method.equals("PUT") || method.equals("DELETE") ? "/promo-id" : ""))
                .accept(method.equals("GET") ? MediaType.TEXT_EVENT_STREAM : MediaType.APPLICATION_JSON)
                .cookie("Bearer", "token").cookie("XSRF-TOKEN", "csrf").header("X-XSRF-TOKEN", "csrf")
                .contentType(MediaType.APPLICATION_JSON);
        request.bodyValue("{}").exchange().expectStatus().isForbidden();
        verifyNoInteractions(carts);
    }

    @ParameterizedTest
    @ValueSource(strings = {"VET", "RECEPTIONIST", "INVENTORY_MANAGER"})
    void nonAdminEmployeesCannotCreatePromos(String role) {
        role(role);
        client.post().uri(BASE).accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON)
                .cookie("Bearer", "token").cookie("XSRF-TOKEN", "csrf").header("X-XSRF-TOKEN", "csrf")
                .bodyValue("{}").exchange().expectStatus().isForbidden();
        verifyNoInteractions(carts);
    }

    @Test
    void unauthenticatedCannotManagePromos() {
        client.get().uri(BASE).accept(MediaType.TEXT_EVENT_STREAM).exchange().expectStatus().isUnauthorized();
        verifyNoInteractions(carts);
    }
}
