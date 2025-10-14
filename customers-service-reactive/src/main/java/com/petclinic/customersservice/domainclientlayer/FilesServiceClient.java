package com.petclinic.customersservice.domainclientlayer;

import com.petclinic.customersservice.customersExceptions.exceptions.BadRequestException;
import com.petclinic.customersservice.customersExceptions.exceptions.NotFoundException;
import com.petclinic.customersservice.customersExceptions.exceptions.UnprocessableEntityException;
import com.petclinic.customersservice.util.Rethrower;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private Rethrower rethrower;

    public FilesServiceClient(WebClient.Builder webClientBuilder, @Value("${app.files-service.host}") String filesServiceHost, @Value("${app.files-service.port}") String filesServicePort) {
        this.webClientBuilder = webClientBuilder;
        filesServiceUrl = "http://" + filesServiceHost + ":" + filesServicePort + "/files/";
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

    public Mono<FileResponseDTO> addFile(FileServiceRequestDTO fileDetails) {
        log.info("Sending file to Files Service URL: {}, fileName: {}, fileType: {}, fileData length: {}", 
                filesServiceUrl, fileDetails.getFileName(), fileDetails.getFileType(), 
                fileDetails.getFileData() != null ? fileDetails.getFileData().length() : 0);
        
        return webClientBuilder.build()
                .post()
                .uri(filesServiceUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(fileDetails))
                .retrieve()
                .onStatus(HttpStatus.UNPROCESSABLE_ENTITY::equals, resp -> {
                    log.error("Files Service returned 422 Unprocessable Entity");
                    return rethrower.rethrow(resp, ex -> new UnprocessableEntityException(ex.get("message").toString()));
                })
                .onStatus(HttpStatus.BAD_REQUEST::equals, resp -> {
                    log.error("Files Service returned 400 Bad Request");
                    return rethrower.rethrow(resp, ex -> new BadRequestException(ex.get("message").toString()));
                })
                .onStatus(HttpStatus.INTERNAL_SERVER_ERROR::equals, resp -> {
                    log.error("Files Service returned 500 Internal Server Error");
                    return rethrower.rethrow(resp, ex -> new RuntimeException(ex.get("message").toString()));
                })
                .bodyToMono(FileResponseDTO.class)
                .doOnSuccess(response -> log.info("Successfully received response from Files Service, fileId: {}", 
                        response != null ? response.getFileId() : "null"))
                .doOnError(error -> log.error("Error calling Files Service: {}", error.getMessage(), error));
    }

    public Mono<FileResponseDTO> updateFile(String fileId, FileServiceRequestDTO fileDetails) {
        return webClientBuilder.build()
                .put()
                .uri(filesServiceUrl + "/{fileId}", fileId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(fileDetails))
                .retrieve()
                .onStatus(HttpStatus.NOT_FOUND::equals, resp -> rethrower.rethrow(resp, ex -> new NotFoundException(ex.get("message").toString())))
                .onStatus(HttpStatus.UNPROCESSABLE_ENTITY::equals, resp -> rethrower.rethrow(resp, ex -> new UnprocessableEntityException(ex.get("message").toString())))
                .onStatus(HttpStatus.BAD_REQUEST::equals, resp -> rethrower.rethrow(resp, ex -> new BadRequestException(ex.get("message").toString())))
                .onStatus(HttpStatus.INTERNAL_SERVER_ERROR::equals, resp -> rethrower.rethrow(resp, ex -> new RuntimeException(ex.get("message").toString())))
                .bodyToMono(FileResponseDTO.class);
    }

    public Mono<Void> deleteFile(String fileId) {
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
