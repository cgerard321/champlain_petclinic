package com.petclinic.bffapigateway.presentationlayer;

import com.petclinic.bffapigateway.domainclientlayer.BillServiceClient;
import com.petclinic.bffapigateway.domainclientlayer.CustomersServiceClient;
import com.petclinic.bffapigateway.dtos.BillDetails;
import com.petclinic.bffapigateway.dtos.TransactionDetails;
import lombok.var;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
public class BillingOwnerAggregate {
    @Resource
    private CustomersServiceClient customersServiceClient;

    @Resource
    private BillServiceClient billServiceClient;

    private int billId, ownerId;
    @RequestMapping(path = "/aggregate/transaction/{billId}")

    public TransactionDetails getTransaction(final @PathVariable int billId) {
        //var owner = customersServiceClient.getOwner(ownerId);
        var bill = billServiceClient.getBilling(billId);
        var owners= customersServiceClient.getOwners();
        var ownerId = bill.map(o -> o.getCustomerId());
        AtomicInteger ownerId2 = new AtomicInteger();
        ownerId.subscribe(o -> ownerId2.set(o.intValue()));
       // bill.subscribe(r->{id=r.getBillId();});
        TransactionDetails transactionDetails = new TransactionDetails(billId, ownerId2.intValue());
        //Mono<List<BillDetails>> billMonoList = billServiceClient.getAllBilling().collectList();
       // Mono<List<OwnerDetails>> ownerMonoList = customersServiceClient.getOwners().collectList();

        return transactionDetails;



        //return t;
    }
    //@GetMapping("/transactions")
    //public Mono<TransactionDetails> getTransaction() {

        /*var owners = customersServiceClient.getOwners();
        //var owner = customersServiceClient.getOwner(ownerId);
        var bills = billServiceClient.getAllBilling();
        List<BillDetails> billList = bills.collectList().block();
        List<TransactionDetails> transactionsList = new ArrayList<TransactionDetails>();
        //transactionsList.stream()
                //.map(BillDetails::getBillId)
                //.collect(Collectors.toList());

        transactionsList.add(new TransactionDetails(1, 1));


        TransactionDetails t= new TransactionDetails(1,1);
       // var transaction = transactionsList.stream().iterator().next();
        return t;
    }*/
}

