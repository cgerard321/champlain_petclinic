package com.petclinic.customersservice.util;

import com.petclinic.customersservice.customersExceptions.ApplicationExceptions;
import com.petclinic.customersservice.presentationlayer.OwnerRequestDTO;
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
    public static UnaryOperator<Mono<OwnerRequestDTO>> validateOwner() {
        return ownerRequest -> ownerRequest
                .filter(hasStringValue(OwnerRequestDTO::getFirstName))
                .switchIfEmpty(ApplicationExceptions.missingOwnerFirstName())
                .filter(hasStringValue(OwnerRequestDTO::getLastName))
                .switchIfEmpty(ApplicationExceptions.missingOwnerLastName())
                .filter(hasStringValue(OwnerRequestDTO::getAddress))
                .switchIfEmpty(ApplicationExceptions.missingOwnerAddress())
                .filter(hasStringValue(OwnerRequestDTO::getCity))
                .switchIfEmpty(ApplicationExceptions.missingOwnerCity())
                .filter(hasStringValue(OwnerRequestDTO::getProvince))
                .switchIfEmpty(ApplicationExceptions.missingOwnerProvince())
                .filter(validPhoneNumber())
                .switchIfEmpty(ApplicationExceptions.invalidOwnerPhoneNumber());
    }

    public static UnaryOperator<Mono<PetRequestDTO>> validatePet() {
        return petRequest -> petRequest
                .filter(hasValidId(PetRequestDTO::getOwnerId))
                .switchIfEmpty(ApplicationExceptions.invalidOwnerId())
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

    public static Predicate<OwnerRequestDTO> validPhoneNumber(){
        return ownerRequestDTO -> Objects.nonNull(ownerRequestDTO.getTelephone()) && ownerRequestDTO.getTelephone().matches("^[0-9]{10}$");
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