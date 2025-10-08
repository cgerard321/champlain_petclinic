package com.petclinic.customersservice.domainclientlayer;

import com.petclinic.customersservice.customersExceptions.exceptions.InvalidInputException;
import com.petclinic.customersservice.customersExceptions.exceptions.NotFoundException;
import lombok.extern.slf4j.Slf4j;
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

    public FilesServiceClient(WebClient.Builder webClientBuilder, @Value("${app.files-service.host}") String filesServiceHost, @Value("${app.files-service.port}") String filesServicePort) {
        this.webClientBuilder = webClientBuilder;
        filesServiceUrl = "http://" + filesServiceHost + ":" + filesServicePort + "/files";
    }

    public Mono<FileResponseDTO> getFile(String fileId) {
        return webClientBuilder.build()
                .get()
                .uri(filesServiceUrl + "/{fileId}", fileId)
                .retrieve()
                .onStatus(HttpStatus.NOT_FOUND::equals, response -> Mono.error(new NotFoundException("File not found: " + fileId)))
                .onStatus(HttpStatus.UNPROCESSABLE_ENTITY::equals, response -> Mono.error(new InvalidInputException("Invalid file ID: " + fileId)))
                .onStatus(HttpStatus.INTERNAL_SERVER_ERROR::equals, response ->
                        response.bodyToMono(String.class)
                            .flatMap(errorBody -> Mono.error(
                                    new RuntimeException(errorBody)
                            ))
                )
                .bodyToMono(FileResponseDTO.class);
    }

    public Mono<FileResponseDTO> AddFile(FileRequestDTO fileDetails) {
        return webClientBuilder.build()
                .post()
                .uri(filesServiceUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(fileDetails)
                .retrieve()
                .onStatus(HttpStatus.UNPROCESSABLE_ENTITY::equals, response ->
                        response.bodyToMono(String.class)
                                .flatMap(errorBody -> Mono.error(
                                        new InvalidInputException(errorBody)
                                ))
                )
                .onStatus(HttpStatus.INTERNAL_SERVER_ERROR::equals, response ->
                        response.bodyToMono(String.class)
                                .flatMap(errorBody -> Mono.error(
                                        new RuntimeException(errorBody)
                                ))
                )
                .bodyToMono(FileResponseDTO.class);
    }

    public Mono<FileResponseDTO> UpdateFile(String fileId, FileRequestDTO fileDetails) {
        return webClientBuilder.build()
                .put()
                .uri(filesServiceUrl + "/{fileId}", fileId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(fileDetails)
                .retrieve()
                .onStatus(HttpStatus.NOT_FOUND::equals, response -> Mono.error(new NotFoundException("File not found: " + fileId)))
                .onStatus(HttpStatus.UNPROCESSABLE_ENTITY::equals, response ->
                        response.bodyToMono(String.class)
                                .flatMap(errorBody -> Mono.error(
                                        new InvalidInputException(errorBody)
                                ))
                )
                .onStatus(HttpStatus.INTERNAL_SERVER_ERROR::equals, response ->
                        response.bodyToMono(String.class)
                                .flatMap(errorBody -> Mono.error(
                                        new RuntimeException(errorBody)
                                ))
                )
                .bodyToMono(FileResponseDTO.class);
    }

    public Mono<Void> DeleteFile(String fileId) {
        return webClientBuilder.build()
                .delete()
                .uri(filesServiceUrl + "/{fileId}", fileId)
                .retrieve()
                .onStatus(HttpStatus.NOT_FOUND::equals, response -> Mono.error(new NotFoundException("File not found: " + fileId)))
                .onStatus(HttpStatus.INTERNAL_SERVER_ERROR::equals, response ->
                        response.bodyToMono(String.class)
                                .flatMap(errorBody -> Mono.error(
                                        new RuntimeException(errorBody)
                                ))
                )
                .bodyToMono(Void.class);
    }
}
