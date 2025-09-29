package com.petclinic.bffapigateway.presentationlayer.v1;

import com.petclinic.bffapigateway.domainclientlayer.FilesServiceClient;
import com.petclinic.bffapigateway.dtos.files.FileResponseDTO;
import com.petclinic.bffapigateway.utils.Security.Annotations.SecuredEndpoint;
import com.petclinic.bffapigateway.utils.Security.Variables.Roles;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController()
@RequiredArgsConstructor
@Slf4j
@RequestMapping("api/gateway/files")
public class FileControllerV1 {

    private final FilesServiceClient filesServiceClient;

    @SecuredEndpoint(allowedRoles = {Roles.ANONYMOUS})
    @GetMapping("{fileId}")
    public Mono<ResponseEntity<FileResponseDTO>> getFile(@PathVariable String fileId) {
        return filesServiceClient.getFile(fileId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
