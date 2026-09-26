package com.petclinic.customersservice.customersExceptions;

import com.petclinic.customersservice.customersExceptions.exceptions.InvalidInputException;
import com.petclinic.customersservice.customersExceptions.exceptions.UnprocessableEntityException;
import reactor.core.publisher.Mono;

public class ApplicationExceptions {
    public static <T> Mono<T> customerNotFound(String customerId) {
        return Mono.error(new InvalidInputException("Customer with id: " + customerId + " is not found"));
    }

    public static <T> Mono<T> petNotFound(String petId) {
        return Mono.error(new InvalidInputException("Pet with id: " + petId + " is not found"));
    }

    public static <T> Mono<T> petTypeNotFound(String petTypeId) {
        return Mono.error(new InvalidInputException("PetType with id: " + petTypeId + " is not found"));
    }

    public static <T> Mono<T> invalidCustomerId(String customerId) {
        return Mono.error(new InvalidInputException("Customer id: " + customerId + " is invalid"));
    }

    public static <T> Mono<T> invalidCustomerId() {
        return Mono.error(new InvalidInputException("Customer id is invalid"));
    }

    public static <T> Mono<T> invalidPetId(String petId) {
        return Mono.error(new InvalidInputException("Pet id: " + petId + " is invalid"));
    }

    public static <T> Mono<T> invalidPetTypeId(String petTypeId) {
        return Mono.error(new InvalidInputException("PetType id: " + petTypeId + " is invalid"));
    }

    public static <T> Mono<T> invalidPetTypeId() {
        return Mono.error(new InvalidInputException("PetType id is invalid"));
    }

    public static <T> Mono<T> missingOwnerFirstName() {
        return Mono.error(new UnprocessableEntityException("first name is required"));
    }

    public static <T> Mono<T> missingCustomerLastName() {
        return Mono.error(new UnprocessableEntityException("last name is required"));
    }

    public static <T> Mono<T> missingCustomerAddress() {
        return Mono.error(new UnprocessableEntityException("Address is required"));
    }

    public static <T> Mono<T> missingCustomerCity() {
        return Mono.error(new UnprocessableEntityException("City is required"));
    }

    public static <T> Mono<T> missingCustomerProvince() {
        return Mono.error(new UnprocessableEntityException("Province is required"));
    }

    public static <T> Mono<T> invalidCustomerPhoneNumber() {
        return Mono.error(new UnprocessableEntityException("Phone number must be 10 digits"));
    }

    public static <T> Mono<T> missingPetName() {
        return Mono.error(new UnprocessableEntityException("name is required"));
    }

    public static <T> Mono<T> invalidPetBirthDate() {
        return Mono.error(new UnprocessableEntityException("birth date is invalid, must not be more recent than today"));
    }

    public static <T> Mono<T> invalidPetWeight() {
        return Mono.error(new UnprocessableEntityException("weight is invalid, must be greater than 0"));
    }

    public static <T> Mono<T> missingPetTypeName(){
        return Mono.error(new UnprocessableEntityException("name is required"));
    }

    public static <T> Mono<T> missingPetTypeDescription(){
        return Mono.error(new UnprocessableEntityException("description is required"));
    }
}