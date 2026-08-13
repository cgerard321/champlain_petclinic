package com.petclinic.bffapigateway.presentationlayer.v2.Emailing;

import com.petclinic.bffapigateway.domainclientlayer.EmailingServiceClient;
import com.petclinic.bffapigateway.dtos.Emailing.DirectEmailModelRequestDTO;
import com.petclinic.bffapigateway.dtos.Emailing.EmailModelResponseDTO;
import com.petclinic.bffapigateway.dtos.Emailing.NotificationEmailModelRequestDTO;
import com.petclinic.bffapigateway.dtos.Emailing.RawEmailModelRequestDTO;
import com.petclinic.bffapigateway.presentationlayer.v2.EmailingController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.reactive.server.WebTestClient.bindToController;

class EmailingControllerIntegrationTest {

    @Mock
    private EmailingServiceClient emailingService;

    @InjectMocks
    private EmailingController emailingController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllEmails() {
        // Mock the service response
        EmailModelResponseDTO email1 = new EmailModelResponseDTO(/* initialize fields */);
        EmailModelResponseDTO email2 = new EmailModelResponseDTO(/* initialize fields */);
        when(emailingService.getAllEmails()).thenReturn(Flux.just(email1, email2));

        // Execute the test
        bindToController(emailingController).build()
                .get()
                .uri("/api/v2/gateway/emailing")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()  // Check if the status is 200 OK
                .expectBodyList(EmailModelResponseDTO.class)  // Validate response body
                .hasSize(2)  // Check the size of the response list
                .contains(email1, email2);  // Validate the contents of the response
    }

    @Test
    void testGetAllEmails_NoContent() {
        // Mock the service response to return an empty Flux
        when(emailingService.getAllEmails()).thenReturn(Flux.empty());

        // Execute the test
        bindToController(emailingController).build()
                .get()
                .uri("/api/v2/gateway/emailing")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent(); // Expect 204 No Content
    }

    @Test
    void testSendTemplate() {
        String templateName = "testTemplate";
        String body = "<html>...</html>";

        // Mock the service response
        when(emailingService.addHtmlTemplate(eq(templateName), eq(body))).thenReturn(Mono.just("Template added"));

        // Execute the test
        bindToController(emailingController).build()
                .post()
                .uri("/api/v2/gateway/emailing/template/{templateName}", templateName)
                .contentType(MediaType.TEXT_HTML)
                .body(BodyInserters.fromValue(body))
                .exchange()
                .expectStatus().isCreated(); // Expect 201 Created
    }

    @Test
    void testSendEmail() {
        DirectEmailModelRequestDTO requestDTO = new DirectEmailModelRequestDTO(/* initialize fields */);

        // Mock the service response
        when(emailingService.sendEmail(requestDTO)).thenReturn(Mono.just(HttpStatus.OK));

        // Execute the test
        bindToController(emailingController).build()
                .post()
                .uri("/api/v2/gateway/emailing/send")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(requestDTO))
                .exchange()
                .expectStatus().isOk(); // Expect 200 OK
    }

    @Test
    void testSendEmailNotification() {
        NotificationEmailModelRequestDTO requestDTO = new NotificationEmailModelRequestDTO(/* initialize fields */);

        // Mock the service response
        when(emailingService.sendEmailNotification(requestDTO)).thenReturn(Mono.just(HttpStatus.OK));

        // Execute the test
        bindToController(emailingController).build()
                .post()
                .uri("/api/v2/gateway/emailing/send/notification")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(requestDTO))
                .exchange()
                .expectStatus().isOk(); // Expect 200 OK
    }
    @Test
    void testSendRawEmail() {
        RawEmailModelRequestDTO requestDTO = new RawEmailModelRequestDTO(/* initialize fields */);

        // Mock the service response
        when(emailingService.sendRawEmail(requestDTO)).thenReturn(Mono.just(HttpStatus.OK));

        // Execute the test
        bindToController(emailingController).build()
                .post()
                .uri("/api/v2/gateway/emailing/send/raw")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(requestDTO))
                .exchange()
                .expectStatus().isOk(); // Expect 200 OK
    }

    @Test
    void testGetAllEmails_Error() {
        when(emailingService.getAllEmails()).thenReturn(Flux.error(new RuntimeException("Service error")));

        bindToController(emailingController).build()
                .get()
                .uri("/api/v2/gateway/emailing")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void testSendTemplate_Error() {
        String templateName = "testTemplate";
        String body = "<html>...</html>";

        when(emailingService.addHtmlTemplate(eq(templateName), eq(body)))
                .thenReturn(Mono.error(new RuntimeException("Service error")));

        bindToController(emailingController).build()
                .post()
                .uri("/api/v2/gateway/emailing/template/{templateName}", templateName)
                .contentType(MediaType.TEXT_HTML)
                .body(BodyInserters.fromValue(body))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void testSendEmail_Error() {
        DirectEmailModelRequestDTO requestDTO = new DirectEmailModelRequestDTO(/* initialize fields */);

        when(emailingService.sendEmail(requestDTO))
                .thenReturn(Mono.error(new RuntimeException("Service error")));

        bindToController(emailingController).build()
                .post()
                .uri("/api/v2/gateway/emailing/send")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(requestDTO))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void testSendEmailNotification_Error() {
        NotificationEmailModelRequestDTO requestDTO = new NotificationEmailModelRequestDTO(/* initialize fields */);

        when(emailingService.sendEmailNotification(requestDTO))
                .thenReturn(Mono.error(new RuntimeException("Service error")));

        bindToController(emailingController).build()
                .post()
                .uri("/api/v2/gateway/emailing/send/notification")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(requestDTO))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void testSendRawEmail_Error() {
        RawEmailModelRequestDTO requestDTO = new RawEmailModelRequestDTO(/* initialize fields */);

        when(emailingService.sendRawEmail(requestDTO))
                .thenReturn(Mono.error(new RuntimeException("Service error")));

        bindToController(emailingController).build()
                .post()
                .uri("/api/v2/gateway/emailing/send/raw")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(requestDTO))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}