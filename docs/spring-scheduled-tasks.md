# Scheduled Tasks in Spring Boot

Back to [Backend Standards](./java-coding-standards.md)

<!-- TOC -->

- [Scheduled Tasks in Spring Boot](#scheduled-tasks-in-spring-boot)
  - [Enabling Scheduling](#enabling-scheduling)
  - [Scheduled Task Example](#scheduled-task-example)
  - [Scheduling Approaches](#scheduling-approaches)
  - [Cron Expression Reference](#cron-expression-reference)
  - [Unit Testing Scheduled Tasks](#unit-testing-scheduled-tasks)
  - [Test Dependency Guidelines](#test-dependency-guidelines)

<!-- /TOC -->

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

Here's how to create a scheduled task that runs every 24 hours:

```java
@Component
public class BillingScheduler {
    private static final Logger log = LoggerFactory.getLogger(BillingScheduler.class);
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 5000; // 5 seconds
    private final BillService billService;

    public BillingScheduler(BillService billService) {
        this.billService = billService;
    }

    @Scheduled(fixedDelay = 86_400_000) // 24 hours
    public void markOverdueBills() {
        billService.updateOverdueBills()
            .retryWhen(
                reactor.util.retry.Retry.backoff(MAX_RETRIES, java.time.Duration.ofMillis(RETRY_DELAY_MS))
                    .doBeforeRetry(retrySignal ->
                        org.slf4j.LoggerFactory.getLogger(BillingScheduler.class)
                            .warn("Retrying updateOverdueBills (attempt {}/{}): {}", retrySignal.totalRetries() + 1, MAX_RETRIES, retrySignal.failure())
                    )
            )
            .doOnError(error -> org.slf4j.LoggerFactory.getLogger(BillingScheduler.class)
                    .error("Permanently failed to update overdue bills after {} retries: {}", MAX_RETRIES, error)
            )
            .subscribe(
                null,
                error -> log.error("Subscription error in scheduled task", error),
                () -> log.debug("Successfully completed overdue bills update")
            );
    }
}
```

A few things to note:

- We use `fixedDelay` instead of `cron` to prevent overlapping runs
- The `.subscribe()` call is needed because Spring doesn't automatically subscribe to reactive chains
- We include retry logic since network/database calls can fail

## Scheduling Approaches

Spring offers two main ways to schedule tasks:

### Fixed Delay - Wait Between Runs

```java
@Scheduled(fixedDelay = 86_400_000) // 24 hours between runs
public void processData() {
    // Your task code
}
```

This waits 24 hours after the task finishes before starting again. If your task takes 30 minutes, the next run starts 24 hours and 30 minutes after the original start time.

**Use this when:**

- Your task might take different amounts of time
- You're making network calls or database operations
- You have retry logic that could make runs longer
- You don't want multiple instances running at once

### Cron Expressions - Run at Specific Times

```java
@Scheduled(cron = "0 0 0 * * ?") // Every day at midnight
public void generateDailyReport() {
    // Your task code
}
```

This runs at exact times based on a schedule. If your previous run is still going at midnight, Spring will start another one anyway.

**Use this when:**

- You need tasks to run at specific times
- Your tasks are quick and predictable
- Timing matters more than avoiding overlaps

### Which Should You Choose?

|                            | Fixed Delay                 | Cron                        |
| -------------------------- | --------------------------- | --------------------------- |
| **Multiple runs at once?** | Never                       | Possible                    |
| **Timing**                 | Consistent gaps             | Exact schedule              |
| **Good for**               | Long or unpredictable tasks | Quick, time-sensitive tasks |

**Why we use fixed delay:** Our billing task has retries and reactive code, so runs can take different amounts of time. We'd rather avoid overlapping runs than hit an exact schedule.

## Cron Expression Reference

Cron expressions follow this format: `second minute hour day-of-month month day-of-week`

Common examples:

- `0 0 0 * * ?` - Every day at midnight
- `0 0 9 * * MON-FRI` - Every weekday at 9 AM
- `0 0 */6 * * ?` - Every 6 hours

Use `?` when you don't care about that field (usually for day-of-month or day-of-week).

For more complex expressions, check the [Spring CronExpression docs](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/scheduling/support/CronExpression.html).

## Testing Your Scheduled Tasks

Testing scheduled tasks is straightforward - just call the method directly and verify it does what you expect:

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

        billingScheduler.markOverdueBills();

        Mockito.verify(billService, Mockito.times(1)).updateOverdueBills();
    }
}
```

**What this test covers:**

- Verifies your scheduled method calls the right service method
- Makes sure the basic wiring works

**What it doesn't test:**

- Whether Spring actually runs the method on schedule
- The business logic inside your service methods

> **Testing the actual scheduling:** If you want to test that Spring's scheduling really works, you'll need integration tests. Look into `ScheduledTaskRegistrar` or `@DirtiesContext` for managing scheduled tasks in tests.

## Dependencies

For most Spring Boot projects, you just need:

```gradle
testImplementation 'org.springframework.boot:spring-boot-starter-test'
testImplementation 'io.projectreactor:reactor-test'
```

The first one includes Mockito and JUnit, so you don't need to add them separately.

Test your business logic separately from your scheduling logic - it keeps things simpler.

## Further Reading

- [Baeldung: Spring Scheduled Tasks](https://www.baeldung.com/spring-scheduled-tasks)
- [Spring Guide: Scheduling Tasks](https://spring.io/guides/gs/scheduling-tasks)

---

**Tip:** Before adding new dependencies, check if they're already included in `spring-boot-starter-test`. It includes most testing libraries you'll need.

Back to [Java Coding Standards](./java-coding-standards.md)
