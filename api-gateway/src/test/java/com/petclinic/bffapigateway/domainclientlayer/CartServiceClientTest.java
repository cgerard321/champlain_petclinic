package com.petclinic.bffapigateway.domainclientlayer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petclinic.bffapigateway.dtos.Cart.CartRequestDTO;
import com.petclinic.bffapigateway.dtos.Cart.CartResponseDTO;
import com.petclinic.bffapigateway.dtos.CustomerDTOs.OwnerResponseDTO;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CartServiceClientTest {

    @MockBean
    CartServiceClient mockCartServiceClient;

    MockWebServer mockWebServer;

    @BeforeEach
    void setUp(){

        mockWebServer = new MockWebServer();
        mockCartServiceClient = new CartServiceClient(
                WebClient.builder(),
                mockWebServer.getHostName(),
                String.valueOf(mockWebServer.getPort())
        );
    }

    @AfterEach
    void shutdown() throws IOException {
        this.mockWebServer.shutdown();
    }

    @Test
    void testCreateCart(){

        String body = """
                  {
                    "cartId":"98f7b33a-d62a-420a-a84a-05a27c85fc91"
                  }
                """;

        CartRequestDTO cartRequestDTO = new CartRequestDTO();
        cartRequestDTO.setCustomerId("12345");

        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setBody(body));

        final CartResponseDTO cartResponseDTO = mockCartServiceClient.createCart(cartRequestDTO);
        assertEquals(cartResponseDTO.getCartId(),"98f7b33a-d62a-420a-a84a-05a27c85fc91");

    }


    private void prepareResponse(Consumer<MockResponse> consumer) {
        MockResponse response = new MockResponse();
        consumer.accept(response);
        this.mockWebServer.enqueue(response);
    }

}
