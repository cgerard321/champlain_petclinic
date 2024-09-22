

using emailing_service.Models.SMTP;

namespace emailing_service.Utils;

using System.Text.RegularExpressions;
using System;
using System.Net;
using System.Net.Mail;
using System.Threading.Tasks;
using Microsoft.Extensions.Configuration;
using emailing_service.Models;

public static class EmailUtils
{
    public static ConnectionEmailServer emailConnectionString;
    public static List<EmailTemplate> EmailTemplates = new List<EmailTemplate>();
    public static bool sendEmail = true;
    public static ISmtpClient smtpClient;
    
    //Can be disabled mostly for testing
    //public static bool enableLogging = true;
    


    public static void SetUpEmailUtils()
    {
        smtpClient = new SmtpClientWrapper(
            new SmtpClient(emailConnectionString.SmtpServer, emailConnectionString.Port)
            {
                Credentials = new NetworkCredential(emailConnectionString.Username, emailConnectionString.Password),
                EnableSsl = true,
                UseDefaultCredentials = false, // Ensure we're not using system defaults
                Timeout = 10000 // Set a reasonable timeout
            }
        );
    }
    
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
    /// <summary>
    /// Configured for mailsend
    /// </summary>
    /// <param name="to"></param>
    /// <param name="subject"></param>
    /// <param name="body"></param>
    /// <param name="isBodyHtml"></param>
    public static async Task<SendEmailResult> SendEmailAsync(string to, string subject, string body, ISmtpClient smtpClient, bool isBodyHtml = true)
    {
        var fromAddress = new MailAddress(emailConnectionString.Email, emailConnectionString.DisplayName);
        var toAddress = new MailAddress(to);

        using (var mailMessage = new MailMessage(fromAddress, toAddress)
               {
                   Subject = subject,
                   Body = body,
                   IsBodyHtml = isBodyHtml
               })
        {
            try
            {
                if (sendEmail)
                    await smtpClient.SendMailAsync(mailMessage);
                Console.WriteLine("Email sent successfully.");
                return new SendEmailResult { Status = "Sent", ErrorMessage = null };
            }
            catch (System.Exception ex)
            {
                Console.WriteLine($"Failed to send email: {ex.Message}");
                return new SendEmailResult { Status = "Failed", ErrorMessage = ex.Message };
            }
        }
    }

    public class SendEmailResult
    {
        public string Status { get; set; }
        public string ErrorMessage { get; set; }
    }

}