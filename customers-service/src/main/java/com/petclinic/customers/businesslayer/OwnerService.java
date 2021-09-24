package com.petclinic.customers.businesslayer;

import com.petclinic.customers.datalayer.Owner;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

@Service
public interface OwnerService {

        //FIND OWNER IN DATABASE
        public Optional<Owner> findByOwnerId(int Id);

        public List<Owner> findAll();

        //public Optional<Owner> findOwnerLogin(String username, String password);

        public void updateOwner();

        //CREATE OWNER IN DATABASE
        public Owner CreateOwner(Owner owner);

        //DELETE OWNER IN DATABASE
        public void deleteOwner(int Id);



}
