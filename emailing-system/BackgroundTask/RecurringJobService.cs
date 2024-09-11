namespace emailing_system.BackgroundTask;

using Microsoft.Extensions.Hosting;
using Microsoft.Extensions.Logging;
using System;
using System.Threading;
using System.Threading.Tasks;

public class RecurringJobService : BackgroundService
{
    private readonly ILogger<RecurringJobService> _logger;
    private Timer _timer;
    /// <summary>
    /// Simple logger constructor
    /// </summary>
    /// <param name="logger"></param>
    public RecurringJobService(ILogger<RecurringJobService> logger)
    {
        _logger = logger;
    }
    /// <summary>
    /// Initial event function called. Builds
    /// </summary>
    /// <param name="stoppingToken">Necessary for background task by ASP</param>
    /// <returns></returns>
    protected override Task ExecuteAsync(CancellationToken stoppingToken)
    {
        var now = DateTime.Now;
        var initialDelay = CalculateInitialDelay(now);
        var period = TimeSpan.FromMinutes(30);
        // Ensure that the initial delay is not negative
        if (initialDelay < TimeSpan.Zero)
        {
            initialDelay = TimeSpan.Zero;
        }
        _logger.LogInformation($"Recurring job started in: {initialDelay}");

        _timer = new Timer(DoWork, null, initialDelay, period);
        return Task.CompletedTask;
    }
    /// <summary>
    /// Calculate the initial delay before we reach a round half hour ( EX : 19:00, 19:30, 20:00 and etc... ) 
    /// </summary>
    /// <param name="now">Current time</param>
    /// <returns>The time precise time before the work should start</returns>
    private TimeSpan CalculateInitialDelay(DateTime now)
    {
        // Determine the next interval (00 or 30 minutes past the hour)
        int minutesPast = now.Minute % 30;
        TimeSpan nextExecutionTime;

        if (minutesPast == 0)
        {
            // Current time is exactly at a desired interval, so set delay to zero
            nextExecutionTime = TimeSpan.Zero;
        }
        else
        {
            // Calculate minutes to the next interval
            int minutesToNextInterval = 30 - minutesPast;
            nextExecutionTime = TimeSpan.FromMinutes(minutesToNextInterval);
        }

        // Return the calculated delay
        return nextExecutionTime;
    }



    /// <summary>
    /// The task that gets called every half hour
    /// </summary>
    /// <param name="state">Unused parameter</param>
    private void DoWork(object state)
    {
        _logger.LogInformation("Recurring task executed at: {time}", DateTime.Now);
        
        // Your recurring task logic here
    }
    /// <summary>
    /// What happens when the task is cancelled. Will only happen when the program will close.
    /// </summary>
    /// <param name="cancellationToken"></param>
    /// <returns></returns>
    public override Task StopAsync(CancellationToken cancellationToken)
    {
        _timer?.Change(Timeout.Infinite, 0);
        return base.StopAsync(cancellationToken);
    }
}


