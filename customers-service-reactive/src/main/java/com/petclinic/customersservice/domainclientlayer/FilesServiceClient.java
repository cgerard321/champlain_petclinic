package com.petclinic.customersservice.domainclientlayer;

import com.petclinic.customersservice.customersExceptions.exceptions.BadRequestException;
import com.petclinic.customersservice.customersExceptions.exceptions.InvalidInputException;
import com.petclinic.customersservice.customersExceptions.exceptions.NotFoundException;
import com.petclinic.customersservice.util.Rethrower;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class FilesServiceClient {
    private final WebClient.Builder webClientBuilder;
    private final String filesServiceUrl;

    @Autowired
    private Rethrower rethrower;

    public FilesServiceClient(WebClient.Builder webClientBuilder, @Value("${app.files-service.host}") String filesServiceHost, @Value("${app.files-service.port}") String filesServicePort) {
        this.webClientBuilder = webClientBuilder;
        filesServiceUrl = "http://" + filesServiceHost + ":" + filesServicePort + "/files";
    }

    public Mono<FileResponseDTO> getFile(String fileId) {
        return webClientBuilder.build()
                .get()
                .uri(filesServiceUrl + "/{fileId}", fileId)
                .retrieve()
                .onStatus(HttpStatus.NOT_FOUND::equals, resp -> rethrower.rethrow(resp, ex -> new NotFoundException(ex.get("message").toString())))
                .onStatus(HttpStatus.BAD_REQUEST::equals, resp -> rethrower.rethrow(resp, ex -> new BadRequestException(ex.get("message").toString())))
                .onStatus(HttpStatus.INTERNAL_SERVER_ERROR::equals, resp -> rethrower.rethrow(resp, ex -> new RuntimeException(ex.get("message").toString())))
                .bodyToMono(FileResponseDTO.class);
    }

    public Mono<FileResponseDTO> AddFile(FileRequestDTO fileDetails) {
        return webClientBuilder.build()
                .post()
                .uri(filesServiceUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(fileDetails)
                .retrieve()
                .onStatus(HttpStatus.UNPROCESSABLE_ENTITY::equals, resp -> rethrower.rethrow(resp, ex -> new InvalidInputException(ex.get("message").toString())))
                .onStatus(HttpStatus.BAD_REQUEST::equals, resp -> rethrower.rethrow(resp, ex -> new BadRequestException(ex.get("message").toString())))
                .onStatus(HttpStatus.INTERNAL_SERVER_ERROR::equals, resp -> rethrower.rethrow(resp, ex -> new RuntimeException(ex.get("message").toString())))
                .bodyToMono(FileResponseDTO.class);
    }

    public Mono<FileResponseDTO> UpdateFile(String fileId, FileRequestDTO fileDetails) {
        return webClientBuilder.build()
                .put()
                .uri(filesServiceUrl + "/{fileId}", fileId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(fileDetails)
                .retrieve()
                .onStatus(HttpStatus.NOT_FOUND::equals, resp -> rethrower.rethrow(resp, ex -> new NotFoundException(ex.get("message").toString())))
                .onStatus(HttpStatus.UNPROCESSABLE_ENTITY::equals, resp -> rethrower.rethrow(resp, ex -> new InvalidInputException(ex.get("message").toString())))
                .onStatus(HttpStatus.BAD_REQUEST::equals, resp -> rethrower.rethrow(resp, ex -> new BadRequestException(ex.get("message").toString())))
                .onStatus(HttpStatus.INTERNAL_SERVER_ERROR::equals, resp -> rethrower.rethrow(resp, ex -> new RuntimeException(ex.get("message").toString())))
                .bodyToMono(FileResponseDTO.class);
    }

    public Mono<Void> DeleteFile(String fileId) {
        return webClientBuilder.build()
                .delete()
                .uri(filesServiceUrl + "/{fileId}", fileId)
                .retrieve()
                .onStatus(HttpStatus.NOT_FOUND::equals, resp -> rethrower.rethrow(resp, ex -> new NotFoundException(ex.get("message").toString())))
                .onStatus(HttpStatus.BAD_REQUEST::equals, resp -> rethrower.rethrow(resp, ex -> new BadRequestException(ex.get("message").toString())))
                .onStatus(HttpStatus.INTERNAL_SERVER_ERROR::equals, resp -> rethrower.rethrow(resp, ex -> new RuntimeException(ex.get("message").toString())))
                .bodyToMono(Void.class);
    }
}
