package com.petclinic.billing.scheduler;

import com.petclinic.billing.businesslayer.BillService;
import org.junit.jupiter.api.Test;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
public class BillingSchedulerTest {

    @Mock
    private BillService billService;

    @InjectMocks
    private BillingScheduler billingScheduler;

    @Test
    public void testMarkOverdueBillsRuns() throws InterruptedException {
        Mockito.when(billService.updateOverdueBills()).thenReturn(Mono.empty());
        // Call the reactive method and block to allow the chain to execute synchronously in the test
        billingScheduler.markOverdueBills().block();
        Mockito.verify(billService, Mockito.times(1)).updateOverdueBills();
    }

    @Test
    public void testMarkOverdueBillsHandlesError() throws InterruptedException {
        Mockito.when(billService.updateOverdueBills())
            .thenReturn(Mono.error(new RuntimeException("Simulated error")));
        billingScheduler.markOverdueBills().onErrorResume(e -> Mono.empty()).block();
        Mockito.verify(billService, Mockito.times(1)).updateOverdueBills();
    }

    @Test
    public void testMarkOverdueBillsHandlesCompletedMono() throws InterruptedException {
        Mockito.when(billService.updateOverdueBills()).thenReturn(Mono.justOrEmpty(null));
        billingScheduler.markOverdueBills().block();
        Mockito.verify(billService, Mockito.times(1)).updateOverdueBills();
    }
}
