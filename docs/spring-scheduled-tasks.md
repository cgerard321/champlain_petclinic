# Scheduled Tasks in Spring Boot

This section describes how to implement scheduled tasks in a Spring Boot application using the `@Scheduled` annotation.

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

## Creating a Scheduled Task

Create a component class and use the `@Scheduled` annotation:

```java
@Component
public class BillingScheduler {
    /**
     * Schedules the markOverdueBills method to run every day at midnight.
     *
     * Cron notation explained:
     *   - Format: second minute hour day-of-month month day-of-week [year]
     *   - "0 0 0 * * ?" means: at 00:00:00 (midnight) every day
     *   - '?' is used for 'no specific value' in day-of-month or day-of-week
     *   - For more details, see:
     *     https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/scheduling/support/CronExpression.html
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void markOverdueBills() {
        // Your logic here
    }
}
```

## Example: Mark Overdue Bills

In the billing service, you can schedule a method to update bills whose due date has passed:

```java
@Component
public class BillingScheduler {
    @Autowired
    private BillService billService;

    @Scheduled(cron = "0 0 0 * * ?")
    public void markOverdueBills() {
        billService.updateOverdueBills().subscribe();
    }
}
```

## How to Unit Test a Scheduled Task

### Dependencies for Unit Testing

The example test uses Mockito and JUnit Jupiter. In most Spring Boot projects, you only need the following test dependencies:

- `spring-boot-starter-test` (includes JUnit Jupiter, Mockito, and other common test libraries)
- `io.projectreactor:reactor-test` (for testing reactive code)

These are already included in your `build.gradle`:

```gradle
testImplementation 'org.springframework.boot:spring-boot-starter-test'
testImplementation 'io.projectreactor:reactor-test'
```

You do not need to add Mockito or JUnit separately unless you require a specific version or feature.

You can write a unit test to verify that your scheduled method calls the correct service method. For example:

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

**What does this test actually check?**

- It verifies that when the scheduled method is called, it triggers the service method (`updateOverdueBills`).
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
