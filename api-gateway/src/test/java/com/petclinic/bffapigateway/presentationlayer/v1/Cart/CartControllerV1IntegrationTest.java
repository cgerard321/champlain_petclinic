package com.petclinic.bffapigateway.presentationlayer.v1.Cart;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petclinic.bffapigateway.dtos.Cart.CartItemRequestDTO;
import com.petclinic.bffapigateway.dtos.Cart.CartProductResponseDTO;
import com.petclinic.bffapigateway.dtos.Cart.CartResponseDTO;
import com.petclinic.bffapigateway.dtos.Cart.UpdateProductQuantityRequestDTO;
import com.petclinic.bffapigateway.dtos.Cart.WishlistItemRequestDTO;
import com.petclinic.bffapigateway.dtos.Cart.WishlistTransferDirectionDTO;
import com.petclinic.bffapigateway.dtos.Cart.WishlistTransferRequestDTO;
import com.petclinic.bffapigateway.presentationlayer.v2.mockservers.MockServerConfigAuthService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CartControllerV1IntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    private ClientAndServer cartServiceMockServer;
    private MockServerClient cartMockClient;
    private MockServerConfigAuthService authServiceMock;

    private static final String OWNER_TOKEN = MockServerConfigAuthService.jwtTokenForValidOwnerId;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeAll
    void startMockServers() {
	cartServiceMockServer = ClientAndServer.startClientAndServer(7008);
	cartMockClient = new MockServerClient("localhost", 7008);

	authServiceMock = new MockServerConfigAuthService();
	authServiceMock.registerValidateTokenForOwnerEndpoint();
    }

    @AfterAll
    void stopMockServers() {
	if (cartMockClient != null) {
	    cartMockClient.reset();
	}
	if (cartServiceMockServer != null) {
	    cartServiceMockServer.stop();
	}
	if (authServiceMock != null) {
	    authServiceMock.stopMockServer();
	}
    }

    @BeforeEach
    void resetCartMock() {
	if (cartMockClient != null) {
	    cartMockClient.reset();
	}
    }

    @Test
    void addProductToCart_returnsCreatedWithLocation() throws JsonProcessingException {
	String cartId = "cart-001";
	String productId = "prod-100";

	stubCartService(
		"POST",
		"/api/v1/carts/" + cartId + "/products",
		201,
		cartResponseJson(cartId, List.of(buildCartProduct(productId, 2)), List.of())
	);

	CartItemRequestDTO requestDTO = new CartItemRequestDTO(productId, 2);

	webTestClient.post()
		.uri("/api/gateway/carts/{cartId}/products", cartId)
		.cookie("Bearer", OWNER_TOKEN)
		.contentType(MediaType.APPLICATION_JSON)
		.accept(MediaType.APPLICATION_JSON)
		.bodyValue(requestDTO)
		.exchange()
		.expectStatus().isCreated()
		.expectHeader().valueEquals("Location", "/api/gateway/carts/" + cartId + "/products/" + productId)
		.expectBody()
		.jsonPath("$.cartId").isEqualTo(cartId)
		.jsonPath("$.products[0].productId").isEqualTo(productId)
		.jsonPath("$.products[0].quantityInCart").isEqualTo(2);
    }

    @Test
    void addProductToCart_whenCartServiceReturnsBadRequest_returnsBadRequestWithMessage() throws JsonProcessingException {
	String cartId = "cart-002";
	String productId = "prod-404";
	String errorMessage = "Requested quantity exceeds inventory";

	stubCartService(
		"POST",
		"/api/v1/carts/" + cartId + "/products",
		400,
		cartErrorResponseJson(errorMessage)
	);

	CartItemRequestDTO requestDTO = new CartItemRequestDTO(productId, 5);

	webTestClient.post()
		.uri("/api/gateway/carts/{cartId}/products", cartId)
		.cookie("Bearer", OWNER_TOKEN)
		.contentType(MediaType.APPLICATION_JSON)
		.accept(MediaType.APPLICATION_JSON)
		.bodyValue(requestDTO)
		.exchange()
		.expectStatus().isBadRequest()
		.expectBody()
		.jsonPath("$.message").isEqualTo(errorMessage);
    }

    @Test
    void removeProductFromCart_returnsNoContent() {
	String cartId = "cart-003";
	String productId = "prod-509";

	stubCartService(
		"DELETE",
		"/api/v1/carts/" + cartId + "/products/" + productId,
		204,
		null
	);

	webTestClient.delete()
		.uri("/api/gateway/carts/{cartId}/products/{productId}", cartId, productId)
		.cookie("Bearer", OWNER_TOKEN)
		.accept(MediaType.APPLICATION_JSON)
		.exchange()
		.expectStatus().isNoContent()
		.expectBody().isEmpty();
    }

    @Test
    void removeProductFromCart_whenCartServiceReturnsNotFound_returnsNotFound() {
	String cartId = "cart-003";
	String productId = "prod-missing";

	stubCartService(
		"DELETE",
		"/api/v1/carts/" + cartId + "/products/" + productId,
		404,
		null
	);

	webTestClient.delete()
		.uri("/api/gateway/carts/{cartId}/products/{productId}", cartId, productId)
		.cookie("Bearer", OWNER_TOKEN)
		.accept(MediaType.APPLICATION_JSON)
		.exchange()
		.expectStatus().isNotFound();
    }

    @Test
    void deleteAllItemsInCart_returnsNoContent() {
	String cartId = "cart-004";

	stubCartService(
		"DELETE",
		"/api/v1/carts/" + cartId + "/products",
		204,
		null
	);

	webTestClient.delete()
		.uri("/api/gateway/carts/{cartId}/products", cartId)
		.cookie("Bearer", OWNER_TOKEN)
		.accept(MediaType.APPLICATION_JSON)
		.exchange()
		.expectStatus().isNoContent()
		.expectBody().isEmpty();
    }

    @Test
    void deleteAllItemsInCart_whenCartServiceReturnsNotFound_returnsNotFound() {
	String cartId = "cart-004";

	stubCartService(
		"DELETE",
		"/api/v1/carts/" + cartId + "/products",
		404,
		null
	);

	webTestClient.delete()
		.uri("/api/gateway/carts/{cartId}/products", cartId)
		.cookie("Bearer", OWNER_TOKEN)
		.accept(MediaType.APPLICATION_JSON)
		.exchange()
		.expectStatus().isNotFound();
    }

    @Test
    void patchProductQuantityInCart_returnsOkWithUpdatedCart() throws JsonProcessingException {
	String cartId = "cart-005";
	String productId = "prod-222";

	stubCartService(
		"PATCH",
		"/api/v1/carts/" + cartId + "/products/" + productId,
		200,
		cartResponseJson(cartId, List.of(buildCartProduct(productId, 5)), List.of())
	);

	UpdateProductQuantityRequestDTO requestDTO = UpdateProductQuantityRequestDTO.builder()
		.quantity(5)
		.build();

	webTestClient.patch()
		.uri("/api/gateway/carts/{cartId}/products/{productId}", cartId, productId)
		.cookie("Bearer", OWNER_TOKEN)
		.contentType(MediaType.APPLICATION_JSON)
		.accept(MediaType.APPLICATION_JSON)
		.bodyValue(requestDTO)
		.exchange()
		.expectStatus().isOk()
		.expectBody()
		.jsonPath("$.products[0].productId").isEqualTo(productId)
		.jsonPath("$.products[0].quantityInCart").isEqualTo(5);
    }

    @Test
    void patchProductQuantityInCart_whenCartServiceReturnsNotFound_returnsNotFound() {
	String cartId = "cart-005";
	String productId = "prod-unknown";

	stubCartService(
		"PATCH",
		"/api/v1/carts/" + cartId + "/products/" + productId,
		404,
		null
	);

	UpdateProductQuantityRequestDTO requestDTO = UpdateProductQuantityRequestDTO.builder()
		.quantity(3)
		.build();

	webTestClient.patch()
		.uri("/api/gateway/carts/{cartId}/products/{productId}", cartId, productId)
		.cookie("Bearer", OWNER_TOKEN)
		.contentType(MediaType.APPLICATION_JSON)
		.bodyValue(requestDTO)
		.exchange()
		.expectStatus().isNotFound();
    }

    @Test
    void addProductToWishlist_returnsCreated() throws JsonProcessingException {
	String cartId = "cart-006";
	String productId = "prod-777";

	stubCartService(
		"POST",
		"/api/v1/carts/" + cartId + "/wishlist",
		201,
		cartResponseJson(cartId, List.of(), List.of(buildCartProduct(productId, 1)))
	);

	WishlistItemRequestDTO requestDTO = new WishlistItemRequestDTO(productId, 1);

	webTestClient.post()
		.uri("/api/gateway/carts/{cartId}/wishlist", cartId)
		.cookie("Bearer", OWNER_TOKEN)
		.contentType(MediaType.APPLICATION_JSON)
		.accept(MediaType.APPLICATION_JSON)
		.bodyValue(requestDTO)
		.exchange()
		.expectStatus().isCreated()
		.expectBody()
		.jsonPath("$.wishListProducts[0].productId").isEqualTo(productId)
		.jsonPath("$.wishListProducts[0].quantityInCart").isEqualTo(1);
    }

    @Test
    void addProductToWishlist_whenCartServiceReturnsBadRequest_returnsBadRequestWithMessage() throws JsonProcessingException {
	String cartId = "cart-006";
	String productId = "prod-777";
	String errorMessage = "Wishlist item already present";

	stubCartService(
		"POST",
		"/api/v1/carts/" + cartId + "/wishlist",
		400,
		cartErrorResponseJson(errorMessage)
	);

	WishlistItemRequestDTO requestDTO = new WishlistItemRequestDTO(productId, 1);

	webTestClient.post()
		.uri("/api/gateway/carts/{cartId}/wishlist", cartId)
		.cookie("Bearer", OWNER_TOKEN)
		.contentType(MediaType.APPLICATION_JSON)
		.accept(MediaType.APPLICATION_JSON)
		.bodyValue(requestDTO)
		.exchange()
		.expectStatus().isBadRequest()
		.expectBody()
		.jsonPath("$.message").isEqualTo(errorMessage);
    }

    @Test
    void removeProductFromWishlist_returnsNoContent() {
	String cartId = "cart-007";
	String productId = "prod-888";

	stubCartService(
		"DELETE",
		"/api/v1/carts/" + cartId + "/wishlist/" + productId,
		204,
		null
	);

	webTestClient.delete()
		.uri("/api/gateway/carts/{cartId}/wishlist/{productId}", cartId, productId)
		.cookie("Bearer", OWNER_TOKEN)
		.accept(MediaType.APPLICATION_JSON)
		.exchange()
		.expectStatus().isNoContent()
		.expectBody().isEmpty();
    }

    @Test
    void removeProductFromWishlist_whenCartServiceReturnsNotFound_returnsNotFound() {
	String cartId = "cart-007";
	String productId = "prod-missing";

	stubCartService(
		"DELETE",
		"/api/v1/carts/" + cartId + "/wishlist/" + productId,
		404,
		null
	);

	webTestClient.delete()
		.uri("/api/gateway/carts/{cartId}/wishlist/{productId}", cartId, productId)
		.cookie("Bearer", OWNER_TOKEN)
		.accept(MediaType.APPLICATION_JSON)
		.exchange()
		.expectStatus().isNotFound();
    }

    @Test
    void createWishlistTransfer_returnsOk() throws JsonProcessingException {
	String cartId = "cart-008";
	List<String> productIds = List.of("prod-1", "prod-2");

	stubCartService(
		"POST",
		"/api/v1/carts/" + cartId + "/wishlist-transfers",
		200,
		cartResponseJson(cartId, List.of(buildCartProduct("prod-1", 1), buildCartProduct("prod-2", 1)), List.of())
	);

	WishlistTransferRequestDTO requestDTO = new WishlistTransferRequestDTO(productIds, WishlistTransferDirectionDTO.TO_CART);

	webTestClient.post()
		.uri("/api/gateway/carts/{cartId}/wishlist-transfers", cartId)
		.cookie("Bearer", OWNER_TOKEN)
		.contentType(MediaType.APPLICATION_JSON)
		.accept(MediaType.APPLICATION_JSON)
		.bodyValue(requestDTO)
		.exchange()
		.expectStatus().isOk()
		.expectBody()
		.jsonPath("$.products[0].productId").isEqualTo("prod-1")
		.jsonPath("$.products[1].productId").isEqualTo("prod-2");
    }

    @Test
    void createWishlistTransfer_whenCartServiceReturnsNotFound_returnsNotFound() {
	String cartId = "cart-008";

	stubCartService(
		"POST",
		"/api/v1/carts/" + cartId + "/wishlist-transfers",
		404,
		null
	);

	WishlistTransferRequestDTO requestDTO = new WishlistTransferRequestDTO(List.of("prod-404"), WishlistTransferDirectionDTO.TO_CART);

	webTestClient.post()
		.uri("/api/gateway/carts/{cartId}/wishlist-transfers", cartId)
		.cookie("Bearer", OWNER_TOKEN)
		.contentType(MediaType.APPLICATION_JSON)
		.bodyValue(requestDTO)
		.exchange()
		.expectStatus().isNotFound();
    }

    private void stubCartService(String method, String path, int statusCode, String body) {
	HttpResponse mockResponse = response().withStatusCode(statusCode);
	if (body != null) {
	    mockResponse.withHeader("Content-Type", "application/json");
	    mockResponse.withBody(body);
	}

	cartMockClient
		.when(
			request()
				.withMethod(method)
				.withPath(path)
		)
		.respond(mockResponse);
    }

    private CartProductResponseDTO buildCartProduct(String productId, int quantity) {
	return CartProductResponseDTO.builder()
		.productId(productId)
		.imageId("image-1")
		.productName("Test Product")
		.productDescription("Sample description")
		.productSalePrice(19.99)
		.averageRating(4.5)
		.quantityInCart(quantity)
		.productQuantity(100)
		.build();
    }

    private String cartResponseJson(String cartId,
				    List<CartProductResponseDTO> products,
				    List<CartProductResponseDTO> wishlistProducts) throws JsonProcessingException {
	CartResponseDTO responseBody = CartResponseDTO.builder()
		.cartId(cartId)
		.customerId("customer-123")
		.customerName("Jane Doe")
		.products(products)
		.wishListProducts(wishlistProducts)
		.subtotal(49.99)
		.tvq(4.0)
		.tvc(2.0)
		.total(55.99)
		.paymentStatus("PENDING")
		.build();
	return objectMapper.writeValueAsString(responseBody);
    }

    private String cartErrorResponseJson(String message) throws JsonProcessingException {
	CartResponseDTO responseBody = CartResponseDTO.builder()
		.message(message)
		.build();
	return objectMapper.writeValueAsString(responseBody);
    }
}
