using Quartz;
using Quartz.Impl;
using System.Threading.Tasks;

namespace BillingScheduler
{
    public static class SchedulerStartup
    {
        public static async Task StartSchedulerAsync()
        {
            var scheduler = await StdSchedulerFactory.GetDefaultScheduler();
            await scheduler.Start();

            // Register BillStatusUpdaterJob to run daily at 2:00 AM
            var job = JobBuilder.Create<BillStatusUpdaterJob>()
                .WithIdentity("billStatusUpdater", "billing")
                .Build();

            var trigger = TriggerBuilder.Create()
                .WithIdentity("dailyTrigger", "billing")
                .StartNow()
                .WithDailyTimeIntervalSchedule(x => x
                    .StartingDailyAt(TimeOfDay.HourAndMinuteOfDay(2, 0))) // 2:00 AM daily
                .Build();

            await scheduler.ScheduleJob(job, trigger);
        }
    }
}
