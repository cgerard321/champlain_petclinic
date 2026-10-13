package com.petclinic.customersservice.business;

import com.petclinic.customersservice.domainclientlayer.FileRequestDTO;
import com.petclinic.customersservice.presentationlayer.CustomerRequestDTO;
import com.petclinic.customersservice.presentationlayer.CustomerResponseDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.springframework.data.domain.Pageable;


public interface CustomerService {
    Flux<CustomerResponseDTO> getAllCustomers();
    Mono<Long> getTotalNumberOfCustomersWithFilters(String customerId,String firstName,String lastName,String phoneNumber, String city);
    Flux<CustomerResponseDTO> getAllCustomersPagination(Pageable pageable, String customerId, String firstName, String lastName, String phoneNumber, String city);
    Mono<CustomerResponseDTO> getCustomerByCustomerId(String customerId);
    Mono<CustomerResponseDTO> getCustomerByCustomerId(String customerId, boolean includePhoto);
    Mono<CustomerResponseDTO> addCustomer(Mono<CustomerRequestDTO> customerRequestDTO);
    Mono<CustomerResponseDTO> updateCustomer(Mono<CustomerRequestDTO> customerRequestDTO, String customerId);
    Mono<CustomerResponseDTO> updateCustomerPhoto(String customerId, FileRequestDTO photo);
    Mono<CustomerResponseDTO> deleteCustomerByCustomerId(String customerId);
    Mono<CustomerResponseDTO> deleteCustomerPhoto(String customerId);
}

