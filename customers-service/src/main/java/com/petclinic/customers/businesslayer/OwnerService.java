package com.petclinic.customers.businesslayer;

import com.petclinic.customers.datalayer.Owner;
import com.petclinic.customers.datalayer.Pet;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

public interface OwnerService {
        
        //FIND OWNER
        public Optional<Owner> findByOwnerId(int Id);

        //FIND ALL OWNER
        public List<Owner> findAll();

        //UPDATE OWNER
        public Owner updateOwner(int id, Owner owner);

        //CREATE OWNER
        public Owner createOwner(Owner owner);

        //DELETE OWNER
        public void deleteOwner(int id);

}
