package com.petclinic.customers.businesslayer;

import com.petclinic.customers.datalayer.Owner;

public interface OwnerService {

        //FIND OWNER IN DATABASE
        public Owner findById(int Id);

        //CREATE OWNER IN DATABASE
        public Owner CreateOwner(Owner owner);

        //DELETE OWNER IN DATABASE
        public void deleteOwner(int Id);

}
