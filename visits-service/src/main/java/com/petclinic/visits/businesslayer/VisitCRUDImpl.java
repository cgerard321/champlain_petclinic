package com.petclinic.visits.businesslayer;

import com.petclinic.visits.datalayer.VisitRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VisitCRUDImpl implements VisitCRUD {

    private static final Logger LOG = LoggerFactory.getLogger(VisitCRUD.class);

    private final VisitRepository repository;

    public VisitCRUDImpl(VisitRepository repository){
        this.repository = repository;
    }


    @Override
    public void deleteVisit(int visitId) {
        LOG.debug("deleteProduct: trying to delete entity with productId: {}", visitId);
        repository.findById(visitId).ifPresent(e -> repository.delete(e));
    }
}
