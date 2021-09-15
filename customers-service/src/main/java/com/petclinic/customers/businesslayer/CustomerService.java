package com.petclinic.customers.businesslayer;

import com.petclinic.customers.datalayer.Owner;

public interface CustomerService {

        //FIND OWNER IN DATABASE
        public Owner findOwner(int Id);

        //CREATE OWNER IN DATABASE
        public Owner CreateOwner(Owner owner);

        //DELETE OWNER IN DATABASE
        public void deleteCustomer(int Id);

}
