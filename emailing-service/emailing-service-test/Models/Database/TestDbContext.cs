using System.Globalization;
using emailing_service.Models.Database;

namespace emailing_service_test.Models.Database;

public class TestDbContext : IDatabaseHelper
{
    private List<EmailModel> _emails;
    private List<EmailModelNotification> _emailsNotifications;
    public TestDbContext()
    {
        _emails = new List<EmailModel>();
        _emailsNotifications = new List<EmailModelNotification>();
    }

    public Task CreateTableAsync()
    {
        return Task.CompletedTask;
    }

    public Task CreateTableAsync(int ticks)
    {
        return Task.FromResult(_emails);
    }

    public Task AddEmailAsync(string email, string subject, string body, string emailStatus)
    {
        var newEmail = new EmailModel
        {
            Id = _emails.Count + 1, 
            Email = email,
            Subject = subject,
            Body = body,
            EmailStatus = emailStatus
        };
        
        _emails.Add(newEmail); 
        return Task.CompletedTask;
    }

    public Task AddEmailNotificationAsync(string email, string subject, string body, string emailStatus, DateTime sendTime)
    {
        var newEmail = new EmailModelNotification
        {
            Id = _emails.Count + 1, 
            Email = email,
            Subject = subject,
            Body = body,
            EmailStatus = emailStatus,
            SendTime = sendTime
        };
        
        _emailsNotifications.Add(newEmail); 
        return Task.CompletedTask;
    }

    public Task<List<EmailModel>> GetAllEmailsAsync()
    {
        return Task.FromResult(_emails.ToList());
    }

    public Task<List<EmailModelNotification>> GetAllEmailsNotificationAsync()
    {
        return Task.FromResult(_emailsNotifications.ToList());
    }

    public Task MoveEmailNotificationToEmailsAsync(int id, String result)
    {
        EmailModelNotification? emailNotification = _emailsNotifications.Find(email => email.Id == id);
        if (emailNotification != null)
        {
            _emails.Add(new EmailModel()
            {
                Id = emailNotification.Id,
                Body = emailNotification.Body,
                Subject = emailNotification.Subject,
                EmailStatus = result
            });
            return Task.FromResult(_emailsNotifications.Remove(emailNotification));
        }
        return Task.FromResult(emailNotification);
    }
}

