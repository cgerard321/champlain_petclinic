package com.petclinic.customersservice.presentationlayer;

import com.petclinic.customersservice.business.CustomerService;
import com.petclinic.customersservice.data.Customer;
import com.petclinic.customersservice.domainclientlayer.FileResponseDTO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class CustomerControllerUnitTest {

    @Mock
    private CustomerService customerService;

    @InjectMocks
    private CustomerController customerController;

    private final String testCustomerId = "f9b46d32-0951-420b-afe6-22a738d97d9b";
    private Customer mockCustomer;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this).close();

        mockCustomer = new Customer();
        mockCustomer.setCustomerId(testCustomerId);
        mockCustomer.setFirstName("John");
        mockCustomer.setPhotoId("11aef324-15b4-409d-8078-86d22e38cde4");
    }

    @Test
    void getCustomerByCustomerId_ShouldReturnCustomerWithoutPhoto_WhenIncludePhotoFalse() {
        CustomerResponseDTO mockResponse = new CustomerResponseDTO();
        mockResponse.setCustomerId(testCustomerId);
        mockResponse.setFirstName("John");

        doReturn(Mono.just(mockResponse)).when(customerService).getCustomerByCustomerId(testCustomerId, false);

        Mono<ResponseEntity<CustomerResponseDTO>> result = customerController.getCustomerByCustomerId(testCustomerId, false);

        StepVerifier.create(result)
                .consumeNextWith(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertNotNull(response.getBody());
                    CustomerResponseDTO body = response.getBody();
                    assertNotNull(body);
                    assertEquals(testCustomerId, body.getCustomerId());
                    assertEquals("John", body.getFirstName());
                })
                .verifyComplete();
        verify(customerService, times(1)).getCustomerByCustomerId(testCustomerId, false);
    }

    @Test
    void getCustomerByCustomerId_ShouldReturnCustomerWithPhoto_WhenIncludePhotoTrue() {
        byte[] imageData = "custom-image-data".getBytes();
        CustomerResponseDTO mockResponse = new CustomerResponseDTO();
        mockResponse.setCustomerId(testCustomerId);
        mockResponse.setFirstName("John");
        FileResponseDTO photo = FileResponseDTO.builder()
                .fileData(imageData)
                .fileType("image/jpeg")
                .build();
        mockResponse.setPhoto(photo);

        doReturn(Mono.just(mockResponse)).when(customerService).getCustomerByCustomerId(testCustomerId, true);

        Mono<ResponseEntity<CustomerResponseDTO>> result = customerController.getCustomerByCustomerId(testCustomerId, true);

        StepVerifier.create(result)
                .consumeNextWith(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertNotNull(response.getBody());
                    CustomerResponseDTO body = response.getBody();
                    assertNotNull(body);
                    assertEquals(testCustomerId, body.getCustomerId());
                    assertEquals("John", body.getFirstName());
                    assertNotNull(body.getPhoto());
                    assertEquals(imageData, body.getPhoto().getFileData());
                    assertEquals("image/jpeg", body.getPhoto().getFileType());
                })
                .verifyComplete();
        verify(customerService, times(1)).getCustomerByCustomerId(testCustomerId, true);
    }

    @Test
    void getCustomerByCustomerId_ShouldReturnNotFound_WhenCustomerNotFound() {
        doReturn(Mono.empty()).when(customerService).getCustomerByCustomerId(testCustomerId, false);

        Mono<ResponseEntity<CustomerResponseDTO>> result = customerController.getCustomerByCustomerId(testCustomerId, false);

        StepVerifier.create(result)
                .consumeNextWith(response -> {
                    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
                })
                .verifyComplete();
    }

    @Test
    void updateCustomerPhoto_ShouldReturnUpdatedCustomer() {
        com.petclinic.customersservice.domainclientlayer.FileRequestDTO photoRequest = 
            com.petclinic.customersservice.domainclientlayer.FileRequestDTO.builder()
                .fileName("profile-photo.jpg")
                .fileType("image/jpeg")
                .fileData("base64data".getBytes())
                .build();

        CustomerResponseDTO mockResponse = new CustomerResponseDTO();
        mockResponse.setCustomerId(testCustomerId);
        mockResponse.setFirstName("John");
        FileResponseDTO photo = FileResponseDTO.builder()
                .fileId("photo-456")
                .fileType("image/jpeg")
                .fileData("base64data".getBytes())
                .build();
        mockResponse.setPhoto(photo);

        doReturn(Mono.just(mockResponse))
            .when(customerService)
            .updateCustomerPhoto(org.mockito.ArgumentMatchers.eq(testCustomerId), org.mockito.ArgumentMatchers.any(com.petclinic.customersservice.domainclientlayer.FileRequestDTO.class));

        Mono<ResponseEntity<CustomerResponseDTO>> result = customerController.updateCustomerPhoto(testCustomerId, Mono.just(photoRequest));

        StepVerifier.create(result)
            .consumeNextWith(response -> {
                assertEquals(HttpStatus.OK, response.getStatusCode());
                assertNotNull(response.getBody());
                CustomerResponseDTO body = response.getBody();
                if (body != null) {
                    assertEquals(testCustomerId, body.getCustomerId());
                    assertNotNull(body.getPhoto());
                    assertEquals("photo-456", body.getPhoto().getFileId());
                }
            })
            .verifyComplete();
        verify(customerService, times(1)).updateCustomerPhoto(org.mockito.ArgumentMatchers.eq(testCustomerId), org.mockito.ArgumentMatchers.any(com.petclinic.customersservice.domainclientlayer.FileRequestDTO.class));
    }

    @Test
    void whenDeleteCustomerPhoto_thenReturnNoContent() {
        CustomerResponseDTO mockResponse = new CustomerResponseDTO();
        mockResponse.setCustomerId(testCustomerId);
        mockResponse.setFirstName("John");

        doReturn(Mono.just(mockResponse)).when(customerService).deleteCustomerPhoto(testCustomerId);

        Mono<ResponseEntity<Void>> result = customerController.deleteCustomerPhoto(testCustomerId);

        StepVerifier.create(result)
                .consumeNextWith(response -> {
                    assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
                    assertNull(response.getBody());
                })
                .verifyComplete();

        verify(customerService, times(1)).deleteCustomerPhoto(testCustomerId);
    }

    @Test
    void whenDeleteCustomerPhoto_ShouldReturnNotFound_ifCustomerNotFound() {
        doReturn(Mono.empty()).when(customerService).deleteCustomerPhoto(testCustomerId);

        Mono<ResponseEntity<Void>> result = customerController.deleteCustomerPhoto(testCustomerId);

        StepVerifier.create(result)
                .consumeNextWith(response -> {
                    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
                })
                .verifyComplete();

        verify(customerService, times(1)).deleteCustomerPhoto(testCustomerId);
    }
}

