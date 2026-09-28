package com.petclinic.customersservice.business;

import com.petclinic.customersservice.customersExceptions.exceptions.NotFoundException;
import com.petclinic.customersservice.data.Customer;
import com.petclinic.customersservice.data.CustomerRepo;
import com.petclinic.customersservice.domainclientlayer.FileRequestDTO;
import com.petclinic.customersservice.domainclientlayer.FileResponseDTO;
import com.petclinic.customersservice.domainclientlayer.FilesServiceClient;
import com.petclinic.customersservice.presentationlayer.CustomerRequestDTO;
import com.petclinic.customersservice.presentationlayer.CustomerResponseDTO;
import com.petclinic.customersservice.util.EntityDTOUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.springframework.data.domain.Pageable;

import java.util.function.Predicate;

@Slf4j
@Service
public class CustomerServiceImpl implements CustomerService {

    @Autowired
    CustomerRepo customerRepo;
    
    @Autowired
    FilesServiceClient filesServiceClient;

    // insertCustomer has been updated, now sets a UUID for customerId rather than leave null
    @Override
    public Mono<CustomerResponseDTO> addCustomer(Mono<CustomerRequestDTO> customerRequestDTO) {
        return customerRequestDTO
                .map(EntityDTOUtil::toCustomer)
                .flatMap(customerRepo::save)
                .map(EntityDTOUtil::toCustomerReponseDTO);
    }


    // get customer by customerId has been updated and now return
    @Override
    public Mono<CustomerResponseDTO> getCustomerByCustomerId(String customerId) {
        return customerRepo.findCustomerByCustomerId(customerId)
                .map(EntityDTOUtil::toCustomerReponseDTO);
    }

    @Override
    public Mono<CustomerResponseDTO> getCustomerByCustomerId(String customerId, boolean includePhoto) {
        return customerRepo.findCustomerByCustomerId(customerId)
                .flatMap(customer -> {
                    CustomerResponseDTO dto = EntityDTOUtil.toCustomerReponseDTO(customer);
                    if (includePhoto && customer.getPhotoId() != null && !customer.getPhotoId().isEmpty()) {
                        return filesServiceClient.getFile(customer.getPhotoId())
                                .map(fileResp -> {
                                    dto.setPhoto(fileResp);
                                    return dto;
                                })
                                .onErrorResume(err -> {
                                    log.error("Error fetching file {} for customerId {}: {}", customer.getPhotoId(), customerId, err.getMessage());
                                    return Mono.just(dto);
                                });
                    } else {
                        dto.setPhoto(null);
                        return Mono.just(dto);
                    }
                });
    }

    @Override
    public Mono<Void> deleteCustomer(String customerId) {
        return customerRepo.deleteById(customerId);
    }


    @Override
    public Mono<CustomerResponseDTO> deleteCustomerByCustomerId(String customerId) {
        return customerRepo.findCustomerByCustomerId(customerId)
                .switchIfEmpty(Mono.defer(() -> Mono.error(new NotFoundException("Customer id not found: " + customerId))))
                .flatMap(found -> customerRepo.delete(found)
                        .then(Mono.just(found)))
                .map(EntityDTOUtil::toCustomerReponseDTO);
    }

    @Override
    public Mono<CustomerResponseDTO> updateCustomer(Mono<CustomerRequestDTO> customerRequestDTO, String customerId) {
        return customerRepo.findCustomerByCustomerId(customerId)
                .flatMap(customer -> customerRequestDTO
                        .map(EntityDTOUtil::toCustomer)
                        .doOnNext(o -> {
                            o.setId(customer.getId());
                            o.setCustomerId(customerId);
                        })
                )
                .flatMap(customerRepo::save)
                .map(EntityDTOUtil::toCustomerReponseDTO);
    }

    @Override
    public Mono<CustomerResponseDTO> updateCustomerPhoto(String customerId, FileRequestDTO photo) {
        return customerRepo.findCustomerByCustomerId(customerId)
                .switchIfEmpty(Mono.error(new NotFoundException("Customer not found with id: " + customerId)))
                .flatMap(existingCustomer -> {
                    Mono<FileResponseDTO> fileOperation;
                    
                    if (existingCustomer.getPhotoId() != null && !existingCustomer.getPhotoId().isEmpty()) {
                        fileOperation = filesServiceClient.updateFile(existingCustomer.getPhotoId(), photo)
                                .onErrorResume(e -> {
                                    log.warn("Photo file {} not found or error updating, creating new file instead: {}", 
                                            existingCustomer.getPhotoId(), e.getMessage());
                                    return filesServiceClient.addFile(photo);
                                });
                    } else {
                        fileOperation = filesServiceClient.addFile(photo);
                    }
                    
                    return fileOperation
                            .flatMap(fileResp -> {
                                existingCustomer.setPhotoId(fileResp.getFileId());
                                return customerRepo.save(existingCustomer)
                                        .map(savedCustomer -> {
                                            CustomerResponseDTO dto = EntityDTOUtil.toCustomerReponseDTO(savedCustomer);
                                            dto.setPhoto(fileResp);
                                            return dto;
                                        });
                            });
                });
    }



    @Override
    public Flux<CustomerResponseDTO> getAllCustomers() {
        return customerRepo.findAll()
                .map(EntityDTOUtil::toCustomerReponseDTO);
    }

    @Override
    public Mono<Long> getTotalNumberOfCustomersWithFilters(String customerId, String firstName, String lastName, String phoneNumber, String city) {
         Predicate<Customer> filterCriteria = customer ->
                (customerId == null || customer.getCustomerId().equals(customerId)) &&
                (firstName == null || customer.getFirstName().equals(firstName)) &&
                (lastName == null || customer.getLastName().equals(lastName)) &&
                (phoneNumber == null || customer.getTelephone().equals(phoneNumber)) &&
                (city == null || customer.getCity().equals(city));

        return customerRepo.findAll()
                .filter(filterCriteria) // Apply filtering
                .map(EntityDTOUtil::toCustomerReponseDTO)
                .count();
    }

    @Override
    public Flux<CustomerResponseDTO> getAllCustomersPagination(Pageable pageable,
                                                               String customerId,
                                                               String firstName,
                                                               String lastName,
                                                               String phoneNumber,
                                                               String city){

        Predicate<Customer> filterCriteria = customer ->
                (customerId == null || customer.getCustomerId().equals(customerId)) &&
                (firstName == null || customer.getFirstName().equals(firstName)) &&
                (lastName == null || customer.getLastName().equals(lastName)) &&
                (phoneNumber == null || customer.getTelephone().equals(phoneNumber)) &&
                (city == null || customer.getCity().equals(city));

        if(customerId == null && firstName == null && lastName == null && phoneNumber == null && city == null){
            return customerRepo.findAll()
                    .map(EntityDTOUtil::toCustomerReponseDTO)
                    .skip(pageable.getPageNumber() * pageable.getPageSize())
                    .take(pageable.getPageSize());
        } else {
            return customerRepo.findAll()
                    .filter(filterCriteria)
                    .map(EntityDTOUtil::toCustomerReponseDTO)
                    .skip(pageable.getPageNumber() * pageable.getPageSize())
                    .take(pageable.getPageSize());
        }
    }

    @Override
    public Mono<CustomerResponseDTO> deleteCustomerPhoto(String customerId) {
        return customerRepo.findCustomerByCustomerId(customerId)
                .switchIfEmpty(Mono.error(new NotFoundException("Customer not found with id: " + customerId)))
                .flatMap(existingCustomer -> {
                    String photoId = existingCustomer.getPhotoId();
                    if (photoId != null && !photoId.isEmpty()) {

                        existingCustomer.setPhotoId(null);
                        return customerRepo.save(existingCustomer)
                                .flatMap(savedCustomer ->

                                        filesServiceClient.deleteFile(photoId)
                                                .onErrorResume(e -> {
                                                    log.error("Error deleting photo file {}: {}", photoId, e.getMessage());
                                                    return Mono.empty();
                                                })
                                                .thenReturn(savedCustomer)
                                )
                                .map(EntityDTOUtil::toCustomerReponseDTO);
                    }

                    return Mono.just(EntityDTOUtil.toCustomerReponseDTO(existingCustomer));
                });
    }


}
