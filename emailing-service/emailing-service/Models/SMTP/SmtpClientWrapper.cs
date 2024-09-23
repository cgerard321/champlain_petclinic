using System.Net.Mail;

namespace emailing_service.Models.SMTP;

public class SmtpClientWrapper : ISmtpClient
{
    private readonly SmtpClient _smtpClient;

    public SmtpClientWrapper(SmtpClient smtpClient)
    {
        _smtpClient = smtpClient;
    }

    public async Task SendMailAsync(MailMessage message)
    {
        await _smtpClient.SendMailAsync(message);
    }
}