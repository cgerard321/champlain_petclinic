# Scheduled Tasks in Spring Boot

Back to [Backend Standards](./java-coding-standards.md)

<!-- TOC -->

- [Scheduled Tasks in Spring Boot](#scheduled-tasks-in-spring-boot)
  - [Enabling Scheduling](#enabling-scheduling)
  - [Scheduled Task Example](#scheduled-task-example)
  - [Cron Expression Reference](#cron-expression-reference)
  - [Unit Testing Scheduled Tasks](#unit-testing-scheduled-tasks)
  - [Test Dependency Guidelines](#test-dependency-guidelines)

<!-- TOC -->

## Enabling Scheduling

Add `@EnableScheduling` to your main application class:

```java
@SpringBootApplication
@EnableScheduling
public class BillingServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(BillingServiceApplication.class, args);
    }
}
```

## Scheduled Task Example

Create a component class and use the `@Scheduled` annotation. Example for marking overdue bills:

```java
@Component
public class BillingScheduler {
    private static final int MAX_RETRIES = 3; // example constant for the snippet
    private final BillService billService;

    public BillingScheduler(BillService billService) {
        this.billService = billService;
    }

    /**
     * Runs every day at midnight to mark overdue bills.
     * Retries up to 3 times with 5s delay between attempts.
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void markOverdueBills() {
        // The actual MAX_RETRIES constant is defined in BillingScheduler.java; use the same constant in your implementation
        // @Scheduled methods should subscribe to reactive chains if they are fire-and-forget.
        billService.updateOverdueBills()
            .retryWhen(
                reactor.util.retry.Retry.backoff(MAX_RETRIES, java.time.Duration.ofSeconds(5))
                    .doBeforeRetry(retrySignal ->
                        org.slf4j.LoggerFactory.getLogger(BillingScheduler.class)
                            .warn("Retrying updateOverdueBills (attempt {}/{}): {}", retrySignal.totalRetries() + 1, MAX_RETRIES, retrySignal.failure())
                    )
            )
            .doOnError(error -> org.slf4j.LoggerFactory.getLogger(BillingScheduler.class)
                    .error("Permanently failed to update overdue bills after {} retries", MAX_RETRIES, error)
            )
            .subscribe();
    }
}
```

## Cron Expression Reference

- Format: `second minute hour day-of-month month day-of-week [year]`
- Example: `0 0 0 * * ?` means at 00:00:00 (midnight) every day
- `?` is used for 'no specific value' in day-of-month or day-of-week
- See [Spring CronExpression docs](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/scheduling/support/CronExpression.html)

## Unit Testing Scheduled Tasks

Use Mockito and JUnit Jupiter to verify that your scheduled method calls the correct service method. Example:

```java
@ExtendWith(MockitoExtension.class)
public class BillingSchedulerTest {
    @Mock
    private BillService billService;

    @InjectMocks
    private BillingScheduler billingScheduler;

    @Test
    public void testMarkOverdueBillsRuns() {
        Mockito.when(billService.updateOverdueBills()).thenReturn(reactor.core.publisher.Mono.empty());
        // This unit test calls the scheduled method directly; it verifies the method triggers the service call.
        // It does NOT verify that Spring's scheduling infrastructure actually runs the method on a schedule.
        billingScheduler.markOverdueBills();
        Mockito.verify(billService, Mockito.times(1)).updateOverdueBills();
    }
}
```

**What does this test check?**

- Verifies that when the scheduled method is called, it triggers the service method (`updateOverdueBills`).

## Test Dependency Guidelines

- Use only `spring-boot-starter-test` and `io.projectreactor:reactor-test` for most Spring Boot projects
- No need to add Mockito or JUnit separately unless a specific version/feature is required

Example Gradle configuration:

```gradle
testImplementation 'org.springframework.boot:spring-boot-starter-test'
testImplementation 'io.projectreactor:reactor-test'
```

- It does NOT test the actual scheduled execution (i.e., that Spring runs it on a schedule).
- It does NOT test the business logic inside `updateOverdueBills()`.

For full coverage, add separate tests for your business logic and consider integration tests for scheduled execution.

## Further Reading

- [Baeldung: Spring Scheduled Tasks](https://www.baeldung.com/spring-scheduled-tasks)
- [Spring Guide: Scheduling Tasks](https://spring.io/guides/gs/scheduling-tasks)

---

### Note on Dependency Management

Before adding new dependencies to `billing-service/build.gradle`, always check if the required libraries are already included via existing starter dependencies (e.g., `spring-boot-starter-test`). This helps keep your build clean and avoids unnecessary duplication.

For more Java practices, see [java-coding-standards.md](./java-coding-standards.md).
