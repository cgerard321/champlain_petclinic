using emailing_service.BuisnessLayer;
using emailing_service.Models.Database;
using emailing_service.Models.EmailType;
using emailing_service.Utils;

namespace emailing_service.BackgroundTask;

using Microsoft.Extensions.Hosting;
using Microsoft.Extensions.Logging;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;

public class RecurringJobService : BackgroundService
{
    private readonly ILogger<RecurringJobService> _logger;
    private Timer _timer = null!;
    private IDatabaseHelper _databaseHelper;
    /// <summary>
    /// Simple logger constructor
    /// </summary>
    /// <param name="logger"></param>
    public RecurringJobService(ILogger<RecurringJobService> logger)
    {
        _logger = logger;
        _databaseHelper = new DatabaseHelper();
    }
    public RecurringJobService(ILogger<RecurringJobService> logger, IDatabaseHelper databaseHelper)
    {
        _logger = logger;
        _databaseHelper = databaseHelper;
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
        var period = TimeSpan.FromMinutes(1); // Check every minute

        // Ensure that the initial delay is not negative
        if (initialDelay < TimeSpan.Zero)
        {
            initialDelay = TimeSpan.Zero;
        }

        _logger.LogInformation($"Recurring job started with initial delay of: {initialDelay}");
        _timer = new Timer(DoWork, null, initialDelay, period);
        return Task.CompletedTask;
    }

    /// <summary>
    /// Calculate the initial delay before we reach a round minute
    /// </summary>
    /// <param name="now">Current time</param>
    /// <returns>The time precise time before the work should start</returns>
    private TimeSpan CalculateInitialDelay(DateTime now)
    {
        // Calculate the time to the next minute
        return TimeSpan.FromSeconds(60 - now.Second);
    }

    /// <summary>
    /// The task that gets called every minute
    /// </summary>
    /// <param name="state">Unused parameter</param>
    public async void DoWork(object? state)
    {
        DateTime now = TimeZoneInfo.ConvertTimeFromUtc(DateTime.UtcNow,
            TimeZoneInfo.CreateCustomTimeZone("UTC-4", new TimeSpan(-4, 0, 0), "UTC-4", "UTC-4"));
        
        DateTime nowWithoutSeconds = new DateTime(now.Year, now.Month, now.Day, now.Hour, now.Minute, 0);
        DateTime OneMinuteFromNow = nowWithoutSeconds.AddMinutes(1);
        _logger.LogInformation("Recurring task executed at: {time}", now);
        
        // Fetch emails scheduled to be sent at the current time
        List<EmailModelNotification> emailsToSend;
        try
        {
            emailsToSend = await _databaseHelper.GetAllEmailsNotificationAsync();
        }
        catch (emailing_service.Utils.Exception.MissingDatabaseException ex)
        {
            Console.WriteLine("Database Offline, we could not send the message");             
            emailsToSend = new List<EmailModelNotification>();
        }

        
        foreach (var email in emailsToSend)
        {
            if (email.SendTime < OneMinuteFromNow)
            {
                if (email.SendTime >= nowWithoutSeconds)
                { 
                    var sendEmailResult = await EmailUtils.SendEmailAsync(
                        email.Email,
                        email.Subject,
                        email.Body,
                        EmailUtils.smtpClient
                    );
                    if (sendEmailResult.Status == "Sent")
                    {
                        await _databaseHelper.MoveEmailNotificationToEmailsAsync(email.Id, "Sent");
                    }
                    else
                    {
                        await _databaseHelper.MoveEmailNotificationToEmailsAsync(email.Id, "Failed");
                    }
                    
                }
                else
                {
                    await _databaseHelper.MoveEmailNotificationToEmailsAsync(email.Id,"Failed");
                }
            }
        }
    }



    /// <summary>
    /// What happens when the task is cancelled. Will only happen when the program will close.
    /// </summary>
    /// <param name="cancellationToken"></param>
    /// <returns></returns>
    public override Task StopAsync(CancellationToken cancellationToken)
    {
        _timer.Change(Timeout.Infinite, 0);
        return base.StopAsync(cancellationToken);
    }
}
