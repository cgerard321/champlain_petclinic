package com.petclinic.billing.scheduler;

import com.petclinic.billing.businesslayer.BillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.util.retry.Retry;
// Note: method below subscribes to the reactive chain for fire-and-forget scheduling
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class BillingScheduler {
    private static final Logger log = LoggerFactory.getLogger(BillingScheduler.class);
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 5000;
    private final BillService billService;

    public BillingScheduler(BillService billService) {
        this.billService = billService;
    }

    /**
     * Runs every 24 hours to mark overdue bills.
     * Sequential execution is enforced: the next run will not start until the previous one completes,
     * preventing concurrent executions.
     * Implements a retry system (similar to mailer) using Reactor's Retry.backoff:
     *   - Retries up to MAX_RETRIES times with RETRY_DELAY_MS delay between attempts
     *   - Logs each retry and permanent failure
     */
    // Using fixedDelay to prevent concurrent executions if a previous run is still in progress.
    @Scheduled(fixedDelay = 86_400_000)
    public void markOverdueBills() {
        // subscribe() because @Scheduled does not auto-subscribe reactive types; this is fire-and-forget
        billService.updateOverdueBills()
            .retryWhen(
                Retry.backoff(MAX_RETRIES, java.time.Duration.ofMillis(RETRY_DELAY_MS))
                    .doBeforeRetry(retrySignal ->
                        log.warn("Retrying updateOverdueBills (attempt {}/{}): {}", retrySignal.totalRetries() + 1, MAX_RETRIES, retrySignal.failure())
                    )
            )
            .doOnError(error -> log.error("Permanently failed to update overdue bills after {} retries: {}", MAX_RETRIES, error))
            .subscribe(
                null,
                error -> log.error("Subscription error in scheduled task", error),
                () -> log.debug("Successfully completed overdue bills update")
            );
    }
}
