package com.petclinic.customersservice.util;

import com.petclinic.customersservice.customersExceptions.ApplicationExceptions;
import com.petclinic.customersservice.presentationlayer.CustomerRequestDTO;
import com.petclinic.customersservice.presentationlayer.PetRequestDTO;
import com.petclinic.customersservice.presentationlayer.PetTypeRequestDTO;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public class Validator {
    public static UnaryOperator<Mono<CustomerRequestDTO>> validateCustomer() {
        return customerRequest -> customerRequest
                .filter(hasStringValue(CustomerRequestDTO::getFirstName))
                .switchIfEmpty(ApplicationExceptions.missingCustomerFirstName())
                .filter(hasStringValue(CustomerRequestDTO::getLastName))
                .switchIfEmpty(ApplicationExceptions.missingCustomerLastName())
                .filter(hasStringValue(CustomerRequestDTO::getAddress))
                .switchIfEmpty(ApplicationExceptions.missingCustomerAddress())
                .filter(hasStringValue(CustomerRequestDTO::getCity))
                .switchIfEmpty(ApplicationExceptions.missingCustomerCity())
                .filter(hasStringValue(CustomerRequestDTO::getProvince))
                .switchIfEmpty(ApplicationExceptions.missingCustomerProvince())
                .filter(validPhoneNumber())
                .switchIfEmpty(ApplicationExceptions.invalidCustomerPhoneNumber());
    }

    public static UnaryOperator<Mono<PetRequestDTO>> validatePet() {
        return petRequest -> petRequest
                .filter(hasValidId(PetRequestDTO::getCustomerId))
                .switchIfEmpty(ApplicationExceptions.invalidCustomerId())
                .filter(hasStringValue(PetRequestDTO::getName))
                .switchIfEmpty(ApplicationExceptions.missingPetName())
                .filter(hasValidId(PetRequestDTO::getPetTypeId))
                .switchIfEmpty(ApplicationExceptions.invalidPetTypeId())
                .filter(validBirthDate())
                .switchIfEmpty(ApplicationExceptions.invalidPetBirthDate())
                .filter(validWeight())
                .switchIfEmpty(ApplicationExceptions.invalidPetWeight());
    }

    public static UnaryOperator<Mono<PetTypeRequestDTO>> validatePetType() {
        return petTypeRequest -> petTypeRequest
                .filter(hasStringValue(PetTypeRequestDTO::getName))
                .switchIfEmpty(ApplicationExceptions.missingPetTypeName())
                .filter(hasStringValue(PetTypeRequestDTO::getPetTypeDescription))
                .switchIfEmpty(ApplicationExceptions.missingPetTypeDescription());
    }

    public static <T> Predicate<T> hasStringValue(Function<T, String> getter) {
        return obj -> {
            String value = getter.apply(obj);
            return Objects.nonNull(value) && !value.trim().isEmpty();
        };
    }

    public static Predicate<CustomerRequestDTO> validPhoneNumber(){
        return customerRequestDTO -> Objects.nonNull(customerRequestDTO.getTelephone()) && customerRequestDTO.getTelephone().matches("^[0-9]{10}$");
    }

    public static <T> Predicate<T> hasValidId(Function<T, String> getter) {
        return obj -> {
            String value = getter.apply(obj);
            return Objects.nonNull(value) && value.length() == 36;
        };
    }

    public static Predicate<PetRequestDTO> validWeight() {
        return petRequestDTO -> {
            try {
                return new BigDecimal(petRequestDTO.getWeight().trim()).compareTo(BigDecimal.ZERO) > 0;
            } catch (Exception e) {
                return false;
            }
        };
    }

    public static Predicate<PetRequestDTO> validBirthDate() {
        return petRequestDTO -> petRequestDTO.getBirthDate() != null && !petRequestDTO.getBirthDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().isAfter(LocalDate.now());
    }
}