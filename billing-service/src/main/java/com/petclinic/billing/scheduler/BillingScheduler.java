package com.petclinic.billing.scheduler;

import com.petclinic.billing.businesslayer.BillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.util.retry.Retry;
import reactor.core.publisher.Mono;
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
     * Runs every day at midnight to mark overdue bills.
     * Implements a retry system (similar to mailer) using Reactor's Retry.backoff:
     *   - Retries up to MAX_RETRIES times with RETRY_DELAY_MS delay between attempts
     *   - Logs each retry and permanent failure
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public Mono<Void> markOverdueBills() {
        return billService.updateOverdueBills()
            .retryWhen(
                Retry.backoff(MAX_RETRIES, java.time.Duration.ofMillis(RETRY_DELAY_MS))
                    .doBeforeRetry(retrySignal ->
                        log.warn("Retrying updateOverdueBills (attempt {}/{}): {}", retrySignal.totalRetries() + 1, MAX_RETRIES, retrySignal.failure())
                    )
            )
            .doOnError(error -> log.error("Permanently failed to update overdue bills after {} retries", MAX_RETRIES, error));
    }
}
