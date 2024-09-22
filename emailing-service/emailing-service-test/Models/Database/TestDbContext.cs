using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using emailing_service.Models.Database;
using emailing_service.Utils;
using Microsoft.EntityFrameworkCore;

namespace emailing_service_test;

public class TestDbContext : IDatabaseHelper
{
    private readonly List<EmailModel> _emails;

    public TestDbContext()
    {
        _emails = new List<EmailModel>();
    }

    public Task CreateTableAsync()
    {
        return Task.CompletedTask;
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

    public Task<List<EmailModel>> GetAllEmailsAsync()
    {
        return Task.FromResult(_emails.ToList());
    }
}

