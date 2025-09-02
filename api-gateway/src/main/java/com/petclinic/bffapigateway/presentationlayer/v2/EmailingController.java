package com.petclinic.bffapigateway.presentationlayer.v2;



import com.petclinic.bffapigateway.domainclientlayer.EmailingServiceClient;
import com.petclinic.bffapigateway.dtos.Emailing.DirectEmailModelRequestDTO;
import com.petclinic.bffapigateway.dtos.Emailing.EmailModelResponseDTO;
import com.petclinic.bffapigateway.dtos.Emailing.NotificationEmailModelRequestDTO;
import com.petclinic.bffapigateway.dtos.Emailing.RawEmailModelRequestDTO;
import com.petclinic.bffapigateway.utils.Security.Annotations.SecuredEndpoint;
import com.petclinic.bffapigateway.utils.Security.Variables.Roles;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v2/gateway/emailing")
@Validated
public class EmailingController {

    private final EmailingServiceClient emailingService;

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<?>> getAllEmails() {
        return emailingService.getAllEmails()
                .collectList() // Collect into a list for checking if empty
                .flatMap(emails -> {
                    if (emails.isEmpty()) {
                        return Mono.just(ResponseEntity.noContent().build()); // Return 204 No Content
                    }
                    return Mono.just(ResponseEntity.ok(Flux.fromIterable(emails))); // Return 200 OK with emails
                })
                .onErrorResume(e -> {
                    log.error("Error retrieving emails", e);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()); // Handle errors
                });
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @PostMapping(
            value = "/template/{templateName}",
            consumes = MediaType.TEXT_HTML_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Mono<ResponseEntity<Object>> sendTemplate(@PathVariable String templateName, @RequestBody String body) {
        return emailingService.addHtmlTemplate(templateName, body)
                .map(result -> ResponseEntity.status(HttpStatus.CREATED).build()) // Return 201 Created
                .onErrorResume(e -> {
                    log.error("Error sending template", e);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()); // Handle errors
                });
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @PostMapping(
            value = "/send",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public Mono<ResponseEntity<Object>> sendEmail(@RequestBody DirectEmailModelRequestDTO body) {
        return emailingService.sendEmail(body)
                .map(result -> ResponseEntity.status(result).build()) // Return 200 OK
                .onErrorResume(e -> {
                    log.error("Error sending email", e);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()); // Handle errors
                });
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @PostMapping(
            value = "/send/notification",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public Mono<ResponseEntity<Object>> sendEmailNotification(@RequestBody NotificationEmailModelRequestDTO body) {
        return emailingService.sendEmailNotification(body)
                .map(result -> ResponseEntity.status(result.value()).build()) // Return 200 OK
                .onErrorResume(e -> {
                    log.error("Error sending email notification", e);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()); // Handle errors
                });
    }
    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @PostMapping(
            value = "/send/raw",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public Mono<ResponseEntity<Object>> sendRawEmail(@RequestBody RawEmailModelRequestDTO body) {
        return emailingService.sendRawEmail(body)
                .map(result -> ResponseEntity.status(result.value()).build()) // Return 200 OK
                .onErrorResume(e -> {
                    log.error("Error sending email notification", e);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()); // Handle errors
                });
    }

}
