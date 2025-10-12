package com.petclinic.customersservice.domainclientlayer;

import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

public class FilesServiceClient {

    private final WebClient webClient;
    private final FilesServiceRethrower rethrower;
    private final String FILES_SERVICE_BASE_URL = "/api/v1/files";

    public FilesServiceClient(WebClient webClient, FilesServiceRethrower rethrower) {
        this.webClient = webClient;
        this.rethrower = rethrower;
    }

    public Mono<FileResponseDTO> getFile(String fileId) {
        return webClient.get()
                .uri(FILES_SERVICE_BASE_URL + "/{fileId}", fileId)
                .retrieve()
                .bodyToMono(FileResponseDTO.class)
                .onErrorMap(throwable -> rethrower.rethrow(throwable, "getFile"));
    }

    public Mono<FileResponseDTO> AddFile(FileRequestDTO requestDTO) {
        return webClient.post()
                .uri(FILES_SERVICE_BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDTO)
                .retrieve()
                .bodyToMono(FileResponseDTO.class)
                .onErrorMap(throwable -> rethrower.rethrow(throwable, "AddFile"));
    }

    public Mono<FileResponseDTO> UpdateFile(String fileId, FileRequestDTO requestDTO) {
        return webClient.put()
                .uri(FILES_SERVICE_BASE_URL + "/{fileId}", fileId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDTO)
                .retrieve()
                .bodyToMono(FileResponseDTO.class)
                .onErrorMap(throwable -> rethrower.rethrow(throwable, "UpdateFile"));
    }

    public Mono<Void> DeleteFile(String fileId) {
        return webClient.delete()
                .uri(FILES_SERVICE_BASE_URL + "/{fileId}", fileId)
                .retrieve()
                .bodyToMono(Void.class)
                .onErrorMap(throwable -> rethrower.rethrow(throwable, "DeleteFile"))
                .then();
    }
}
