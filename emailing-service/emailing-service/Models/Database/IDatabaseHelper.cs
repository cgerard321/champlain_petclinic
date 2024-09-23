namespace emailing_service.Models.Database;

public interface IDatabaseHelper
{
    Task CreateTableAsync(int ticks);
    Task AddEmailAsync(string email, string subject, string body, string emailStatus);
    Task<List<EmailModel>> GetAllEmailsAsync();
}