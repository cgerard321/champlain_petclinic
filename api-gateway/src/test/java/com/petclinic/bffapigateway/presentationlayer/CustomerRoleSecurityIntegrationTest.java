package com.petclinic.bffapigateway.presentationlayer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petclinic.bffapigateway.config.GlobalServletExceptionHandler;
import com.petclinic.bffapigateway.domainclientlayer.AuthServiceClient;
import com.petclinic.bffapigateway.domainclientlayer.CartServiceClient;
import com.petclinic.bffapigateway.domainclientlayer.CustomersServiceClient;
import com.petclinic.bffapigateway.domainclientlayer.VisitsServiceClient;
import com.petclinic.bffapigateway.dtos.Auth.Register;
import com.petclinic.bffapigateway.dtos.Auth.TokenResponseDTO;
import com.petclinic.bffapigateway.dtos.Cart.CartResponseDTO;
import com.petclinic.bffapigateway.dtos.Visits.reviews.ReviewResponseDTO;
import com.petclinic.bffapigateway.presentationlayer.v1.CartControllerV1;
import com.petclinic.bffapigateway.presentationlayer.v1.VisitsControllerV1;
import com.petclinic.bffapigateway.utils.Security.Filters.CsrfFilter;
import com.petclinic.bffapigateway.utils.Security.Filters.IsUserFilter;
import com.petclinic.bffapigateway.utils.Security.Filters.JwtTokenFilter;
import com.petclinic.bffapigateway.utils.Security.Filters.JwtTokenUtil;
import com.petclinic.bffapigateway.utils.Security.Filters.RoleFilter;
import com.petclinic.bffapigateway.utils.Security.Variables.SecurityConst;
import com.petclinic.bffapigateway.utils.Utility;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = {CartControllerV1.class, VisitsControllerV1.class},
        properties = "frontend.url=http://localhost:3000")
@Import({Utility.class, JwtTokenUtil.class, SecurityConst.class, JwtTokenFilter.class,
        RoleFilter.class, IsUserFilter.class, CsrfFilter.class, GlobalServletExceptionHandler.class})
class CustomerRoleSecurityIntegrationTest {
    private static final String TOKEN = "customer-role-test-token";
    private static final String USER_ID = "customer-one";
    private static final String CSRF = "customer-role-test-csrf";
    private static final String CARTS = "/api/gateway/carts";

    @Autowired private WebTestClient client;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private AuthServiceClient auth;
    @MockBean private CartServiceClient carts;
    @MockBean private VisitsServiceClient visits;
    @MockBean private CustomersServiceClient customers;

    @Test
    void customerCanCreateCartOnPreviouslyOwnerOnlyEndpoint() {
        validatedRole("CUST");
        when(carts.createCart(any())).thenReturn(Mono.just(CartResponseDTO.builder().cartId("cart-one").build()));

        createCartWithCsrf().exchange().expectStatus().isCreated()
                .expectBody().jsonPath("$.cartId").isEqualTo("cart-one");

        verify(auth).validateToken(TOKEN);
        verify(carts).createCart(argThat(request -> USER_ID.equals(request.getCustomerId())));
    }

    @ParameterizedTest
    @ValueSource(strings = {"CUST", "ADMIN", "[CUST]", "cust"})
    void sharedCartEndpointKeepsCustomerAndAdminAccessAndRoleParsing(String role) {
        validatedRole(role);
        when(carts.getCartByCartId("cart-one"))
                .thenReturn(Mono.just(CartResponseDTO.builder().cartId("cart-one").build()));

        client.get().uri(CARTS + "/cart-one").accept(MediaType.APPLICATION_JSON)
                .cookie("Bearer", TOKEN).exchange().expectStatus().isOk()
                .expectBody().jsonPath("$.cartId").isEqualTo("cart-one");

        verify(carts).getCartByCartId("cart-one");
    }

    @ParameterizedTest
    @ValueSource(strings = {"ADMIN", "VET", "RECEPTIONIST", "INVENTORY_MANAGER"})
    void excludedRolesStillCannotCreateCustomerCart(String role) {
        validatedRole(role);

        createCartWithCsrf().exchange().expectStatus().isForbidden();

        verifyNoInteractions(carts);
    }

    @Test
    void customerCanDeleteOwnReview() {
        validatedRole("CUST");
        when(visits.deleteReview("review-one")).thenReturn(Mono.just(new ReviewResponseDTO()));

        deleteReview(USER_ID).exchange().expectStatus().isOk();

        verify(visits).deleteReview("review-one");
    }

    @Test
    void customerCannotDeleteAnotherUsersReview() {
        validatedRole("CUST");

        deleteReview("another-customer").exchange().expectStatus().isForbidden();

        verifyNoInteractions(visits);
    }

    @Test
    void administratorStillBypassesReviewUserIdRestriction() {
        validatedRole("ADMIN");
        when(visits.deleteReview("review-one")).thenReturn(Mono.just(new ReviewResponseDTO()));

        deleteReview("another-customer").exchange().expectStatus().isOk();

        verify(visits).deleteReview("review-one");
    }

    @Test
    void missingTokenStillRejected() {
        client.get().uri(CARTS + "/cart-one").accept(MediaType.APPLICATION_JSON)
                .exchange().expectStatus().isUnauthorized();

        verifyNoInteractions(auth, carts);
    }

    @Test
    void invalidTokenStillRejectedByValidationResponse() {
        when(auth.validateToken(TOKEN)).thenReturn(Mono.just(ResponseEntity.status(401).build()));

        client.get().uri(CARTS + "/cart-one").accept(MediaType.APPLICATION_JSON)
                .cookie("Bearer", TOKEN).exchange().expectStatus().isUnauthorized();

        verify(auth).validateToken(TOKEN);
        verifyNoInteractions(carts);
    }

    @Test
    void cookieWithoutBearerStillRejected() {
        client.get().uri(CARTS + "/cart-one").accept(MediaType.APPLICATION_JSON)
                .cookie("unrelated", "value").exchange().expectStatus().isUnauthorized();

        verifyNoInteractions(auth, carts);
    }

    @Test
    void legacyOwnerRoleHasNoAlias() {
        validatedRole("OWNER");

        client.get().uri(CARTS + "/cart-one").accept(MediaType.APPLICATION_JSON)
                .cookie("Bearer", TOKEN).exchange().expectStatus().isBadRequest();

        verifyNoInteractions(carts);
    }

    @Test
    void customerWriteWithoutCsrfStillRejected() {
        validatedRole("CUST");

        client.post().uri(CARTS).accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON).cookie("Bearer", TOKEN)
                .bodyValue("{\"customerId\":\"" + USER_ID + "\"}")
                .exchange().expectStatus().isForbidden()
                .expectBody(String.class).isEqualTo("CSRF token missing or invalid");

        verifyNoInteractions(carts);
    }

    @Test
    void customerWriteWithMismatchedCsrfStillRejected() {
        validatedRole("CUST");

        client.post().uri(CARTS).accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON).cookie("Bearer", TOKEN)
                .cookie("XSRF-TOKEN", CSRF).header("X-XSRF-TOKEN", "different-csrf")
                .bodyValue("{\"customerId\":\"" + USER_ID + "\"}")
                .exchange().expectStatus().isForbidden()
                .expectBody(String.class).isEqualTo("CSRF token missing or invalid");

        verifyNoInteractions(carts);
    }

    @Test
    void registrationSerializesCustAndIgnoresSuppliedDefaultRole() throws Exception {
        Register request = objectMapper.readValue("{\"username\":\"customer\",\"defaultRole\":\"ADMIN\"}", Register.class);

        assertEquals("CUST", request.getDefaultRole());
        assertEquals("CUST", objectMapper.readTree(objectMapper.writeValueAsString(request)).get("defaultRole").asText());
        assertEquals("CUST", Register.builder().username("customer").build().getDefaultRole());
    }

    private void validatedRole(String role) {
        when(auth.validateToken(TOKEN)).thenReturn(Mono.just(ResponseEntity.ok(TokenResponseDTO.builder()
                .token(TOKEN).userId(USER_ID).roles(List.of(role)).build())));
    }

    private WebTestClient.RequestHeadersSpec<?> createCartWithCsrf() {
        return client.post().uri(CARTS).accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON).cookie("Bearer", TOKEN)
                .cookie("XSRF-TOKEN", CSRF).header("X-XSRF-TOKEN", CSRF)
                .bodyValue("{\"customerId\":\"" + USER_ID + "\"}");
    }

    private WebTestClient.RequestHeadersSpec<?> deleteReview(String ownerId) {
        return client.delete().uri("/api/gateway/visits/owners/{ownerId}/reviews/review-one", ownerId)
                .accept(MediaType.APPLICATION_JSON).cookie("Bearer", TOKEN)
                .cookie("XSRF-TOKEN", CSRF).header("X-XSRF-TOKEN", CSRF);
    }
}
