package com.petclinic.bffapigateway.domainclientlayer;

import com.petclinic.bffapigateway.dtos.files.FileResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
        filesServiceUrl = "http://" + filesServiceHost + ":" + filesServicePort + "/files/";
    }

    public Mono<FileResponseDTO> getFile(String fileId) { //shouldn't it also return the responseEntity?
        return webClientBuilder.build().get()
                .uri(filesServiceUrl + fileId)
                .retrieve()
                .bodyToMono(FileResponseDTO.class);
    }
}
