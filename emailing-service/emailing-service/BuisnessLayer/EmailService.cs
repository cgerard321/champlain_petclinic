using emailing_service.Models;
using emailing_service.Models.EmailType;

namespace emailing_service.BuisnessLayer;

public interface IEmailService
{
    public OperationResult ReceiveHtml(string templateName, string htmlBody);
    public void SendEmail(DirectEmailModel model);
}