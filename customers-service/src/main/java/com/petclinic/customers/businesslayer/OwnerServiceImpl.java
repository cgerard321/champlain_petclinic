package com.petclinic.customers.businesslayer;

import com.petclinic.customers.datalayer.Owner;
import com.petclinic.customers.datalayer.OwnerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class OwnerServiceImpl implements OwnerService {

    private static final Logger LOG = LoggerFactory.getLogger(OwnerServiceImpl.class);


    /*
    private final OwnerRepository repository;



    @Override
    public Owner findById(int Id) {
        Owner owner = repository.findById(Id)
                .orElseThrow(() -> new NotFoundException("No product found for productId: " + productId));

        return null;
    }
    */


    @Override
    public Owner findById(int Id) {
        return null;
    }

    @Override
    public Owner CreateOwner(Owner owner) {
        return null;
    }

    @Override
    public void deleteOwner(int Id) {

    }
}
