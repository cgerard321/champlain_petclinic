package com.petclinic.customersservice.presentationlayer;

import com.petclinic.customersservice.business.CustomerService;
import com.petclinic.customersservice.customersExceptions.ApplicationExceptions;
import com.petclinic.customersservice.customersExceptions.exceptions.InvalidInputException;
import com.petclinic.customersservice.domainclientlayer.FileRequestDTO;
import com.petclinic.customersservice.util.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping()
    public Flux<CustomerResponseDTO> getAllCustomers() {
        return customerService.getAllCustomers();
    }

    @GetMapping("/customers-count")
    public Mono<ResponseEntity<Long>> getTotalNumberOfCustomers(){
        return customerService.getAllCustomers().count()
                .map(response -> ResponseEntity.status(HttpStatus.OK).body(response));
    }

    @GetMapping("/customers-pagination")
    public Flux<CustomerResponseDTO> getAllCustomersPagination(
            @RequestParam Optional<Integer> page,
            @RequestParam Optional<Integer> size,
            @RequestParam(required = false) String customerId,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) String city
    ){
        return customerService.getAllCustomersPagination(
                PageRequest.of(page.orElse(0),size.orElse(5)),customerId,firstName,lastName,phoneNumber,city);
    }

    @GetMapping("/customers-filtered-count")
    public Mono<Long> getTotalNumberOfCustomersWithFilters(
            @RequestParam(required = false) String customerId,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) String city) {

        return customerService.getTotalNumberOfCustomersWithFilters(customerId,firstName,lastName,phoneNumber,city);
    }

    @GetMapping("/{customerId}")
    public Mono<ResponseEntity<CustomerResponseDTO>> getCustomerByCustomerId(@PathVariable String customerId, @RequestParam(required = false, defaultValue = "false") boolean includePhoto) {
        return customerService.getCustomerByCustomerId(customerId, includePhoto)
                .map(customerResponseDTO -> ResponseEntity.status(HttpStatus.OK).body(customerResponseDTO))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping()
    public Mono<ResponseEntity<CustomerResponseDTO>> addCustomer(@RequestBody Mono<CustomerRequestDTO> customerRequestDTOMono) {
        return customerRequestDTOMono
                .transform(Validator.validateCustomer())
                .as(customerService::addCustomer)
                .map(customerResponseDTO -> ResponseEntity.status(HttpStatus.CREATED).body(customerResponseDTO));
    }

    @DeleteMapping(value = "/{customerId}")
    public Mono<ResponseEntity<Void>> deleteCustomerByCustomerId(@PathVariable String customerId){
        return Mono.just(customerId)
                .filter(id -> id.length() == 36)
                .switchIfEmpty(Mono.error(new InvalidInputException("Provided course id is invalid: " + customerId)))
                .flatMap(customerService::deleteCustomerByCustomerId)
                .map(v -> ResponseEntity.noContent().<Void>build())
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    @PutMapping("/{customerId}")
    public Mono<ResponseEntity<CustomerResponseDTO>> updateCustomer(@RequestBody Mono<CustomerRequestDTO> customerRequestDTOMono, @PathVariable String customerId) {
        return Mono.just(customerId)
                .filter(id -> id.length() == 36)
                .switchIfEmpty(ApplicationExceptions.invalidCustomerId(customerId))
                .thenReturn(customerRequestDTOMono.transform(Validator.validateCustomer()))
                .flatMap(request -> customerService.updateCustomer(request, customerId))
                .map(ResponseEntity::ok)
                .switchIfEmpty(ApplicationExceptions.customerNotFound(customerId));
    }

    @PatchMapping("/{customerId}/photo")
    public Mono<ResponseEntity<CustomerResponseDTO>> updateCustomerPhoto(@PathVariable String customerId, @RequestBody Mono<FileRequestDTO> photoMono) {
        return Mono.just(customerId)
                .filter(id -> id.length() == 36)
                .switchIfEmpty(ApplicationExceptions.invalidCustomerId(customerId))
                .flatMap(validId ->
                        photoMono.flatMap(photo ->
                                customerService.updateCustomerPhoto(validId, photo)
                        )
                )
                .map(ResponseEntity::ok)
                .switchIfEmpty(ApplicationExceptions.customerNotFound(customerId));
    }

    @DeleteMapping("/{customerId}/photo")
    public Mono<ResponseEntity<Void>> deleteCustomerPhoto(@PathVariable String customerId) {
        return customerService.deleteCustomerPhoto(customerId)
                .map(v -> ResponseEntity.noContent().<Void>build())
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}