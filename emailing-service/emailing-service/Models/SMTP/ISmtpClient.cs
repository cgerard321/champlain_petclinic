namespace emailing_service.Models.SMTP;
using System.Net.Mail;

public interface ISmtpClient
{
    Task SendMailAsync(MailMessage message);
}