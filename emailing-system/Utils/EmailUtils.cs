using System.Text.RegularExpressions;
using emailing_system.Models;
using System;
using System.Net;
using System.Net.Mail;
using System.Threading.Tasks;
using Microsoft.Extensions.Configuration;


namespace emailing_system.Utils;

public static class EmailUtils
{
    public static ConnectionEmailServer emailConnectionString;
    public static bool CheckIfEmailIsValid(string email)
    {
        if (string.IsNullOrWhiteSpace(email))
        {
            return false;
        }

        // Regular expression for validating an email
        string emailPattern = @"^[^@\s]+@[^@\s]+\.[^@\s]+$";
        
        return Regex.IsMatch(email, emailPattern);
    }

    public static async Task SendEmailAsync(string to, string subject, string body, bool isBodyHtml = true)
    {
        var fromAddress = new MailAddress(emailConnectionString.Email, emailConnectionString.DisplayName);
        var toAddress = new MailAddress(to);

        var smtpClient = new SmtpClient(emailConnectionString.SmtpServer, emailConnectionString.Port)
        {
            Credentials = new NetworkCredential(emailConnectionString.Username, emailConnectionString.Password),
            EnableSsl = false
        };

        var mailMessage = new MailMessage(fromAddress, toAddress)
        {
            Subject = subject,
            Body = body,
            IsBodyHtml = isBodyHtml
        };

        try
        {
            await smtpClient.SendMailAsync(mailMessage);
            Console.WriteLine("Email sent successfully.");
        }
        catch (System.Exception ex)
        {
            Console.WriteLine($"Failed to send email: {ex.Message}");
        }
    }

}