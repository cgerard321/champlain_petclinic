package com.petclinic.customersservice.domainclientlayer;

import com.petclinic.customersservice.customersExceptions.exceptions.BadRequestException;
import com.petclinic.customersservice.customersExceptions.exceptions.FailedDependencyException;
import com.petclinic.customersservice.customersExceptions.exceptions.UnprocessableEntityException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class FilesServiceClient {
    private final WebClient.Builder webClientBuilder;
    private final String filesServiceUrl;

    public FilesServiceClient(WebClient.Builder webClientBuilder, @Value("${app.files-service.host}") String filesServiceHost, @Value("${app.files-service.port}") String filesServicePort) {
        this.webClientBuilder = webClientBuilder;
        filesServiceUrl = "http://" + filesServiceHost + ":" + filesServicePort + "/files/";
    }

    public Mono<FileResponseDTO> getFile(String fileId) {
        return webClientBuilder.build()
                .get()
                .uri(filesServiceUrl + "/{fileId}", fileId)
                .retrieve()
                .onStatus(HttpStatus.NOT_FOUND::equals, resp -> Mono.error(new FailedDependencyException("Failed to get file from Files Service")))
                .onStatus(HttpStatus.BAD_REQUEST::equals, resp -> Mono.error(new BadRequestException("Invalid File Request Model")))
                .onStatus(HttpStatus.INTERNAL_SERVER_ERROR::equals, resp -> Mono.error(new FailedDependencyException("Failed to get file from Files Service")))
                .bodyToMono(FileResponseDTO.class);
    }

    public Mono<FileResponseDTO> addFile(FileRequestDTO fileDetails) {
        log.info("Sending file to Files Service URL: {}, fileName: {}, fileType: {}, fileData length: {}",
                filesServiceUrl, fileDetails.getFileName(), fileDetails.getFileType(),
                fileDetails.getFileData() != null ? fileDetails.getFileData().length : 0);

        return webClientBuilder.build()
                .post()
                .uri(filesServiceUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(fileDetails))
                .retrieve()
                .onStatus(HttpStatus.UNPROCESSABLE_ENTITY::equals, resp -> Mono.error(new UnprocessableEntityException("Unprocessable File Request Model")))
                .onStatus(HttpStatus.BAD_REQUEST::equals, resp -> Mono.error(new BadRequestException("Invalid File Request Model")))
                .onStatus(HttpStatus.INTERNAL_SERVER_ERROR::equals, resp -> Mono.error(new FailedDependencyException("Failed to get file from Files Service")))
                .bodyToMono(FileResponseDTO.class)
                .doOnSuccess(response -> log.info("Successfully received response from Files Service, fileId: {}",
                        response != null ? response.getFileId() : "null"))
                .doOnError(error -> log.error("Error calling Files Service: {}", error.getMessage(), error));
    }

    public Mono<FileResponseDTO> updateFile(String fileId, FileRequestDTO fileDetails) {
        return webClientBuilder.build()
                .put()
                .uri(filesServiceUrl + "/{fileId}", fileId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(fileDetails))
                .retrieve()
                .onStatus(HttpStatus.NOT_FOUND::equals, resp -> Mono.error(new FailedDependencyException("Failed to get file from Files Service")))
                .onStatus(HttpStatus.UNPROCESSABLE_ENTITY::equals, resp -> Mono.error(new UnprocessableEntityException("Unprocessable File Request Model")))
                .onStatus(HttpStatus.BAD_REQUEST::equals, resp -> Mono.error(new BadRequestException("Invalid File Request Model")))
                .onStatus(HttpStatus.INTERNAL_SERVER_ERROR::equals, resp -> Mono.error(new FailedDependencyException("Failed to get file from Files Service")))
                .bodyToMono(FileResponseDTO.class);
    }

    public Mono<Void> deleteFile(String fileId) {
        return webClientBuilder.build()
                .delete()
                .uri(filesServiceUrl + "/{fileId}", fileId)
                .retrieve()
                .onStatus(HttpStatus.NOT_FOUND::equals, resp -> Mono.error(new FailedDependencyException("Failed to get file from Files Service")))
                .onStatus(HttpStatus.BAD_REQUEST::equals, resp -> Mono.error(new BadRequestException("Invalid File Request Model")))
                .onStatus(HttpStatus.INTERNAL_SERVER_ERROR::equals, resp -> Mono.error(new FailedDependencyException("Failed to get file from Files Service")))
                .bodyToMono(Void.class);
    }
}