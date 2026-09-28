package com.petclinic.products.domainclientlayer;


import com.petclinic.products.utils.exceptions.BadRequestException;
import com.petclinic.products.utils.exceptions.FailedDependencyException;
import com.petclinic.products.utils.exceptions.FileNotFoundInFilesServiceException;
import com.petclinic.products.utils.exceptions.UnprocessableEntityException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.codec.DecodingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
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
                .onStatus(HttpStatus.NOT_FOUND::equals, resp -> Mono.error(new FailedDependencyException("Failed to get file from Files Service")))
                .onStatus(HttpStatus.BAD_REQUEST::equals, resp -> Mono.error(new BadRequestException("Invalid File Request Model")))
                .onStatus(HttpStatusCode::isError, resp -> Mono.error(new FailedDependencyException("Failed to get file from Files Service")))
                .bodyToMono(FileResponseDTO.class)
                .transform(response -> requireValidFileResponse(response, "get"))
                .transform(response -> mapTransportErrors(response, "get"));
    }

    public Mono<FileResponseDTO> addFile(FileRequestDTO fileDetails) {
        log.info("Sending file to Files Service URL: {}, fileName: {}, fileType: {}, fileData length: {}",
                filesServiceUrl, fileDetails.getFileName(), fileDetails.getFileType(),
                fileDetails.getFileData() != null ? fileDetails.getFileData().length : 0);

        return webClientBuilder.build()
                .post()
                .uri(filesServiceUrl + "/")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(fileDetails))
                .retrieve()
                .onStatus(HttpStatus.UNPROCESSABLE_ENTITY::equals, resp -> Mono.error(new UnprocessableEntityException("Unprocessable File Request Model")))
                .onStatus(HttpStatus.BAD_REQUEST::equals, resp -> Mono.error(new BadRequestException("Invalid File Request Model")))
                .onStatus(HttpStatusCode::isError, response -> Mono.error(
                        new FailedDependencyException("Failed to upload file")))
                .bodyToMono(FileResponseDTO.class)
                .transform(response -> requireValidFileResponse(response, "upload"))
                .transform(response -> mapTransportErrors(response, "upload"))
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
                .onStatus(
                        HttpStatus.NOT_FOUND::equals,
                        response -> Mono.error(
                                new FileNotFoundInFilesServiceException(
                                        "File was not found in Files Service")))
                .onStatus(HttpStatus.UNPROCESSABLE_ENTITY::equals, resp -> Mono.error(new UnprocessableEntityException("Unprocessable File Request Model")))
                .onStatus(HttpStatus.BAD_REQUEST::equals, resp -> Mono.error(new BadRequestException("Invalid File Request Model")))
                .onStatus(HttpStatusCode::isError, resp -> Mono.error(new FailedDependencyException("Failed to update file from Files Service")))
                .bodyToMono(FileResponseDTO.class)
                .transform(response -> requireValidFileResponse(response, "update"))
                .transform(response -> mapTransportErrors(response, "update"));
    }

    public Mono<Void> deleteFile(String fileId) {
        return webClientBuilder.build()
                .delete()
                .uri(filesServiceUrl + "/{fileId}", fileId)
                .retrieve()
                .onStatus(
                        HttpStatus.NOT_FOUND::equals,
                        response -> Mono.empty())
                .onStatus(HttpStatus.BAD_REQUEST::equals, resp -> Mono.error(new BadRequestException("Invalid File Request Model")))
                .onStatus(HttpStatusCode::isError, resp -> Mono.error(new FailedDependencyException("Failed to delete file from Files Service")))
                .bodyToMono(Void.class)
                .transform(response -> mapTransportErrors(response, "delete"));
    }

    private Mono<FileResponseDTO> requireValidFileResponse(
            Mono<FileResponseDTO> response, String operation) {
        return response
                .filter(file -> file.getFileId() != null && !file.getFileId().isBlank())
                .switchIfEmpty(Mono.error(new FailedDependencyException(
                        "Files Service returned an invalid response during " + operation)));
    }

    private <T> Mono<T> mapTransportErrors(Mono<T> response, String operation) {
        return response
                .onErrorMap(
                        DecodingException.class,
                        error -> new FailedDependencyException(
                                "Files Service returned an invalid response during " + operation))
                .onErrorMap(
                        WebClientException.class,
                        error -> new FailedDependencyException(
                                "Files Service is unavailable during " + operation));
    }
}
