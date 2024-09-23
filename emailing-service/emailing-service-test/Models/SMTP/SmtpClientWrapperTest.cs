using System.Net;
using System.Net.Mail;
using System.Threading.Tasks;
using emailing_service.Models.SMTP;
using Moq;
using NUnit.Framework;

namespace emailing_service_test.Models.SMTP;
[TestFixture]
public class SmtpClientWrapperTest
{
    [Test]
    public async Task SendMailAsync_ValidMailMessage_NoConnectionExceptionThrown()
    {
        // Arrange
        var smtpClient = new SmtpClient
        {
            Host = "localhost",
            Port = 25,
            EnableSsl = false,
            UseDefaultCredentials = false
        };

        var smtpClientWrapper = new SmtpClientWrapper(smtpClient);
        var mailMessage = new MailMessage("from@example.com", "to@example.com", "Subject", "Body");

        // Act and Assert
        //WE ARE EXCEPTING AN EXCEPTION BECAUSE WE HAVE NO SMTP SERVER LSITENING
        Assert.ThrowsAsync<SmtpException>(() => smtpClientWrapper.SendMailAsync(mailMessage));
    }
}