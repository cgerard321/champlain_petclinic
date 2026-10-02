package com.petclinic.customersservice.presentationlayer;

import com.petclinic.customersservice.data.Customer;
import com.petclinic.customersservice.data.CustomerRepo;
import com.petclinic.customersservice.domainclientlayer.FilesServiceClient;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;

@SpringBootTest
@AutoConfigureWebTestClient
class CustomerControllerIntegrationTest {

    @Autowired
    private WebTestClient client;

    @Autowired
    private CustomerRepo repo;

    @MockBean
    private FilesServiceClient filesServiceClient;

    private Customer buildCustomer() {
        return Customer.builder()
                .id("55")
                .customerId("customerId-123")
                .firstName("FirstName")
                .lastName("LastName")
                .address("Test address")
                .city("test city")
                .province("test province")
                .telephone("telephone")
                .build();
    }

    private Customer buildCustomer2() {
        return Customer.builder()
                .id("56")
                .customerId("customerId-456")
                .firstName("FirstName2")
                .lastName("LastName2")
                .address("Test address2")
                .city("test city2")
                .province("test province2")
                .telephone("telephone2")
                .build();
    }

    private Customer buildCustomerId(String firstName, String customerId) {
        return Customer.builder()
                .customerId(customerId)
                .firstName(firstName)
                .lastName("Doe")
                .address("123 Main St")
                .city("Anytown")
                .province("CA")
                .telephone("5555555555")
                .build();
    }

    Customer customerEntity = buildCustomer();

    Customer customerEntity2 = buildCustomer2();

    String customerId = customerEntity.getId();

    String publicCustomerId = customerEntity.getCustomerId();

    Customer customer1 = buildCustomerId("Billy","customerId_1");
    @Test
    void deleteCustomerbyCustomerId() {

        String uniqueCustomerId = java.util.UUID.randomUUID().toString();

        Customer customerEntity = Customer.builder()
                .id(java.util.UUID.randomUUID().toString())
                .customerId(uniqueCustomerId)
                .firstName("FirstName")
                .lastName("LastName")
                .address("Test address")
                .city("test city")
                .province("province")
                .telephone("telephone")
                .build();

        StepVerifier.create(repo.save(customerEntity))
                .expectNextMatches(saved -> saved.getCustomerId().equals(uniqueCustomerId))
                .verifyComplete();

        client.delete().uri("/customers/" + uniqueCustomerId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent()
                .expectBody();
    }
    @Test
    void deleteNonExistentCustomerByCustomerId() throws InterruptedException {

        StepVerifier.create(repo.deleteAll()).verifyComplete();
       try {
           Thread.sleep(100);
       } catch (InterruptedException e) {
           Thread.currentThread().interrupt();
       }
        String nonExistentCustomerId = java.util.UUID.randomUUID().toString();

        client.delete().uri("/customers/" + nonExistentCustomerId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Customer id not found: " + nonExistentCustomerId);
    }



    @Test
    void getTotalNumberOfCustomers(){
        Customer customer1 = Customer.builder()
                .customerId("customerId-11")
                .firstName("FirstName1")
                .lastName("LastName1")
                .address("Test address1")
                .city("test city1")
                .province("province1")
                .telephone("telephone1")
                .build();

        StepVerifier.create(repo.deleteAll().thenMany(repo.save(customer1))).expectNextCount(1).verifyComplete();

        client.get()
                .uri("/customers/customers-count")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Long.class)
                .value(total -> {
                    assertNotNull(total);
                    assertEquals(1L, total);
                });

    }

    @Test
    void getCustomersPagination() {

        Customer customer1 = Customer.builder()
                .customerId("customerId-11")
                .firstName("FirstName1")
                .lastName("LastName1")
                .address("Test address1")
                .city("test city1")
                .province("province1")
                .telephone("telephone1")
                .build();

        int page = 0;
        int size = 1;

        StepVerifier.create(repo.deleteAll().thenMany(repo.save(customer1))).expectNextCount(1).verifyComplete();
        StepVerifier.create(repo.save(customer1)).expectNextCount(1).verifyComplete();

        client.get()
                .uri("/customers/customers-pagination?page="+page+"&size="+size)
                .accept(MediaType.valueOf(MediaType.TEXT_EVENT_STREAM_VALUE))
                .acceptCharset(StandardCharsets.UTF_8)
                .exchange().expectStatus().isOk()
                .expectHeader().valueEquals("Content-Type","text/event-stream;charset=UTF-8")
                .expectBodyList(CustomerResponseDTO.class)
                .value((list) -> {
                    assertNotNull(list);
                    assertEquals(size,list.size());
                });

    }

    @Test
    void getTotalNumberOfCustomersWithFilters1_shouldSucceed(){

        String firstName = "FirstName1";
        String city = "test city1";


        Customer customer1 = Customer.builder()
                .customerId("customerId-1")
                .firstName("FirstName1")
                .lastName("LastName1")
                .address("Test address1")
                .city("test city1")
                .province("province1")
                .telephone("telephone1")
                .photoId(null)
                .build();

        StepVerifier.create(repo.deleteAll().thenMany(repo.save(customer1))).expectNextCount(1).verifyComplete();

        client.get()
                .uri("/customers/customers-filtered-count?&firstName="+firstName+"&city="+city)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Long.class)
                .value(total -> {
                    assertNotNull(total);
                    assertEquals(1L, total);
                });
    }

    @Test
    void getTotalNumberOfCustomersWithFilters2_shouldSucceed(){

        String firstName = "FirstName2";
        String customerId = "customerId-2";


        Customer customer1 = Customer.builder()
                .customerId("customerId-2")
                .firstName("FirstName2")
                .lastName("LastName2")
                .address("Test address2")
                .city("test city2")
                .province("province2")
                .telephone("telephone2")
                .build();

        StepVerifier.create(repo.deleteAll().thenMany(repo.save(customer1))).expectNextCount(1).verifyComplete();

        client.get()
                .uri("/customers/customers-filtered-count?&firstName="+firstName+"&customerId="+customerId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Long.class)
                .value(total -> {
                    assertNotNull(total);
                    assertEquals(1L, total);
                });
    }

    @Test
    void getTotalNumberOfCustomersWithFilters3_shouldSucceed(){

        String firstName = "FirstName3";
        String customerId = "customerId-3";
        String lastname = "LastName3";
        String city = "test city3";
        String telephone = "telephone3";


        Customer customer1 = Customer.builder()
                .customerId("customerId-3")
                .firstName("FirstName3")
                .lastName("LastName3")
                .address("Test address3")
                .city("test city3")
                .province("province3")
                .telephone("telephone3")
                .build();

        StepVerifier.create(repo.deleteAll().thenMany(repo.save(customer1))).expectNextCount(1).verifyComplete();

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString("/customers/customers-filtered-count");

        builder.queryParam("customerId", customerId);
        builder.queryParam("firstName", firstName);
        builder.queryParam("lastName",lastname);
        builder.queryParam("city", city);
        builder.queryParam("phoneNumber", telephone);


        client.get()
                .uri(builder.build().toUri())
                .exchange()
                .expectStatus().isOk()
                .expectBody(Long.class)
                .value(total -> {
                    assertNotNull(total);
                    assertEquals(1L, total);
                });
    }


    @Test
    void getCustomerByCustomerId() {
        Publisher<Customer> setup = repo.deleteAll().thenMany(repo.save(customerEntity));
        StepVerifier.create(setup).expectNextCount(1).verifyComplete();
        client.get().uri("/customers/" + publicCustomerId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange().expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(CustomerResponseDTO.class)
                .value(customerResponseDTO -> {
                    assertNotNull(customerResponseDTO);
                    assertEquals(customerResponseDTO.getCustomerId(), customerEntity.getCustomerId());
                    assertEquals(customerResponseDTO.getFirstName(), customerEntity.getFirstName());
                    assertEquals(customerResponseDTO.getLastName(), customerEntity.getLastName());
                    assertEquals(customerResponseDTO.getAddress(), customerEntity.getAddress());
                    assertEquals(customerResponseDTO.getCity(), customerEntity.getCity());
                    assertEquals(customerResponseDTO.getProvince(), customerEntity.getProvince());
                    assertEquals(customerResponseDTO.getTelephone(), customerEntity.getTelephone());
                });

    }

    @Test
    void updateCustomerByCustomerId() {
        // Setup a unique customer for this test
        String testCustomerId = "1b747de5-f242-4182-ae92-2b6937b982a2";
        Customer existingCustomer = buildCustomerId("OldFirst", testCustomerId);
        existingCustomer.setId("1");

        // 1. Save the existing customer
        Publisher<Customer> setup = repo.deleteAll().then(repo.save(existingCustomer));
        StepVerifier.create(setup).expectNextCount(1).verifyComplete();

        // 2. Prepare the update DTO (assuming a full request DTO is needed)
        CustomerRequestDTO updateDTO = new CustomerRequestDTO();
        updateDTO.setFirstName("NewFirstName");
        updateDTO.setLastName("NewLastName");
        updateDTO.setAddress("New Address");
        updateDTO.setCity("New City");
        updateDTO.setProvince("New Province");
        updateDTO.setTelephone("9999999999");
        // photoId is left null

        // 3. Make the PUT request
        client.put()
                .uri("/customers/" + testCustomerId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(updateDTO), CustomerRequestDTO.class)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                // 4. Assert the response contents
                .expectBody(CustomerResponseDTO.class)
                .value(responseDTO -> {
                    assertNotNull(responseDTO);
                    assertEquals(testCustomerId, responseDTO.getCustomerId());
                    assertEquals("NewFirstName", responseDTO.getFirstName());
                    assertEquals("New City", responseDTO.getCity());
                });

        // 5. Verify the update persisted (optional but robust)
        Mono<Customer> checkCustomer = repo.findCustomerByCustomerId(testCustomerId);
        StepVerifier.create(checkCustomer)
                .expectNextMatches(customer ->
                        customer.getFirstName().equals("NewFirstName") &&
                                customer.getCity().equals("New City")
                )
                .verifyComplete();
    }

    @Test
    void whenDeleteCustomerPhoto_withValidId_ShouldReturnNoContentAndRemovePhotoId() {
        String testCustomerId = "delete-photo-id-789";
        String TEST_PHOTO_ID = "photo-to-delete-456";

        Customer customerWithPhoto = buildCustomerId("TestCustomer", testCustomerId);
        customerWithPhoto.setPhotoId(TEST_PHOTO_ID);

        Publisher<Customer> setup = repo.deleteAll().then(repo.save(customerWithPhoto));
        StepVerifier.create(setup).expectNextCount(1).verifyComplete();

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        when(filesServiceClient.deleteFile(TEST_PHOTO_ID)).thenReturn(Mono.empty());

        client.delete().uri("/customers/" + testCustomerId + "/photo")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent()
                .expectBody();

        Mono<Customer> checkCustomer = repo.findCustomerByCustomerId(testCustomerId);
        StepVerifier.create(checkCustomer)
                .expectNextMatches(customer -> customer.getPhotoId() == null)
                .verifyComplete();

        verify(filesServiceClient).deleteFile(TEST_PHOTO_ID);
    }
}
