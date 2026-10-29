package com.petclinic.cartsservice.domainclientlayer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Component
public class FilesServiceClient {
    private final WebClient webClient;

    public FilesServiceClient(WebClient.Builder builder,
            @Value("${app.files-service.host}") String host,
            @Value("${app.files-service.port}") String port) {
        webClient = builder.clone().baseUrl("http://" + host + ":" + port + "/files")
                .codecs(config -> config.defaultCodecs().maxInMemorySize(2 * 1024 * 1024)).build();
    }

    public Mono<FileResponseDTO> getFile(String fileId) {
        return webClient.get().uri("/{fileId}", fileId).retrieve()
                .bodyToMono(FileResponseDTO.class);
    }

    public Mono<FileResponseDTO> addFile(FileRequestDTO file) {
        return webClient.post().uri("/").contentType(MediaType.APPLICATION_JSON)
                .bodyValue(file).retrieve().bodyToMono(FileResponseDTO.class)
                .filter(response -> response.getFileId() != null && !response.getFileId().isBlank())
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.FAILED_DEPENDENCY,
                        "Files Service returned an invalid upload response")))
                .onErrorMap(WebClientException.class, error -> new ResponseStatusException(
                        HttpStatus.FAILED_DEPENDENCY, "Could not upload promotion image", error));
    }

    public Mono<Void> deleteFile(String fileId) {
        return webClient.delete().uri("/{fileId}", fileId).retrieve()
                .onStatus(HttpStatus.NOT_FOUND::equals, response -> Mono.empty())
                .bodyToMono(Void.class);
    }
}
