namespace emailing_service.Models.Database;

public interface IDatabaseHelper
{
    Task CreateTableAsync(int ticks);
    Task AddEmailAsync(string email, string subject, string body, string emailStatus);
    Task AddEmailNotificationAsync(string email, string subject, string body, string emailStatus, DateTime sendTime);
    Task<List<EmailModel>> GetAllEmailsAsync();
    Task<List<EmailModelNotification>> GetAllEmailsNotificationAsync();
    Task MoveEmailNotificationToEmailsAsync(int id, String result);


}