package com.petclinic.customers.businesslayer;

import com.petclinic.customers.datalayer.Owner;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

@Service
public interface OwnerService {

        /**
         * IF YOU SEE NEXT METHOD, EVERYTHING IS OK
         */
        //FIND OWNER
        public Optional<Owner> findByOwnerId(int Id);

        //FIND ALL OWNER
        public List<Owner> findAll();

        //UPDATE OWNER
        public void updateOwner();

        //CREATE OWNER
        public Owner createOwner(Owner owner);

        //DELETE OWNER
        public void deleteOwner(int Id);

        //public Optional<Owner> findOwnerLogin(String username, String password);




}
