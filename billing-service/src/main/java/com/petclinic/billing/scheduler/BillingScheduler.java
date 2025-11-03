
package com.petclinic.billing.scheduler;


import com.petclinic.billing.businesslayer.BillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.util.retry.Retry;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Component
public class BillingScheduler {
    private static final Logger log = LoggerFactory.getLogger(BillingScheduler.class);
    private final BillService billService;

    public BillingScheduler(BillService billService) {
        this.billService = billService;
    }

    /**
     * Runs every day at midnight to mark overdue bills.
     * Implements a retry system (similar to mailer) using Reactor's Retry.backoff:
     *   - Retries up to 3 times with 5s delay between attempts
     *   - Logs each retry and permanent failure
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void markOverdueBills() {
        final int MAX_RETRIES = 3;
        final long RETRY_DELAY_MS = 5000;
        billService.updateOverdueBills()
            .retryWhen(
                Retry.backoff(MAX_RETRIES, java.time.Duration.ofMillis(RETRY_DELAY_MS))
                    .doBeforeRetry(retrySignal ->
                        log.warn("Retrying updateOverdueBills (attempt {}/{}): {}", retrySignal.totalRetries() + 1, MAX_RETRIES, retrySignal.failure().toString())
                    )
            )
            .subscribe(
                null,
                error -> log.error("Permanently failed to update overdue bills after {} retries", MAX_RETRIES, error)
            );
    }
}
