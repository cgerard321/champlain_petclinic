using emailing_service.Models;
using emailing_service.Models.Database;
using emailing_service.Models.EmailType;

namespace emailing_service.BuisnessLayer;

public interface IEmailService
{
    public void SetDatabaseHelper(IDatabaseHelper databaseHelper);
    public List<EmailModel> GetAllEmails();
    public OperationResult ReceiveHtml(string templateName, string htmlBody);
    public OperationResult SendEmail(DirectEmailModel model);
    public OperationResult SendEmailNotification(NotificationEmailModel model);
    public OperationResult SendRawEmail(RawEmailModel model);
}