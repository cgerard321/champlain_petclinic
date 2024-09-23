using System.Net.Mail;
using System.Threading.Tasks;
using emailing_service.Models;
using emailing_service.Models.SMTP;
using emailing_service.Utils;
using Moq;
using NUnit.Framework;

namespace emailing_service_test.Utils;
[TestFixture]
public class EmailUtilsTest
{
    private Mock<ISmtpClient> _smtpClientMock;
    

    [OneTimeSetUp]
    public void SetUp()
    {
        EmailUtils.emailConnectionString  = new ConnectionEmailServer(
            "Mock",
            000,
            "Mock",
            "Mock",
            "mockemail@gmail.com",
            "MockPetClinic"
        );
        _smtpClientMock = new Mock<ISmtpClient>();
    }

    [Test]
    public void SetUpEmailUtilsTest_ValidConnectionString_ReturnVoid()
    {
        EmailUtils.emailConnectionString  = new ConnectionEmailServer(
            "Mock",
            000,
            "Mock",
            "Mock",
            "mockemail@gmail.com",
            "MockPetClinic"
        );
        Assert.DoesNotThrow(() => EmailUtils.SetUpEmailUtils());
    }
    [Test]
    public void SetUpEmailUtilsTest_UnModifiedEmailConnectionString_ReturnNullReferenceException()
    {
        EmailUtils.emailConnectionString   = null;
        Assert.ThrowsAsync<System.NullReferenceException>(async () =>
            EmailUtils.SetUpEmailUtils()
        );
    }
    
    
    [Test]
    public void EmailTemplates_ShouldBeInitializedCorrectly()
    {
        Assert.NotNull(EmailUtils.EmailTemplates);
    }
    
    [Test]
    [TestCase("xilef992@gmail.com")]
    [TestCase("example@gmail.com")]
    [TestCase("example@gmail.ca")]
    [TestCase("example.123@gmail.ca")]
    [TestCase("example@outlook.com.com")]
    public void CheckIfEmailIsValid_ShouldReturnTrue_WhenEmailIsValid(string email)
    {
        bool result = EmailUtils.CheckIfEmailIsValid(email);
        Assert.That(result, Is.True);
    }
    
    
    [Test]
    [TestCase("xilef@992@gmail.com")]
    [TestCase("example@gmail")]
    [TestCase("example@.ca")]
    [TestCase("@example@outlook.com")]
    public void CheckIfEmailIsValid_ShouldReturnFalse_WhenEmailIsInvalid(string email)
    {
        bool result = EmailUtils.CheckIfEmailIsValid(email);
        Assert.That(result, Is.False);
    }
    [Test]
    [TestCase(null)]
    [TestCase("")]
    [TestCase("    ")]
    public void CheckIfEmailIsValid_ShouldReturnFalse_WhenNullOrEmpty(string? email)
    {
        bool result = EmailUtils.CheckIfEmailIsValid(email);
        Assert.That(result, Is.False);
    }

    
    

    [Test]
    public async Task SendEmailAsync_ValidEmail_SendsEmail()
    {
        // Arrange
        var toEmail = "test@example.com";
        var subject = "Test Subject";
        var body = "This is the test email body.";
        var isBodyHtml = true;

        _smtpClientMock.Setup(smtp => smtp.SendMailAsync(It.IsAny<MailMessage>()))
            .Returns(Task.CompletedTask);

        EmailUtils.sendEmail = true; // Ensure the sendEmail flag is set to true

        // Act
        await EmailUtils.SendEmailAsync(toEmail, subject, body, _smtpClientMock.Object, isBodyHtml);

        // Assert
        _smtpClientMock.Verify(smtp => smtp.SendMailAsync(It.IsAny<MailMessage>()), Times.Once);
    }

    [Test]
    public void SendEmailAsync_InvalidEmail_ThrowsException()
    {
        // Arrange
        var invalidEmail = "invalidEmail";
        var subject = "Test Subject";
        var body = "This is the test email body.";
        var isBodyHtml = true;

        // Act & Assert
        Assert.ThrowsAsync<System.FormatException>(async () =>
            await EmailUtils.SendEmailAsync(invalidEmail, subject, body, _smtpClientMock.Object, isBodyHtml)
        );
    }
}