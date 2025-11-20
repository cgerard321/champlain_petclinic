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
    public void testMarkOverdueBillsRuns() {
        Mockito.when(billService.updateOverdueBills()).thenReturn(Mono.empty());
        // The scheduler subscribes internally (fire-and-forget). Verify interaction.
        billingScheduler.markOverdueBills();
        Mockito.verify(billService, Mockito.times(1)).updateOverdueBills();
    }

    @Test
    public void testMarkOverdueBillsHandlesError() {
        Mockito.when(billService.updateOverdueBills())
            .thenReturn(Mono.error(new RuntimeException("Simulated error")));
        billingScheduler.markOverdueBills();
        Mockito.verify(billService, Mockito.times(1)).updateOverdueBills();
    }

    @Test
    public void testMarkOverdueBillsHandlesCompletedMono() {
        Mockito.when(billService.updateOverdueBills()).thenReturn(Mono.justOrEmpty(null));
        billingScheduler.markOverdueBills();
        Mockito.verify(billService, Mockito.times(1)).updateOverdueBills();
    }
}
