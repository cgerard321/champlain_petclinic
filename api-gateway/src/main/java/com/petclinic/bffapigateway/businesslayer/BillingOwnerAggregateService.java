package com.petclinic.bffapigateway.businesslayer;

import com.petclinic.bffapigateway.domainclientlayer.BillServiceClient;
import com.petclinic.bffapigateway.domainclientlayer.CustomersServiceClient;
import com.petclinic.bffapigateway.dtos.BillDetails;
import com.petclinic.bffapigateway.dtos.OwnerDetails;
import com.petclinic.bffapigateway.dtos.TransactionDetails;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
//Marina Melnichuk
@Service
@AllArgsConstructor
public class BillingOwnerAggregateService {


    private final BillServiceClient billServiceClient;
    private final CustomersServiceClient customersServiceClient;

    public Mono<TransactionDetails> getTransaction(Integer ownerId) {
        return Mono.zip(
                this.billServiceClient.getBillByOwnerId(ownerId),
                this.customersServiceClient.getOwner(ownerId)

        ).map(this::combine);
    }

    private TransactionDetails combineAll (Tuple2<BillDetails, OwnerDetails> tuple) {
        return TransactionDetails.create(
                tuple.getT1(),
                tuple.getT2()
        );
    }
    public Flux<TransactionDetails> getTransactions(Integer ownerId) {
        return Flux.zip(
                this.billServiceClient.getAllBilling(),
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

