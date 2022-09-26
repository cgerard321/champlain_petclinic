package com.petclinic.bffapigateway.presentationlayer;

import com.petclinic.bffapigateway.domainclientlayer.BillServiceClient;
import com.petclinic.bffapigateway.domainclientlayer.CustomersServiceClient;
import com.petclinic.bffapigateway.dtos.BillDetails;
import com.petclinic.bffapigateway.dtos.OwnerDetails;
import com.petclinic.bffapigateway.dtos.TransactionDetails;
import lombok.AllArgsConstructor;
import lombok.var;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import javax.annotation.Resource;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@AllArgsConstructor
public class BillingOwnerAggregate {

    private final BillServiceClient billServiceClient;
    private final CustomersServiceClient customersServiceClient;

    public Mono<TransactionDetails> getTransaction(Integer ownerId) {
        return Mono.zip(
                this.billServiceClient.getBillByOwnerId(ownerId),
                this.customersServiceClient.getOwner(ownerId)

        ).map(this::combine);
    }

    private TransactionDetails combine (Tuple2<BillDetails, OwnerDetails> tuple) {
        return TransactionDetails.create(
                tuple.getT1(),
                tuple.getT2()
        );
    }
}

