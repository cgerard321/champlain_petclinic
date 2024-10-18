using emailing_service.Models;
using emailing_service.Models.Database;
using emailing_service.Models.EmailType;

namespace emailing_service.BuisnessLayer
{
    public interface IEmailService
    {
        void SetDatabaseHelper(IDatabaseHelper databaseHelper);
        List<EmailModel> GetAllEmails();
        OperationResult ReceiveHtml(string templateName, string htmlBody);
        OperationResult SendEmail(DirectEmailModel model);
        OperationResult SendEmailNotification(NotificationEmailModel model);
        OperationResult SendRawEmail(RawEmailModel model);
        //OperationResult SendReminderEmail(ReminderEmailModel model);
    }
}