package com.petclinic.bffapigateway.presentationlayer.v2;



import com.petclinic.bffapigateway.domainclientlayer.EmailingServiceClient;
import com.petclinic.bffapigateway.dtos.Emailing.DirectEmailModelRequestDTO;
import com.petclinic.bffapigateway.dtos.Emailing.EmailModelResponseDTO;
import com.petclinic.bffapigateway.utils.Security.Annotations.SecuredEndpoint;
import com.petclinic.bffapigateway.utils.Security.Variables.Roles;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v2/gateway/emailing")
@Validated
@CrossOrigin(origins = "http://localhost:3000, http://localhost:80")
public class EmailingController {

    private final EmailingServiceClient emailingService;

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @GetMapping(value = "", produces= MediaType.APPLICATION_JSON_VALUE)
    public Flux<EmailModelResponseDTO> getAllEmails() {
        return emailingService.getAllEmails();
    }


    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @PostMapping(
            value = "/template/{templateName}",
            consumes= MediaType.TEXT_HTML_VALUE,
            produces= MediaType.APPLICATION_JSON_VALUE
    )
    public Mono<String> sendTemplate(@PathVariable String templateName, @RequestBody String body) {
        return emailingService.addHtmlTemplate(templateName, body);
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @PostMapping(
            value = "/send",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public Mono<HttpStatus> sendEmail(@RequestBody DirectEmailModelRequestDTO body) {
        return emailingService.sendEmail(body)
                .map(status -> {
                    // Here, you can handle the status code returned from the emailing service
                    return status; // This returns the HTTP status code
                })
                .onErrorResume(e -> {
                    // Handle any exceptions that may occur
                    return Mono.just(HttpStatus.INTERNAL_SERVER_ERROR); // or another appropriate status
                });
    }



    /*
    * public Mono<String> sendEmail(DirectEmailModelRequestDTO directEmailModelRequestDTO) {
        return webClientBuilder.build().post()
                .uri(emailingServiceUrl + "/send")
                .bodyValue(directEmailModelRequestDTO) // Send HTML content in the body
                .retrieve()
                .bodyToMono(String.class)
                .switchIfEmpty(Mono.error(new RuntimeException("No response from service")));
    }
    * */

}
