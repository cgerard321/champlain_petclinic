package com.petclinic.customersservice.business;

import com.petclinic.customersservice.customersExceptions.exceptions.NotFoundException;
import com.petclinic.customersservice.data.Customer;
import com.petclinic.customersservice.data.CustomerRepo;
import com.petclinic.customersservice.domainclientlayer.FilesServiceClient;
import com.petclinic.customersservice.presentationlayer.CustomerRequestDTO;
import com.petclinic.customersservice.presentationlayer.CustomerResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {"spring.data.mongodb.port=27020"})
@AutoConfigureWebTestClient
class CustomerServiceImplTest {

    @MockBean
    private CustomerRepo repo;

    @MockBean
    private FilesServiceClient filesServiceClient;

    @Autowired
    private CustomerService customerService;

    private final Customer customerEntity = buildCustomer();
    private final CustomerRequestDTO customerRequestDTO = buildCustomerRequestDTO();

    private Customer buildCustomer() {
        return Customer.builder()
                .id("55")
                .customerId("customerId-123")
                .firstName("FirstName")
                .lastName("LastName")
                .address("Test address")
                .city("test city")
                .province("test province")
                .telephone("1234567890")
                .build();
    }

    private CustomerRequestDTO buildCustomerRequestDTO() {
        return CustomerRequestDTO.builder()
                .firstName("FirstName")
                .lastName("LastName")
                .address("Test address")
                .city("test city")
                .province("test province")
                .telephone("1234567890")
                .build();
    }

    @Test
    void getAllCustomers_ShouldSucceed() {
        // Arrange
        Customer customerEntity = buildCustomer();
        when(repo.findAll()).thenReturn(Flux.just(customerEntity));

        // Act & Assert
        StepVerifier
                .create(customerService.getAllCustomers()) // Call service method
                .expectNextMatches(customerDto -> customerDto.getCustomerId().equals(customerEntity.getCustomerId()))
                .expectComplete()
                .verify();
        verify(repo).findAll();

    }

    @Test
    void getCustomersPagination_ShouldSucceed(){
        // Arrange: Setup 3 customers
        Customer customer1 = buildCustomer();
        customer1.setCustomerId("customerId-11");
        Customer customer2 = buildCustomer();
        customer2.setCustomerId("customerId-17");
        Customer customer3 = buildCustomer();
        customer3.setCustomerId("customerId-13");

        Pageable pageable = PageRequest.of(0, 2);

        when(repo.findAll()).thenReturn(Flux.just(customer1, customer2, customer3));

        Flux<CustomerResponseDTO> customers = customerService.getAllCustomersPagination(pageable,null,null,null,null,null);

        StepVerifier.create(customers)
                .expectNextMatches(customerDto1 -> customerDto1.getCustomerId().equals(customer1.getCustomerId()))
                .expectNextMatches(customerDto2 -> customerDto2.getCustomerId().equals(customer2.getCustomerId()))
                .expectComplete()
                .verify();
    }

    @Test
    void getCustomersPaginationWithFiltersApplied1_ShouldSucceed(){
        Customer customer1 = buildCustomer();
        customer1.setCustomerId("customerId-11");
        customer1.setCity("test city1");

        Pageable pageable = PageRequest.of(0, 2);
        String city = "test city1";
        String customerId = "customerId-11";

        when(repo.findAll()).thenReturn(Flux.just(customer1));

        Flux<CustomerResponseDTO> customers = customerService.getAllCustomersPagination(pageable,customerId,null,null,null,city);

        StepVerifier.create(customers)
                .expectNextMatches(customerDto1 -> customerDto1.getCustomerId().equals(customer1.getCustomerId())
                        && customerDto1.getCity().equals(customer1.getCity()))
                .expectComplete()
                .verify();
    }

    @Test
    void getCustomersPaginationWithFiltersApplied2_ShouldSucceed(){
        Customer customer1 = buildCustomer();
        customer1.setCustomerId("customerId-2");
        customer1.setFirstName("FirstName2");
        customer1.setLastName("LastName2");
        customer1.setCity("test city2");
        customer1.setTelephone("telephone2");

        Pageable pageable = PageRequest.of(0, 2);

        String city = "test city2";
        String customerId = "customerId-2";
        String lastName = "LastName2";
        String firstName = "FirstName2";
        String phoneNumber = "telephone2";

        when(repo.findAll()).thenReturn(Flux.just(customer1));

        Flux<CustomerResponseDTO> customers = customerService.getAllCustomersPagination(pageable,customerId,firstName,lastName,phoneNumber,city);

        StepVerifier.create(customers)
                .expectNextMatches(
                        customerDto1 -> customerDto1.getCustomerId().equals(customer1.getCustomerId())
                                && customerDto1.getCity().equals(customer1.getCity())
                                && customerDto1.getTelephone().equals(customer1.getTelephone())
                                && customerDto1.getFirstName().equals(customer1.getFirstName())
                                && customerDto1.getLastName().equals(customer1.getLastName()))
                .expectComplete()
                .verify();
    }

    @Test
    void addCustomer_ShouldSucceed() {
        when(repo.save(any(Customer.class))).thenReturn(Mono.just(customerEntity));

        StepVerifier.create(customerService.addCustomer(Mono.just(customerRequestDTO)))
                .consumeNextWith(foundCustomer -> {
                    assertEquals(customerRequestDTO.getFirstName(), foundCustomer.getFirstName());
                    assertEquals(customerRequestDTO.getLastName(), foundCustomer.getLastName());
                    assertEquals(customerRequestDTO.getCity(), foundCustomer.getCity());
                    assertEquals(customerRequestDTO.getTelephone(), foundCustomer.getTelephone());
                    assertEquals(customerRequestDTO.getAddress(), foundCustomer.getAddress());
                    assertEquals(customerRequestDTO.getProvince(), foundCustomer.getProvince());
                })
                .verifyComplete();

        verify(repo).save(any(Customer.class));
    }

    @Test
    void getCustomerByCustomerId_ShouldSucceed() {
        Customer customerEntity = buildCustomer();
        String customerId = customerEntity.getCustomerId();
        when(repo.findCustomerByCustomerId(customerId)).thenReturn(Mono.just(customerEntity));

        Mono<CustomerResponseDTO> customerResponseDTOMono = customerService.getCustomerByCustomerId(customerId);

        StepVerifier
                .create(customerResponseDTOMono)
                .consumeNextWith(foundCustomer -> {
                    assertEquals(customerEntity.getCustomerId(), foundCustomer.getCustomerId());
                })
                .verifyComplete();
        verify(repo).findCustomerByCustomerId(customerId);
    }

    @Test
    void deleteCustomerByCustomerId_ShouldCompleteSuccessfully() {
        String customerId = customerEntity.getCustomerId();
        when(repo.deleteById(customerId)).thenReturn(Mono.empty());

        Mono<Void> deleteObj = customerService.deleteCustomer(customerId);

        StepVerifier
                .create(deleteObj)
                .verifyComplete();

       verify(repo).deleteById(customerId);

    }


    @Test
    void updateCustomer_ShouldSucceed() {
        // Define input data
        String customerId = "customerId-123";
        CustomerRequestDTO customerRequestDTO = new CustomerRequestDTO();
        customerRequestDTO.setFirstName("Updated First Name");

        Customer existingCustomer = buildCustomer();

        when(repo.findCustomerByCustomerId(customerId)).thenReturn(Mono.just(existingCustomer));
        when(repo.save(any(Customer.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        Mono<CustomerResponseDTO> updatedCustomer = customerService.updateCustomer(Mono.just(customerRequestDTO), customerId);

        StepVerifier.create(updatedCustomer)
                .expectNextMatches(updateCustomer -> {
                    assertEquals(customerId, updateCustomer.getCustomerId());
                    assertEquals(customerRequestDTO.getFirstName(), updateCustomer.getFirstName());
                    return true;
                })
                .expectComplete()
                .verify();

        verify(repo).findCustomerByCustomerId(customerId);
        verify(repo).save(any(Customer.class));
    }

    @Test
    void getCustomerByCustomerId_WithIncludePhotoTrue_ShouldReturnCustomerWithPhoto() {
        Customer customerEntity = buildCustomer();
        customerEntity.setPhotoId("photo-123");
        String customerId = customerEntity.getCustomerId();
        
        when(repo.findCustomerByCustomerId(customerId)).thenReturn(Mono.just(customerEntity));
        
        com.petclinic.customersservice.domainclientlayer.FileResponseDTO fileResponse = 
            com.petclinic.customersservice.domainclientlayer.FileResponseDTO.builder()
                .fileData("mockPhotoData".getBytes())
                .fileType("image/png")
                .build();
        when(filesServiceClient.getFile("photo-123")).thenReturn(Mono.just(fileResponse));

        Mono<CustomerResponseDTO> customerResponseDTOMono = customerService.getCustomerByCustomerId(customerId, true);

        StepVerifier
                .create(customerResponseDTOMono)
                .consumeNextWith(foundCustomer -> {
                    assertEquals(customerEntity.getCustomerId(), foundCustomer.getCustomerId());
                    assertNotNull(foundCustomer.getPhoto());
                    assertArrayEquals("mockPhotoData".getBytes(), foundCustomer.getPhoto().getFileData());
                    assertEquals("image/png", foundCustomer.getPhoto().getFileType());
                })
                .verifyComplete();
        verify(repo).findCustomerByCustomerId(customerId);
        verify(filesServiceClient).getFile("photo-123");
    }

    @Test
    void getCustomerByCustomerId_WithIncludePhotoFalse_ShouldReturnCustomerWithoutPhoto() {
        Customer customerEntity = buildCustomer();
        String customerId = customerEntity.getCustomerId();
        when(repo.findCustomerByCustomerId(customerId)).thenReturn(Mono.just(customerEntity));

        Mono<CustomerResponseDTO> customerResponseDTOMono = customerService.getCustomerByCustomerId(customerId, false);

        StepVerifier
                .create(customerResponseDTOMono)
                .consumeNextWith(foundCustomer -> {
                    assertEquals(customerEntity.getCustomerId(), foundCustomer.getCustomerId());
                    assertNull(foundCustomer.getPhoto());
                })
                .verifyComplete();
        verify(repo).findCustomerByCustomerId(customerId);
        verify(filesServiceClient, never()).getFile(anyString());
    }

    @Test
    void getCustomerByCustomerId_WithIncludePhotoTrue_ShouldHandleFileServiceError() {
        Customer customerEntity = buildCustomer();
        customerEntity.setPhotoId("photo-123");
        String customerId = customerEntity.getCustomerId();
        
        when(repo.findCustomerByCustomerId(customerId)).thenReturn(Mono.just(customerEntity));
        when(filesServiceClient.getFile("photo-123")).thenReturn(Mono.error(new RuntimeException("File service error")));

        Mono<CustomerResponseDTO> customerResponseDTOMono = customerService.getCustomerByCustomerId(customerId, true);

        StepVerifier
                .create(customerResponseDTOMono)
                .consumeNextWith(foundCustomer -> {
                    assertEquals(customerEntity.getCustomerId(), foundCustomer.getCustomerId());
                    assertNull(foundCustomer.getPhoto());
                })
                .verifyComplete();
        verify(repo).findCustomerByCustomerId(customerId);
        verify(filesServiceClient).getFile("photo-123");
    }

    @Test
    void updateCustomerPhoto_ShouldSucceed() {
        String customerId = "customerId-123";
        Customer existingCustomer = buildCustomer();
        
        com.petclinic.customersservice.domainclientlayer.FileRequestDTO photoRequest = 
            com.petclinic.customersservice.domainclientlayer.FileRequestDTO.builder()
                .fileName("profile-photo")
                .fileType("image/jpeg")
                .fileData("base64data".getBytes())
                .build();
        
        com.petclinic.customersservice.domainclientlayer.FileResponseDTO addFileResponse = 
            com.petclinic.customersservice.domainclientlayer.FileResponseDTO.builder()
                .fileId("new-photo-id")
                .fileName("profile-photo")
                .fileType("image/jpeg")
                .build();
        
        when(repo.findCustomerByCustomerId(customerId)).thenReturn(Mono.just(existingCustomer));
        when(filesServiceClient.addFile(any(com.petclinic.customersservice.domainclientlayer.FileRequestDTO.class)))
            .thenReturn(Mono.just(addFileResponse));
        when(repo.save(any(Customer.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        
        Mono<CustomerResponseDTO> result = customerService.updateCustomerPhoto(customerId, photoRequest);
        
        StepVerifier.create(result)
            .consumeNextWith(updatedCustomer -> {
                assertEquals(customerId, updatedCustomer.getCustomerId());
                assertNotNull(updatedCustomer.getPhoto());
                assertEquals("new-photo-id", updatedCustomer.getPhoto().getFileId());
                assertEquals("image/jpeg", updatedCustomer.getPhoto().getFileType());
            })
            .verifyComplete();
        
        verify(repo).findCustomerByCustomerId(customerId);
        verify(filesServiceClient).addFile(any(com.petclinic.customersservice.domainclientlayer.FileRequestDTO.class));
        verify(repo).save(any(Customer.class));
        verify(filesServiceClient, never()).getFile(anyString());
    }

    @Test
    void updateCustomerPhoto_WithNonExistentCustomer_ShouldThrowNotFoundException() {
        String customerId = "non-existent-id";
        
        com.petclinic.customersservice.domainclientlayer.FileRequestDTO photoRequest = 
            com.petclinic.customersservice.domainclientlayer.FileRequestDTO.builder()
                .fileName("profile-photo")
                .fileType("image/jpeg")
                .fileData("base64data".getBytes())
                .build();
        
        when(repo.findCustomerByCustomerId(customerId)).thenReturn(Mono.empty());
        
        Mono<CustomerResponseDTO> result = customerService.updateCustomerPhoto(customerId, photoRequest);
        
        StepVerifier.create(result)
            .expectError(NotFoundException.class)
            .verify();
        
        verify(repo).findCustomerByCustomerId(customerId);
        verify(filesServiceClient, never()).addFile(any());
        verify(repo, never()).save(any());
    }

    @Test
    void updateCustomerPhoto_WhenFileServiceFails_ShouldPropagateError() {
        String customerId = "customerId-123";
        Customer existingCustomer = buildCustomer();
        
        com.petclinic.customersservice.domainclientlayer.FileRequestDTO photoRequest = 
            com.petclinic.customersservice.domainclientlayer.FileRequestDTO.builder()
                .fileName("profile-photo")
                .fileType("image/jpeg")
                .fileData("base64data".getBytes())
                .build();
        
        when(repo.findCustomerByCustomerId(customerId)).thenReturn(Mono.just(existingCustomer));
        when(filesServiceClient.addFile(any(com.petclinic.customersservice.domainclientlayer.FileRequestDTO.class)))
            .thenReturn(Mono.error(new RuntimeException("File service unavailable")));
        
        Mono<CustomerResponseDTO> result = customerService.updateCustomerPhoto(customerId, photoRequest);
        
        StepVerifier.create(result)
            .expectError(RuntimeException.class)
            .verify();
        
        verify(repo).findCustomerByCustomerId(customerId);
        verify(filesServiceClient).addFile(any(com.petclinic.customersservice.domainclientlayer.FileRequestDTO.class));
        verify(repo, never()).save(any());
    }

    @Test
    void updateCustomerPhoto_WithExistingPhoto_ShouldUpdateFile() {
        String customerId = "customerId-123";
        Customer existingCustomer = buildCustomer();
        existingCustomer.setPhotoId("existing-photo-id");
        
        com.petclinic.customersservice.domainclientlayer.FileRequestDTO photoRequest = 
            com.petclinic.customersservice.domainclientlayer.FileRequestDTO.builder()
                .fileName("updated-photo")
                .fileType("image/png")
                .fileData("newbase64data".getBytes())
                .build();
        
        com.petclinic.customersservice.domainclientlayer.FileResponseDTO updateFileResponse = 
            com.petclinic.customersservice.domainclientlayer.FileResponseDTO.builder()
                .fileId("existing-photo-id")
                .fileName("updated-photo")
                .fileType("image/png")
                .build();
        
        when(repo.findCustomerByCustomerId(customerId)).thenReturn(Mono.just(existingCustomer));
        when(filesServiceClient.updateFile(eq("existing-photo-id"), any(com.petclinic.customersservice.domainclientlayer.FileRequestDTO.class)))
            .thenReturn(Mono.just(updateFileResponse));
        when(repo.save(any(Customer.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        
        Mono<CustomerResponseDTO> result = customerService.updateCustomerPhoto(customerId, photoRequest);
        
        StepVerifier.create(result)
            .consumeNextWith(updatedCustomer -> {
                assertEquals(customerId, updatedCustomer.getCustomerId());
                assertNotNull(updatedCustomer.getPhoto());
                assertEquals("existing-photo-id", updatedCustomer.getPhoto().getFileId());
                assertEquals("image/png", updatedCustomer.getPhoto().getFileType());
            })
            .verifyComplete();
        
        verify(repo).findCustomerByCustomerId(customerId);
        verify(filesServiceClient).updateFile(eq("existing-photo-id"), any(com.petclinic.customersservice.domainclientlayer.FileRequestDTO.class));
        verify(filesServiceClient, never()).addFile(any());
        verify(repo).save(any(Customer.class));
        verify(filesServiceClient, never()).getFile(anyString());
    }

    @Test
    void whenDeleteCustomerPhoto_withPhotoId_thenDeleteFile() {
        String customerId = "customerId-123";
        String PHOTO_ID = "photo-999";
        Customer existingCustomerWithPhoto = buildCustomer();
        existingCustomerWithPhoto.setCustomerId(customerId);
        existingCustomerWithPhoto.setPhotoId(PHOTO_ID);

        Customer savedCustomerWithoutPhoto = buildCustomer();
        savedCustomerWithoutPhoto.setCustomerId(customerId);
        savedCustomerWithoutPhoto.setPhotoId(null);

        when(repo.findCustomerByCustomerId(customerId)).thenReturn(Mono.just(existingCustomerWithPhoto));
        when(repo.save(argThat(customer -> customer.getPhotoId() == null)))
                .thenReturn(Mono.just(savedCustomerWithoutPhoto));
        when(filesServiceClient.deleteFile(PHOTO_ID)).thenReturn(Mono.empty());

        Mono<CustomerResponseDTO> result = customerService.deleteCustomerPhoto(customerId);

        StepVerifier.create(result)
                .consumeNextWith(response -> {
                    assertEquals(customerId, response.getCustomerId());
                    assertNull(response.getPhoto());
                })
                .verifyComplete();

        verify(repo).findCustomerByCustomerId(customerId);
        verify(repo).save(argThat(customer -> customer.getPhotoId() == null));
        verify(filesServiceClient).deleteFile(PHOTO_ID);
    }

    @Test
    void whenDeleteCustomerPhoto_WhenNoPhotoExists_ShouldSucceed_() {
        String customerId = "customerId-123";
        Customer existingCustomerWithoutPhoto = buildCustomer();
        existingCustomerWithoutPhoto.setCustomerId(customerId);
        existingCustomerWithoutPhoto.setPhotoId(null);

        when(repo.findCustomerByCustomerId(customerId)).thenReturn(Mono.just(existingCustomerWithoutPhoto));

        Mono<CustomerResponseDTO> result = customerService.deleteCustomerPhoto(customerId);

        StepVerifier.create(result)
                .consumeNextWith(response -> {
                    assertEquals(customerId, response.getCustomerId());
                    assertNull(response.getPhoto());
                })
                .verifyComplete();

        verify(repo).findCustomerByCustomerId(customerId);
        verify(repo, never()).save(any(Customer.class));
        verify(filesServiceClient, never()).deleteFile(anyString());
    }

    @Test
    void deleteCustomerPhoto_ShouldThrowNotFoundException_WhenCustomerNotFound() {
        String NON_EXISTENT_ID = "non-existent-id";
        when(repo.findCustomerByCustomerId(NON_EXISTENT_ID)).thenReturn(Mono.empty());

        Mono<CustomerResponseDTO> result = customerService.deleteCustomerPhoto(NON_EXISTENT_ID);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException &&
                        throwable.getMessage().contains("Customer not found"))
                .verify();

        verify(repo).findCustomerByCustomerId(NON_EXISTENT_ID);
        verify(repo, never()).save(any(Customer.class));
        verify(filesServiceClient, never()).deleteFile(anyString());
    }

}
