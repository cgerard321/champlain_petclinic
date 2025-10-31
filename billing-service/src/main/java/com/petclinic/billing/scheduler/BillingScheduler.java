
package com.petclinic.billing.scheduler;

import com.petclinic.billing.businesslayer.BillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class BillingScheduler {
    @Autowired
    private BillService billService;

    // Runs every day at midnight to mark overdue bills
    @Scheduled(cron = "0 0 0 * * ?")
    public void markOverdueBills() {
        billService.updateOverdueBills().subscribe();
    }
}
