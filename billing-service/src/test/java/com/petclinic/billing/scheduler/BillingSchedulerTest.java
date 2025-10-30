
package com.petclinic.billing.scheduler;

import com.petclinic.billing.businesslayer.BillService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
public class BillingSchedulerTest {

    @Mock
    private BillService billService;

    @InjectMocks
    private BillingScheduler billingScheduler;

    @Test
    public void testMarkOverdueBillsRuns() {
        Mockito.when(billService.updateOverdueBills()).thenReturn(reactor.core.publisher.Mono.empty());
        billingScheduler.markOverdueBills();
        Mockito.verify(billService, Mockito.times(1)).updateOverdueBills();
    }
}
