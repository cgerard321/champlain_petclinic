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
    @Scheduled(cron = "0 0 0 * * ?") // Runs every day at midnight
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

For more Java practices, see [java-coding-standards.md](./java-coding-standards.md).
