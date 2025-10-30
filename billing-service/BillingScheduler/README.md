# BillingScheduler Service

## Overview

Automates billing tasks (e.g., updating overdue bills) using Quartz.NET in a .NET Worker Service.

## How to Run

1. Open a terminal in `BillingScheduler`.
2. Restore dependencies:
   ```
   dotnet restore
   ```
3. Build and run:
   ```
   dotnet build
   dotnet run
   ```
4. The scheduler will run the BillStatusUpdaterJob daily at 2:00 AM.

## How to Add a New Job

1. Create a new class implementing `Quartz.IJob` (e.g., `PaymentReminderJob`).

   ```csharp
   using Quartz;
   using System.Threading.Tasks;

   public class PaymentReminderJob : IJob
   {
       public Task Execute(IJobExecutionContext context)
       {
           // TODO: Add logic to send payment reminders
           Console.WriteLine("Payment reminders sent.");
           return Task.CompletedTask;
       }
   }
   ```

2. Register the job and its trigger in `SchedulerStartup.cs`:

   ```csharp
   var reminderJob = JobBuilder.Create<PaymentReminderJob>()
       .WithIdentity("paymentReminder", "billing")
       .Build();

   var reminderTrigger = TriggerBuilder.Create()
       .WithIdentity("reminderTrigger", "billing")
       .StartNow()
       .WithDailyTimeIntervalSchedule(x => x
           .StartingDailyAt(TimeOfDay.HourAndMinuteOfDay(9, 0))) // 9:00 AM daily
       .Build();

   await scheduler.ScheduleJob(reminderJob, reminderTrigger);
   ```

3. Build and run the service. The new job will execute at the scheduled time.

## Customization

- Edit `BillStatusUpdaterJob.cs` to connect to the billing MongoDB database using the MongoDB C# driver.
- Database connection details (host, port, database name, username, password, authentication database) are configured via environment variables in docker-compose:
  ```yaml
  environment:
    MONGO_HOST: mongo-billing
    MONGO_PORT: 27017
    MONGO_DB: billings
    MONGO_USER: root
    MONGO_PASS: password
    MONGO_AUTH_DB: admin
  ```
- The schedule for jobs is set in `SchedulerStartup.cs`.
- Job logic can be modified to query and update bills directly in MongoDB as required.

## Monitoring & Logs

- Job execution and errors are logged to the console.
- Console output is visible in Docker container logs and can be viewed using tools such as Dozzle, Portainer, or the `docker logs` command.
- For production, integrate Serilog or NLog for structured logging.

## Troubleshooting

- Ensure Quartz.NET is installed.
- Check for errors in the console output.
- Verify job schedules and triggers in `SchedulerStartup.cs`.

## Maintenance

- To update job logic, edit the relevant job class.
- To change schedules, update the trigger configuration.
- Restart the service after making changes.
