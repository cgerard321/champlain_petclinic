package com.petclinic.customersservice.util;

import com.petclinic.customersservice.business.OwnerAggregate;
import com.petclinic.customersservice.business.PetAggregate;
import com.petclinic.customersservice.data.Owner;
import com.petclinic.customersservice.data.Pet;
import org.springframework.beans.BeanUtils;

public class EntityAggregateUtil {

    public static OwnerAggregate toOwnerAggregate(Owner owner) {
        OwnerAggregate ownerAggregate = new OwnerAggregate();
        BeanUtils.copyProperties(owner, ownerAggregate);
        return ownerAggregate;
    }

    public static Owner toOwner(OwnerAggregate ownerAggregate) {
        Owner owner = new Owner();
        BeanUtils.copyProperties(ownerAggregate, owner);
        return owner;
    }

    public static PetAggregate toPetAggregate(Pet pet) {
        PetAggregate petAggregate = new PetAggregate();
        BeanUtils.copyProperties(pet, petAggregate);
        return petAggregate;
    }

    public static Pet toPet(PetAggregate petAggregate) {
        Pet pet = new Pet();
        BeanUtils.copyProperties(petAggregate, pet);
        return pet;
    }

}
