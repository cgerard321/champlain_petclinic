package com.petclinic.customersservice.customersExceptions;

import com.petclinic.customersservice.customersExceptions.exceptions.InvalidInputException;
import com.petclinic.customersservice.customersExceptions.exceptions.UnprocessableEntityException;
import reactor.core.publisher.Mono;

public class ApplicationExceptions {
    public static <T> Mono<T> ownerNotFound(String ownerId) {
        return Mono.error(new InvalidInputException("Owner with id: " + ownerId + " is not found"));
    }

    public static <T> Mono<T> petNotFound(String petId) {
        return Mono.error(new InvalidInputException("Pet with id: " + petId + " is not found"));
    }

    public static <T> Mono<T> petTypeNotFound(String petTypeId) {
        return Mono.error(new InvalidInputException("PetType with id: " + petTypeId + " is not found"));
    }

    public static <T> Mono<T> invalidOwnerId(String ownerId) {
        return Mono.error(new InvalidInputException("Owner id: " + ownerId + " is invalid"));
    }

    public static <T> Mono<T> invalidOwnerId() {
        return Mono.error(new InvalidInputException("Owner id is invalid"));
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

    public static <T> Mono<T> missingOwnerLastName() {
        return Mono.error(new UnprocessableEntityException("last name is required"));
    }

    public static <T> Mono<T> missingOwnerAddress() {
        return Mono.error(new UnprocessableEntityException("Address is required"));
    }

    public static <T> Mono<T> missingOwnerCity() {
        return Mono.error(new UnprocessableEntityException("City is required"));
    }

    public static <T> Mono<T> missingOwnerProvince() {
        return Mono.error(new UnprocessableEntityException("Province is required"));
    }

    public static <T> Mono<T> invalidOwnerPhoneNumber() {
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