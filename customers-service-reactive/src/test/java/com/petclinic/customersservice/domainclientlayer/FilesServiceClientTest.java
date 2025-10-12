package com.petclinic.customersservice.domainclientlayer;

import com.petclinic.customersservice.customersExceptions.exceptions.BadRequestException;
import com.petclinic.customersservice.customersExceptions.exceptions.InvalidInputException;
import com.petclinic.customersservice.customersExceptions.exceptions.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FilesServiceClientTest {

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.Builder webClientBuilder;
    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;
    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;
    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;
    @Mock
    private WebClient.ResponseSpec responseSpec;

    @Mock
    private FilesServiceRethrower rethrower;

    @InjectMocks
    private FilesServiceClient filesServiceClient;

    @BeforeEach
    void setUp() {
        when(webClientBuilder.build()).thenReturn(webClient);
        filesServiceClient = new FilesServiceClient(webClientBuilder, "test-host", "test-port");
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
    }

    @Test
    void testGetFile_WhenApiFails_ShouldRethrowAsNotFoundException() {
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        doThrow(new NotFoundException("File not found")).when(rethrower).rethrow(any(), any());

        doAnswer(invocation -> {
            try {
                rethrower.rethrow(null, null);
                return Mono.empty();
            } catch (Throwable t) {
                return Mono.error(t);
            }
        }).when(responseSpec).bodyToMono(FileResponseDTO.class);

        StepVerifier.create(filesServiceClient.getFile("missing"))
                .expectError(NotFoundException.class)
                .verify();
        verify(rethrower, times(1)).rethrow(any(), any());
    }

    @Test
    void testAddFile_WhenApiFails_ShouldRethrowAsBadRequestException() {
        FileRequestDTO requestDTO = new FileRequestDTO();

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        doThrow(new BadRequestException("Bad request")).when(rethrower).rethrow(any(), any());

        doAnswer(invocation -> {
            try {
                rethrower.rethrow(null, null);
                return Mono.empty();
            } catch (Throwable t) {
                return Mono.error(t);
            }
        }).when(responseSpec).bodyToMono(FileResponseDTO.class);

        StepVerifier.create(filesServiceClient.AddFile(requestDTO))
                .expectError(BadRequestException.class)
                .verify();
        verify(rethrower, times(1)).rethrow(any(), any());
    }

    @Test
    void testUpdateFile_WhenApiFails_ShouldRethrowAsInvalidInputException() {
        FileRequestDTO requestDTO = new FileRequestDTO();

        when(webClient.put()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString(), anyString())).thenReturn((WebClient.RequestBodySpec) requestBodyUriSpec);
        when(requestBodyUriSpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        doThrow(new InvalidInputException("Invalid input")).when(rethrower).rethrow(any(), any());
        doAnswer(invocation -> {
            try {
                rethrower.rethrow(null, null);
                return Mono.empty();
            } catch (Throwable t) {
                return Mono.error(t);
            }
        }).when(responseSpec).bodyToMono(FileResponseDTO.class);

        StepVerifier.create(filesServiceClient.UpdateFile("file1", requestDTO))
                .expectError(InvalidInputException.class)
                .verify();
        verify(rethrower, times(1)).rethrow(any(), any());
    }

    @Test
    void testDeleteFile_WhenApiFails_ShouldRethrowAsGenericRuntimeException() {
        when(webClient.delete()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        doThrow(new RuntimeException("Server error")).when(rethrower).rethrow(any(), any());

        doAnswer(invocation -> {
            try {
                rethrower.rethrow(null, null);
                return Mono.empty();
            } catch (Throwable t) {
                return Mono.error(t);
            }
        }).when(responseSpec).bodyToMono(Void.class);

        StepVerifier.create(filesServiceClient.DeleteFile("file1"))
                .expectError(RuntimeException.class)
                .verify();
        verify(rethrower, times(1)).rethrow(any(), any());
    }

    @Test
    void testGetFile_ShouldReturnFileSuccessfully() {
        FileResponseDTO expectedResponse = new FileResponseDTO();

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        when(responseSpec.bodyToMono(FileResponseDTO.class)).thenReturn(Mono.just(expectedResponse));

        StepVerifier.create(filesServiceClient.getFile("file1"))
                .expectNext(expectedResponse)
                .verifyComplete();
    }

    @Test
    void testAddFile_ShouldCreateFileSuccessfully() {
        FileRequestDTO requestDTO = new FileRequestDTO();
        FileResponseDTO expectedResponse = new FileResponseDTO();

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        when(responseSpec.bodyToMono(FileResponseDTO.class)).thenReturn(Mono.just(expectedResponse));

        StepVerifier.create(filesServiceClient.AddFile(requestDTO))
                .expectNext(expectedResponse)
                .verifyComplete();
    }

    @Test
    void testUpdateFile_ShouldUpdateFileSuccessfully() {
        FileRequestDTO requestDTO = new FileRequestDTO();
        FileResponseDTO expectedResponse = new FileResponseDTO();

        when(webClient.put()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString(), anyString())).thenReturn((WebClient.RequestBodySpec) requestBodyUriSpec);
        when(requestBodyUriSpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        when(responseSpec.bodyToMono(FileResponseDTO.class)).thenReturn(Mono.just(expectedResponse));

        StepVerifier.create(filesServiceClient.UpdateFile("file1", requestDTO))
                .expectNext(expectedResponse)
                .verifyComplete();
    }

    @Test
    void testDeleteFile_ShouldCompleteSuccessfully() {
        when(webClient.delete()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        when(responseSpec.bodyToMono(Void.class)).thenReturn(Mono.empty());

        StepVerifier.create(filesServiceClient.DeleteFile("file1"))
                .verifyComplete();
    }
}
